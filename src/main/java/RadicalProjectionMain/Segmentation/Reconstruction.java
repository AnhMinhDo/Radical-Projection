package RadicalProjectionMain.Segmentation;

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
import net.imglib2.view.Views;

import java.awt.*;
import java.util.ArrayList;

public class Reconstruction {
private ArrayList<Point> coordinatesOutside;
private final int width;
private final int height;
private final int diameter;
private final RandomAccessibleInterval<FloatType> smoothedStack;

    public Reconstruction(DataDuringSegmentationProcess ddsp, ArrayList<Point> coordinates){
        this.smoothedStack=ddsp.getSmoothStack();
        this.width=ddsp.getWidth();
        this.height=ddsp.getHeight();
        this.diameter=ddsp.getDiameter();
        this.coordinatesOutside=coordinates;
    }

    public void process(){
        // create a mask based on given coordinates
        Point pointForBackground = new Point((int)width-1, (int)height-1);
        // add all points to List
        coordinatesOutside.add(pointForBackground);
        System.err.println(coordinatesOutside);
        // Create innit mask
        CreateMask createMask = new CreateMask(coordinatesOutside,(int)width,(int)height,diameter);
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
        RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, 1); // dimension 2 = Z
        ImagePlus imageForReconstruction = ImageJFunctions.wrapFloat(slice2D, "mask for reconstruction");
        // Make sure the display range is set properly for float images
        imageForReconstruction.resetDisplayRange();
        // impose minima on growRegion image
        ImageProcessor reconstructedProcessor = MinimaAndMaxima.imposeMinima(imageForReconstruction.getProcessor(),
                growRegion.getProcessor(),8);
        reconstructedProcessor.convertToByte(true);
        ImagePlus reconstructedImagePlus = new ImagePlus("reconstructed Image", reconstructedProcessor);
        reconstructedImagePlus.show();
        // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
        ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                reconstructedImagePlus, 255,8
        );
        segmentedImage.show();
    }
}
