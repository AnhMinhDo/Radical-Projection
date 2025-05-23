package RadicalProjectionMain.Segmentation;

import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.ops.OpService;
import net.imagej.ops.Ops;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss3.Gauss3;
import net.imglib2.algorithm.gradient.HessianMatrix;
import net.imglib2.algorithm.labeling.ConnectedComponents;
import net.imglib2.algorithm.labeling.Watershed;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.app.StatusService;
import net.imagej.ops.OpService;
import org.scijava.plugin.Parameter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import static ij.IJ.debugMode;

public class CreateHybridStack {
    private final Path filePath;
    private int weightLignin;
    private int weightCellulose;
    private final Context context;
    private int windowSize;

    @Parameter
    private OpService ops;

    public CreateHybridStack(Context context,
                             Path filePath,
                             int weight,
                             int windowSize) {
        this.filePath = filePath;
        this.weightCellulose = weight;
        this.weightLignin = 100-weight;
        this.context = context;
        this.ops = context.service(OpService.class);
        this.windowSize = windowSize;
    }

    public void process() throws IOException {
        // get the status Service
        StatusService statusService = context.getService(StatusService.class);
        //
//        OpService ops = context.getService(OpService.class);
        // Get DatasetService and UIService from context
        DatasetIOService ioService = context.getService(DatasetIOService.class);
        // load the image
        Dataset img = ioService.open(filePath.toString());
        // open file with file path, pre-condition: the input image is 16-bit
        ImgPlus<?> genericImgPlus = img.getImgPlus();
        // Verify the type
        if (!(genericImgPlus.firstElement() instanceof UnsignedShortType)) {
            throw new IllegalArgumentException("Expected ShortType(16-bit) image");
        }

        ImgPlus<UnsignedShortType> imgPlus = (ImgPlus<UnsignedShortType>) genericImgPlus;
        // check if the image has channels
        int channelDimIdx = imgPlus.dimensionIndex(Axes.CHANNEL);
        boolean hasChannels = (channelDimIdx >= 0);
        long numChannels = hasChannels ? imgPlus.dimension(channelDimIdx) : 1;
        // container for post-processed channels
        ArrayList<RandomAccessibleInterval<UnsignedShortType>> processedChannels = new ArrayList<>();
        // debugging
        if (debugMode){
            for (int d = 0; d < imgPlus.numDimensions(); d++) {
                AxisType axis = imgPlus.axis(d).type();
                String axisName = axis.getLabel();  // Returns "X", "Y", "Z", "Channel", "Time", etc.
                double scale = imgPlus.averageScale(d);  // Pixel spacing for this axis
                String unit = imgPlus.axis(d).unit();    // Unit (e.g., "mm", "µm", "s")

                System.err.println(
                        "Dimension " + d + ": " +
                                "Type = " + axisName + ", " +
                                "Size = " + imgPlus.dimension(d) + ", " +
                                "Scale = " + scale + " " + unit
                );
            }
        }
        // get the dimension index:
        int xDim = imgPlus.dimensionIndex(Axes.X);
        int yDim = imgPlus.dimensionIndex(Axes.Y);
        int zDim = imgPlus.dimensionIndex(Axes.Z);
        // get the spatial dimension size
        long width = imgPlus.dimension(xDim);
        long height = imgPlus.dimension(yDim);
        long depth = imgPlus.dimension(zDim);
        // ratio for each channel
        double weightCelluloseRatio = (double) weightCellulose /100;
        double weightLigninRatio = (double) weightLignin /100;
        // convert weight from double to FloatType
        FloatType weightCelluloseRatioFloatType = new FloatType((float)weightCelluloseRatio);
        FloatType weightLigninRatioFloatType = new FloatType((float)weightLigninRatio);
        // split the channels
        RandomAccessibleInterval<FloatType> cellulose = ops.convert().float32(Views.hyperSlice(imgPlus, channelDimIdx, 0));
        RandomAccessibleInterval<FloatType> lignin = ops.convert().float32(Views.hyperSlice(imgPlus, channelDimIdx, 1));
        RandomAccessibleInterval<FloatType> hybrid = ops.convert().float32(Views.hyperSlice(imgPlus, channelDimIdx, 1));
        // Containers for result
        RandomAccessibleInterval<FloatType> celluloseMultiplied =
                ops.create().img(cellulose);
        RandomAccessibleInterval<FloatType> ligninMultiplied =
                ops.create().img(lignin);
        RandomAccessibleInterval<FloatType> projectedstack =
                ops.create().img(lignin);
        // multiply with weight
        ops.math().multiply(celluloseMultiplied, cellulose, weightCelluloseRatioFloatType);
        ops.math().multiply(ligninMultiplied, lignin, weightLigninRatioFloatType);
        ops.math().add(hybrid,celluloseMultiplied,ligninMultiplied);
        ImageJFunctions.show(hybrid);
        // perform window sliding Projection, each new slide is the average projection of all the slide in the window
        for (long z = 0; z < depth; z++) {
            // Determine the slice window (handle boundaries)
            long startSlice = Math.max(0, z - windowSize / 2);
            long endSlice = Math.min(depth - 1, z + windowSize / 2);
            int numSlicesInWindow = (int) (endSlice - startSlice + 1);

            // Get the output slice (2D)
            RandomAccessibleInterval<FloatType> outputSlice = Views.hyperSlice(projectedstack, 2, z);
            // Initialize a sum buffer for the output slice
            float[] sum = new float[(int) (width * height)];
            // Accumulate values from neighboring slices
            for (long zz = startSlice; zz <= endSlice; zz++) {
                RandomAccessibleInterval<FloatType> inputSlice = Views.hyperSlice(hybrid, 2, zz);
                Cursor<FloatType> cursor = Views.flatIterable(inputSlice).cursor();

                int pixelIndex = 0;
                while (cursor.hasNext()) {
                    sum[pixelIndex] += cursor.next().get();
                    pixelIndex++;
                }
            }
            // Compute average and write to output
            Cursor<FloatType> outputCursor = Views.flatIterable(outputSlice).cursor();
            for (float s : sum) {
                outputCursor.next().set(s / numSlicesInWindow);
            }
        }
        ImageJFunctions.show(projectedstack);
//        // containers for smooth and watershed
//        RandomAccessibleInterval<FloatType> smoothed = ops.create().img(projectedstack);
//        // Smooth the image
//        Gauss3.gauss(new double[]{2.0, 2.0, 2.0}, Views.extendMirrorSingle(projectedstack), smoothed);
//        // calculate the gradient
//        Img<FloatType> gradient = ops.create().img(projectedstack);
//        HessianMatrix.calculateMatrix(Views.extendMirrorSingle(smoothed), gradient);
//        // Step 3: Watershed
////        Watershed.Configuration config = new Watershed.Configuration();
////        config.connectivity = 26; // 26-connectivity for 3D
//        Watershed<FloatType, FloatType> watershed = new Watershed<>(
//                smoothed,
//                gradient,
//                new ArrayImgFactory<>(new FloatType()), // output factory
////                StructuringElements.eightConnected(2),   // neighborhood
////                true // use priority queue
//        );
//        watershed.process();
//
//        // Step 4: Labeling
//        return ConnectedComponents.labelAllConnectedComponents(
//                watershed.getResult(),
//                StructuringElement.TWENTYSIX_CONNECTED);
    }

}
