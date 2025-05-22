package RadicalProjectionMain.Segmentation;

import ij.ImagePlus;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.BinaryImages;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.ops.OpService;
import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import org.scijava.Context;
import org.scijava.app.StatusService;
import org.scijava.plugin.Parameter;

import java.awt.*;
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
    private double sigmaValueForGaussianFilter;
    private int diameter;

    @Parameter
    private OpService ops;

    public CreateHybridStack(Context context,
                             Path filePath,
                             int weight,
                             int windowSize,
                             double sigmaValueForGaussianFilter,
                             int diameter) {
        this.filePath = filePath;
        this.weightCellulose = weight;
        this.weightLignin = 100-weight;
        this.context = context;
        this.ops = context.service(OpService.class);
        this.windowSize = windowSize;
        this.sigmaValueForGaussianFilter = sigmaValueForGaussianFilter;
        this.diameter= diameter;
    }

    public RandomAccessibleInterval<FloatType> process() throws IOException {
        // get the status Service
        StatusService statusService = context.getService(StatusService.class);
        //
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
        RandomAccessibleInterval<FloatType> projectedStack =
                ops.create().img(lignin);
        RandomAccessibleInterval<FloatType> smoothedStack =
                ops.create().img(lignin);
        // multiply with weight
        ops.math().multiply(celluloseMultiplied, cellulose, weightCelluloseRatioFloatType);
        ops.math().multiply(ligninMultiplied, lignin, weightLigninRatioFloatType);
        ops.math().add(hybrid,celluloseMultiplied,ligninMultiplied);
        ImageJFunctions.show(hybrid,"Hybrid image");
        // perform window sliding Projection, each new slide is the average projection of all the slide in the window
        WindowSlidingProjection.averageProjection(hybrid,projectedStack,windowSize,(int)depth,(int)width,(int)height);
        ImageJFunctions.show(projectedStack, "Projected Stack");
        // smooth the image using gaussian filter
        ops.filter().gauss(smoothedStack,projectedStack,sigmaValueForGaussianFilter);
        ImageJFunctions.show(smoothedStack, "Smoothed Stack");
        // create a mask based on given coordinates
        Point point1 = new Point((int)114, (int)68);
        Point point2 = new Point((int)177, (int)32);
        Point pointForBackground = new Point((int)width-1, (int)height-1);
        // add all points to List
        ArrayList<Point> coordinates = new ArrayList<>();
        coordinates.add(point1);
        coordinates.add(point2);
        coordinates.add(pointForBackground);
        // Create innit mask
        CreateMask createMask = new CreateMask(coordinates,(int)width,(int)height,diameter);
        ImagePlus marker = createMask.drawMaskWithCoordinate();
        marker.show();
        // invert marker
        ImagePlus markerInverted = marker.duplicate();
        markerInverted.getProcessor().invert();
        // Compute Distance Transform using Chamfer method
        ChamferDistanceTransform2DFloat cdtf = new ChamferDistanceTransform2DFloat(ChamferMask2D.BORGEFORS);
        FloatProcessor markerDistanceTransformed = cdtf.distanceMap(markerInverted.getProcessor());
        ImagePlus markerDistanceTransformedImagePlus = new ImagePlus("markerDistanceTransformed", markerDistanceTransformed);
        markerDistanceTransformedImagePlus.show();
        // Threshold: Keep pixels where distance <= diameter
        ImageProcessor grownRegionProcessor = markerDistanceTransformed.duplicate();
        float[] markerDistanceTransformedFloatArray = (float[]) markerDistanceTransformed.getPixels();
        float[] grownRegionProcessorFloatArray = (float[]) grownRegionProcessor.getPixels();
        for (int p = 0; p < grownRegionProcessorFloatArray.length; p++) {
            grownRegionProcessorFloatArray[p] = (markerDistanceTransformedFloatArray[p]<=diameter) ? 255 : 0; // any point outside the range is marked as 0
        }
        // write the growRegion to imageplus
        ImagePlus growRegion = new ImagePlus("grown Region", grownRegionProcessor);
        growRegion.show();
        // extract 1 slice and convert to Imagej1 Format
        RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, 468); // dimension 2 = Z
        ImagePlus imageForReconstruction = ImageJFunctions.wrapFloat(slice2D, "mask for reconstruction");
        // Make sure the display range is set properly for float images
        imageForReconstruction.resetDisplayRange();
        // impose minima on growRegion image
        ImageProcessor reconstructedProcessor = MinimaAndMaxima.imposeMinima(imageForReconstruction.getProcessor(),
                                                                                growRegion.getProcessor(),8);
        reconstructedProcessor.convertToByte(true);
        ImagePlus reconstructedImagePlus = new ImagePlus("reconstructed Image", reconstructedProcessor);
        reconstructedImagePlus.show();
        // label minima using connected components (32-bit output)
        ImagePlus labeledMinima = BinaryImages.componentsLabeling( growRegion, 8, 32 );
        labeledMinima.show();
        // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
        ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                reconstructedImagePlus, 255,8
        );
        segmentedImage.show();
        return slice2D;
    }
}
