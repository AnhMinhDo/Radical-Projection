package schneiderlab.tools.radialprojection.imageprocessor.core.segmentation;

import ij.ImagePlus;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;

import java.awt.*;
import java.util.ArrayList;

import static schneiderlab.tools.radialprojection.imageprocessor.core.utils.RadicalProjectionUtils.deepCopyPoints;

public class SegmentationExtendMinimaWaterShed {
    private RandomAccessibleInterval<FloatType> inputSlice;
    private ArrayList<Point> clickCoordinate;
    private int width;
    private int height;
    private Point pointForBackground;
    private int radius;
    private int pixelScaleInNanometer;
    private ImagePlus inputSliceImagePlus;

    public SegmentationExtendMinimaWaterShed( ArrayList<Point> clickCoordinate,
                                              RandomAccessibleInterval<FloatType> inputSlice,
                                              int width,
                                              int height,
                                              int radius,
                                              int pixelScaleInNanometer) {
        this.inputSlice = inputSlice;
        this.clickCoordinate = clickCoordinate;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.pointForBackground = new Point((int) width - 1, (int) height - 1);
        this.pixelScaleInNanometer = pixelScaleInNanometer;
    }

    public ImagePlus getInputSliceImagePlus() {
        return inputSliceImagePlus;
    }

    public ImagePlus performSegmentation (){
        // add Point for background
        ArrayList<Point> clickPoints = deepCopyPoints(clickCoordinate);
        clickPoints.add(pointForBackground);
//        if(debugMode){ System.err.println(coordinatesOutside);}
        // Create innit mask
        CreateMask createMask = new CreateMask(clickPoints, (int) width, (int) height, radius);
        ImagePlus marker = createMask.drawMaskWithCoordinate();
//        if(debugMode){marker.show();}
        // invert marker
        ImagePlus markerInverted = marker.duplicate();
        markerInverted.getProcessor().invert();
        // Compute Distance Transform using Chamfer method
        ChamferDistanceTransform2DFloat cdtf = new ChamferDistanceTransform2DFloat(ChamferMask2D.BORGEFORS);
        FloatProcessor markerDistanceTransformed = cdtf.distanceMap(markerInverted.getProcessor());
        ImagePlus markerDistanceTransformedImagePlus = new ImagePlus("markerDistanceTransformed", markerDistanceTransformed);
//        if(debugMode){markerDistanceTransformedImagePlus.show();}
        // Threshold: Keep pixels where distance <= radius
        ImageProcessor grownRegionProcessor = markerDistanceTransformed.duplicate();
        float[] markerDistanceTransformedFloatArray = (float[]) markerDistanceTransformed.getPixels();
        float[] grownRegionProcessorFloatArray = (float[]) grownRegionProcessor.getPixels();
        for (int p = 0; p < grownRegionProcessorFloatArray.length; p++) {
            grownRegionProcessorFloatArray[p] = (markerDistanceTransformedFloatArray[p]*pixelScaleInNanometer*0.001 <= radius*1f) ? 255 : 0; // Multiply by 0,001 to convert from nm to µm,any point outside the range is marked as 0
        }
        // write the growRegion to imageplus
        ImagePlus growRegion = new ImagePlus("grown Region", grownRegionProcessor);
//        if(debugMode){growRegion.show();}
        // convert to Imagej1 Format
        ImagePlus imageForReconstruction = ImageJFunctions.wrapFloat(inputSlice, "original Image");
        inputSliceImagePlus = imageForReconstruction;
//        // Make sure the display range is set properly for float images
//        imageForReconstruction.resetDisplayRange();
        // impose minima on growRegion image
        ImageProcessor reconstructedProcessor = MinimaAndMaxima.imposeMinima(imageForReconstruction.getProcessor(),
                growRegion.getProcessor(), 8);
        reconstructedProcessor.convertToByte(true);
        ImagePlus reconstructedImagePlus = new ImagePlus("reconstructed Image", reconstructedProcessor);
//        if(debugMode){imageForReconstruction.resetDisplayRange(); reconstructedImagePlus.show();}
        // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
        ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                reconstructedImagePlus, 255, 8
        );
        return segmentedImage;
    }
}
