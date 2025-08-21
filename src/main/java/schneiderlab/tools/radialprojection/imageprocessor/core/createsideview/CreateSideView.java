package schneiderlab.tools.radialprojection.imageprocessor.core.createsideview;

import ij.IJ;
import io.scif.services.DatasetIOService;
import net.imagej.Dataset;
import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.AxisType;
import net.imagej.axis.DefaultLinearAxis;
import net.imagej.ops.OpService;
import net.imglib2.*;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.realtransform.AffineTransform3D;
import net.imglib2.realtransform.RealViews;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;
import org.scijava.Context;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.nio.file.Path;
import java.util.ArrayList;

import static ij.IJ.debugMode;

public class CreateSideView {
    private final Path filePath;
    private final Context context;
    private final int targetXYpixelSize;
    private final int targetZpixelSize;
    private final OpService ops;

    public CreateSideView(Context context,
                          Path filePath,
                          int targetXYpixelSize,
                          int targetZpixelSize) {
        this.filePath = filePath;
        this.context = context;
        this.targetXYpixelSize= targetXYpixelSize;
        this.targetZpixelSize=targetZpixelSize;
        this.ops = context.service(OpService.class);

    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private int currentProgress;
    private final double totalNumberOfSteps=10;

    public void setNewProgressValue(int newProgressValue) {
        int previousProgress = this.currentProgress;
        this.currentProgress = newProgressValue;
        this.pcs.firePropertyChange("progress", previousProgress, currentProgress);
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    public Img<UnsignedShortType> process() throws Exception {
        // get the status Service
//        StatusService statusService = context.getService(StatusService.class);
        // Get DatasetService and UIService from context
        IJ.showStatus("loading file: "+filePath.getFileName().toString());
        DatasetIOService ioService = context.getService(DatasetIOService.class);
        setNewProgressValue((int)(1*(100/totalNumberOfSteps))); // update ProgressBar
        // load the image
        Dataset img = ioService.open(filePath.toString());
        IJ.showStatus("checking image type ....");
        // open file with file path, pre-condition: the input image is 16-bit
        ImgPlus<?> genericImgPlus = img.getImgPlus();
        setNewProgressValue((int)(2*(100/totalNumberOfSteps))); // update ProgressBar
        // Verify the type
        if (!(genericImgPlus.firstElement() instanceof UnsignedShortType)) {
            throw new IllegalArgumentException("Expected ShortType(16-bit) image");
        }
        setNewProgressValue((int)(3*(100/totalNumberOfSteps))); // update ProgressBar
        ImgPlus<UnsignedShortType> imgPlus = (ImgPlus<UnsignedShortType>) genericImgPlus;

        // split the channels
        int channelDim = imgPlus.dimensionIndex(Axes.CHANNEL);
        boolean hasChannels = (channelDim >= 0);
        long numChannels = hasChannels ? imgPlus.dimension(channelDim) : 1;
        setNewProgressValue((int)(4*(100/totalNumberOfSteps))); // update ProgressBar
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
        setNewProgressValue((int)(5*(100/totalNumberOfSteps))); // update ProgressBar
        // get the spatial dimension size
        long width = imgPlus.dimension(xDim);
        long height = imgPlus.dimension(yDim);
        long depth = imgPlus.dimension(zDim);
        setNewProgressValue((int)(6*(100/totalNumberOfSteps))); // update ProgressBar
        IJ.showStatus("Processing image: "+filePath.getFileName().toString());
        // Go through each channel and process
        setNewProgressValue((int)(7*(100/totalNumberOfSteps))); // update ProgressBar
        for (int c = 0; c < numChannels; c++) {
            RandomAccessibleInterval<FloatType> channelImg = hasChannels
                    ? ops.convert().float32(Views.hyperSlice(imgPlus, channelDim, c))
                    : ops.convert().float32(imgPlus);

            if (debugMode){
                // Get after-split-dimensions
                long[] dims = new long[channelImg.numDimensions()];
                channelImg.dimensions(dims);
                IJ.log("dimension after split " +" "+ dims[0]+" " + dims[1]+" " + dims[2]);
            }
            // Get pixel size in microns unit (spacing)
            double pixelSizeX = imgPlus.averageScale(xDim);
            double pixelSizeY = imgPlus.averageScale(yDim);
            double pixelSizeZ = imgPlus.averageScale(zDim);
            if (debugMode) IJ.log("pixel size" +" "+ pixelSizeX+" " + pixelSizeY+" " + pixelSizeZ);
            // targeted pixel size
            double targetedSpacingX = targetXYpixelSize;
            double targetedSpacingY = targetXYpixelSize;
            double targetedSpacingZ = targetZpixelSize;
            if (debugMode) IJ.log("targeted Spacing: " +" "+ targetedSpacingX +" "+ targetedSpacingY +" "+ targetedSpacingZ);
            // Calculate the scaling factor for each dimension
            double finalPixelsX = ((double)width * pixelSizeX * 1000) / targetedSpacingX;
            double finalPixelsY = ((double)height * pixelSizeY * 1000) / targetedSpacingY;
            double finalPixelsZ = ((double)depth * pixelSizeZ * 1000) / targetedSpacingZ;
            if (debugMode) IJ.log("final Pixel size: " +" "+ finalPixelsX +" "+ finalPixelsY +" "+ finalPixelsZ);
            // convert number of final pixels to long
            long finalPixelsXlong = (long) finalPixelsX;
            long finalPixelsYlong = (long) finalPixelsY;
            long finalPixelsZlong = (long) finalPixelsZ;
            //Convert scaling factor to long type
            double scalingFactorX = finalPixelsX / width;
            double scalingFactorY = finalPixelsY / height;
            double scalingFactorZ = finalPixelsZ / depth;
            if (debugMode)
                IJ.log("Read and set dimension and scaling factor " + " " + scalingFactorX + " " + scalingFactorY + " " + scalingFactorZ);
            // Extend the Image and Set up Interpolation
            RealRandomAccessible<FloatType> interpolated = Views.interpolate(
                    Views.extendZero(channelImg),
                    new NLinearInterpolatorFactory<>());
            // Create a Scaling Transform
            AffineTransform3D transform = new AffineTransform3D();
            transform.scale(scalingFactorX, scalingFactorY, scalingFactorZ);
            if (debugMode) IJ.log("scaling transform completed");
            // Apply the Scaling to the Image
            RealRandomAccessible<FloatType> scaled = RealViews.affine(interpolated, transform);
            // Rasterize (Sample) Back to a Discrete Grid
            IntervalView<FloatType> resampled = Views.interval(
                    Views.raster(scaled),
                    new FinalInterval(finalPixelsXlong, finalPixelsYlong, finalPixelsZlong)
            );
            if (debugMode) IJ.log("resize completed");
            // check if width is greater than height, switch
            RandomAccessibleInterval<FloatType> resampledPermuted;
            if(width > height){
                RandomAccessibleInterval<FloatType> swapped = Views.permute(resampled, 0, 1);
                RandomAccessibleInterval<FloatType> inverted = Views.invertAxis(swapped,1);
                resampledPermuted = Views.permute(inverted, 1, 2);
            } else {
                // Then swap axes 0 and 2 (X and Z)
                resampledPermuted = Views.permute(resampled, 1, 2);
            }

            if (debugMode) IJ.log("swap axes finished");
            // convert back to UnsignShortType
            RandomAccessibleInterval<UnsignedShortType> unint16ResampledPermuted = ops.convert().uint16(resampledPermuted);
            // add the post process image to array
            processedChannels.add(unint16ResampledPermuted);
            if (debugMode) IJ.log("Processed channel: " + c);
        }
        // show result
//        for(RandomAccessibleInterval<UnsignedShortType> result : processedChannels){
//            ImageJFunctions.show(result);
//        }
//        IJ.showStatus("saving ouput of: "+filePath.getFileName().toString());
        // Generate new output file path
//        String parentDir = filePath.getParent().toString();
//        String fileNameWithoutExtension = filePath.getFileName().toString().replaceFirst("[.][^.]+$", "");
//        String outputString = parentDir + File.separator + fileNameWithoutExtension + "_sideview.tif";
//        Path newOutputFilePath = Paths.get(outputString);
        // the 2 channels
        RandomAccessibleInterval<UnsignedShortType> rai1 = processedChannels.get(0);
        RandomAccessibleInterval<UnsignedShortType> rai2 = processedChannels.get(1);
        setNewProgressValue((int)(8*(100/totalNumberOfSteps))); // update ProgressBar
        // create new imgplus for output
        ImgPlus<UnsignedShortType> outputImgPlus = createImgPlusFrom3DChannels(rai1, rai2);
        setNewProgressValue((int)(9*(100/totalNumberOfSteps))); // update ProgressBar
        // add the channels to empty
        copyToChannel(rai1,outputImgPlus,0);
        copyToChannel(rai2,outputImgPlus,1);

//        // save the new Image to tif file
//        ImgSaver saver = new ImgSaver(context);
//        saver.saveImg(newOutputFilePath.toString(), outputImgPlus);
//        IJ.showStatus("Finish file: "+filePath.getFileName().toString());
//        if (debugMode) IJ.log("Saved side view image: " + newOutputFilePath);
        setNewProgressValue((int)(10*(100/totalNumberOfSteps))); // update ProgressBar
        return outputImgPlus;
    }

    private static ImgPlus<UnsignedShortType>
    createImgPlusFrom3DChannels (RandomAccessibleInterval<UnsignedShortType> channel1,
                                 RandomAccessibleInterval<UnsignedShortType> channel2){
        // Assign axis types
        AxisType[] axisTypes = new AxisType[] {
                Axes.X, Axes.Y,  Axes.CHANNEL, Axes.Z
        };
        long x = channel1.dimension(0);
        long y = channel1.dimension(1);
        long z = channel1.dimension(2);
        long c = 2;
        long[] dims = new long[] { x, y, c, z};
        Img<UnsignedShortType> img = new ArrayImgFactory<>(new UnsignedShortType()).create(dims);
        ImgPlus<UnsignedShortType> imgPlus = new ImgPlus<>(img);
        for (int i = 0; i < axisTypes.length; i++) {
            imgPlus.setAxis(new DefaultLinearAxis(axisTypes[i]), i);
        }
        return imgPlus;
    }

    public static void copyToChannel(RandomAccessibleInterval<UnsignedShortType> source, ImgPlus<UnsignedShortType> imgPlus, int channelPosition) {
        int channelDim = imgPlus.dimensionIndex(Axes.CHANNEL);
        // Get a view of just the desired channel
        RandomAccessibleInterval<UnsignedShortType> desiredChannel =
                Views.hyperSlice(imgPlus, channelDim, channelPosition);
        // Calculate min/max from source
        double[] minMax = getMinMax(source); // Use the min/max calculation method from earlier
        // Use cursors for efficient iteration
        Cursor<UnsignedShortType> srcCursor = Views.flatIterable(source).cursor();
        Cursor<UnsignedShortType> destCursor = Views.flatIterable(desiredChannel).cursor();
        // loop and copy data
        while (srcCursor.hasNext()) {
            destCursor.next().set(srcCursor.next());
        }
        // Update display range for channel
        imgPlus.setChannelMinimum(channelPosition, minMax[0]);
        imgPlus.setChannelMaximum(channelPosition, minMax[1]);
    }


    // Helper method to get min/max values
    private static double[] getMinMax(RandomAccessibleInterval<UnsignedShortType> image) {
        double min = Double.POSITIVE_INFINITY;
        double max = Double.NEGATIVE_INFINITY;

        for (UnsignedShortType pixel : Views.flatIterable(image)) {
            int val = pixel.get();
            min = Math.min(min, val);
            max = Math.max(max, val);
        }

        return new double[]{min, max};
    }

}