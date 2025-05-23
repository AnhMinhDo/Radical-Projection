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
    private RandomAccessibleInterval<FloatType> smoothStack;
private ArrayList<Point> coordinates;
private int width;
private int height;
private int diameter;

private RandomAccessibleInterval<FloatType> smoothedStack;

    public Reconstruction(DataDuringSegmentationProcess ddsp, ArrayList<Point> coordinates){
        this.smoothStack=ddsp.getSmoothStack();
        this.width=ddsp.getWidth();
        this.height=ddsp.getHeight();
        this.diameter=ddsp.getDiameter();
        this.coordinates=coordinates;
    }

    public void process(){
        // create a mask based on given coordinates
        Point point1 = new Point((int)114, (int)68);
        Point point2 = new Point((int)177, (int)32);
        Point pointForBackground = new Point((int)width-1, (int)height-1);
        // add all points to List
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
        //TODO: AnhMinh: do not hard code the position for the command below, should change to 1st image in the stack
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
        // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
        ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                reconstructedImagePlus, 255,8
        );
        segmentedImage.show();
    }
}
