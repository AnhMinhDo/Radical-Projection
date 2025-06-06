package schneiderlab.tools.radicalprojection.imageprocessor.core.segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import inra.ijpb.binary.distmap.ChamferDistanceTransform2DFloat;
import inra.ijpb.binary.distmap.ChamferMask2D;
import inra.ijpb.measure.region2d.Centroid;
import inra.ijpb.morphology.MinimaAndMaxima;
import inra.ijpb.watershed.ExtendedMinimaWatershed;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import static ij.IJ.debugMode;

public class Reconstruction {
private ArrayList<Point> coordinatesOutside;
private final int width;
private final int height;
private final int diameter;
private final RandomAccessibleInterval<FloatType> smoothedStack;
private Point pointForBackground;

    public Reconstruction(DataDuringSegmentationProcess ddsp, ArrayList<Point> coordinates){
        this.smoothedStack=ddsp.getSmoothStack();
        this.width=ddsp.getWidth();
        this.height=ddsp.getHeight();
        this.diameter=ddsp.getDiameter();
        this.coordinatesOutside=coordinates;
        pointForBackground = new Point((int)width-1, (int)height-1);
    }

    public Overlay process1Slide(){
//        // create a mask based on given coordinates
//        pointForBackground = new Point((int)width-1, (int)height-1);
        // add all points to List
        coordinatesOutside.add(pointForBackground);
//        if(debugMode){ System.err.println(coordinatesOutside);}
        // Create innit mask
        CreateMask createMask = new CreateMask(coordinatesOutside,(int)width,(int)height,diameter);
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
        // Threshold: Keep pixels where distance <= diameter
        ImageProcessor grownRegionProcessor = markerDistanceTransformed.duplicate();
        float[] markerDistanceTransformedFloatArray = (float[]) markerDistanceTransformed.getPixels();
        float[] grownRegionProcessorFloatArray = (float[]) grownRegionProcessor.getPixels();
        for (int p = 0; p < grownRegionProcessorFloatArray.length; p++) {
            grownRegionProcessorFloatArray[p] = (markerDistanceTransformedFloatArray[p]<=diameter) ? 255 : 0; // any point outside the range is marked as 0
        }
        // write the growRegion to imageplus
        ImagePlus growRegion = new ImagePlus("grown Region", grownRegionProcessor);
//        if(debugMode){growRegion.show();}
        // extract 1 slice and convert to Imagej1 Format
        RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, 0); // dimension 2 is Z
        ImagePlus imageForReconstruction = ImageJFunctions.wrapFloat(slice2D, "mask for reconstruction");
        // Make sure the display range is set properly for float images
        imageForReconstruction.resetDisplayRange();
        // impose minima on growRegion image
        ImageProcessor reconstructedProcessor = MinimaAndMaxima.imposeMinima(imageForReconstruction.getProcessor(),
                growRegion.getProcessor(),8);
        reconstructedProcessor.convertToByte(true);
        ImagePlus reconstructedImagePlus = new ImagePlus("reconstructed Image", reconstructedProcessor);
//        if(debugMode){reconstructedImagePlus.show();}
        // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
        ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                reconstructedImagePlus, 255,8
        );
        segmentedImage.show();
        // Overlay the segmentation on to the original image
        ImageProcessor segmentedImageProcessor = segmentedImage.getProcessor();
        segmentedImageProcessor.setThreshold(1,4,ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection ts = new ThresholdToSelection();
        Roi maskRoi = ts.convert(segmentedImageProcessor);
        // Apply overlay to original image
        Overlay overlay = new Overlay(maskRoi);
        overlay.setStrokeColor(Color.RED);
//        imageForReconstruction.setOverlay(overlay);
//        imageForReconstruction.updateAndDraw();
//        imageForReconstruction.show();
        return overlay;

//        // number of centroid based on the user number of input click,
//        // need to remove the first one which corresponding to the background
//        int numberOfCentroids = coordinatesOutside.size()-1;
//        // generate the label array starting from 2 to (2+numberOfCentroid-1)
//        int[] labels = IntStream.range(2,2+numberOfCentroids).toArray();
////        if(debugMode){IJ.log(Arrays.toString(labels));}
//        // calculate the centroid of the newly segmented
//        double[][] centroids = Centroid.centroids(segmentedImage.getProcessor(),labels);
////        if(debugMode){IJ.log(Arrays.deepToString(centroids));}
//        // remove the old coordinates
//        coordinatesOutside.clear();
//        // round the centroid values and convert to int
//        int[][] centroidsInt = new int[centroids.length][centroids[0].length];
//        for (int i = 0; i < centroids.length; i++) {
//            for (int j = 0; j < centroids[i].length; j++) {
//                centroidsInt[i][j] = (int) Math.round(centroids[i][j]);
//            }
//        }
//        System.err.println("new centroids: " + Arrays.deepToString(centroidsInt));
//        // Create new Points from the centroid
//        for (int i = 0; i < centroidsInt.length; i++) {
//            coordinatesOutside.add(new Point(centroidsInt[i][0],centroidsInt[i][1]));
//        }
//        System.err.println("update coordinate object: " + coordinatesOutside);
    }

    public void processWholeStack(){
        ImageStack finalStack = new ImageStack(width, height);
        System.err.println("start the process");
        for (int currentSlice = 0; currentSlice < smoothedStack.dimension(2); currentSlice++) {
            // add background point
            coordinatesOutside.add(pointForBackground);
            // Create innit mask
            CreateMask createMask = new CreateMask(coordinatesOutside,(int)width,(int)height,diameter);
            ImagePlus marker = createMask.drawMaskWithCoordinate();
            // invert marker
            ImagePlus markerInverted = marker.duplicate();
            markerInverted.getProcessor().invert();
            // Compute Distance Transform using Chamfer method
            ChamferDistanceTransform2DFloat cdtf = new ChamferDistanceTransform2DFloat(ChamferMask2D.BORGEFORS);
            FloatProcessor markerDistanceTransformed = cdtf.distanceMap(markerInverted.getProcessor());
            ImagePlus markerDistanceTransformedImagePlus = new ImagePlus("markerDistanceTransformed", markerDistanceTransformed);
            if(debugMode){markerDistanceTransformedImagePlus.show();}
            // Threshold: Keep pixels where distance <= diameter
            ImageProcessor grownRegionProcessor = markerDistanceTransformed.duplicate();
            float[] markerDistanceTransformedFloatArray = (float[]) markerDistanceTransformed.getPixels();
            float[] grownRegionProcessorFloatArray = (float[]) grownRegionProcessor.getPixels();
            for (int p = 0; p < grownRegionProcessorFloatArray.length; p++) {
                grownRegionProcessorFloatArray[p] = (markerDistanceTransformedFloatArray[p]<=diameter) ? 255 : 0; // any point outside the range is marked as 0
            }
            // write the growRegion to imageplus
            ImagePlus growRegion = new ImagePlus("grown Region", grownRegionProcessor);
            if(debugMode){growRegion.show();}
            // extract 1 slice and convert to Imagej1 Format
            RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, currentSlice); // dimension 2 = Z
            ImagePlus imageForReconstruction = ImageJFunctions.wrapFloat(slice2D, "mask for reconstruction");
            // Make sure the display range is set properly for float images
            imageForReconstruction.resetDisplayRange();
            // impose minima on growRegion image
            ImageProcessor reconstructedProcessor = MinimaAndMaxima.imposeMinima(imageForReconstruction.getProcessor(),
                    growRegion.getProcessor(),8);
            reconstructedProcessor.convertToByte(true);
            ImagePlus reconstructedImagePlus = new ImagePlus("reconstructed Image", reconstructedProcessor);
            if(debugMode){reconstructedImagePlus.show();}
            // apply marker-based watershed using the labeled minima on the minima-imposed gradient image
            ImagePlus segmentedImage = ExtendedMinimaWatershed.extendedMinimaWatershed(
                    reconstructedImagePlus, 255,8
            );
            // number of centroid based on the user number of input click,
            // need to remove the first one which corresponding to the background
            int numberOfCentroids = coordinatesOutside.size()-1;
            // generate the label array [2,2+numberOfCentroid)
            int[] labels = IntStream.range(2,2+numberOfCentroids).toArray();
            if(debugMode){IJ.log(Arrays.toString(labels));}
            // calculate the centroid of the newly segmented
            double[][] centroids = Centroid.centroids(segmentedImage.getProcessor(),labels);
            if(debugMode){IJ.log(Arrays.deepToString(centroids));}
            // round the centroid values and convert to int
            int[][] centroidsInt = new int[centroids.length][centroids[0].length];
            for (int row = 0; row < centroids.length; row++) {
                for (int col = 0; col < centroids[row].length; col++) {
                    centroidsInt[row][col] = (int) Math.round(centroids[row][col]);
                }
            }
            // remove the old coordinates
            coordinatesOutside.clear();
            // Create new Points from the centroid
            for (int row = 0; row < centroidsInt.length; row++) {
                coordinatesOutside.add(new Point(centroidsInt[row][0],centroidsInt[row][1]));
            }
            // add the image to the stack
            finalStack.addSlice(segmentedImage.getProcessor());
        }
        ImagePlus finalImagePlus = new ImagePlus("Final Stack", finalStack);
        finalImagePlus.show();
    }
}
