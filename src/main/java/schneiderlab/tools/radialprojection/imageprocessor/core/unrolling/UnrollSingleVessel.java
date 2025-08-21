package schneiderlab.tools.radialprojection.imageprocessor.core.unrolling;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.util.ArrayList;

public class UnrollSingleVessel {
    private final ImagePlus hybridStack;
    private final ImageStack hybridStackImageStack;
    private final ImagePlus binaryMaskEdge;
    private final ImageStack binaryMaskEdgeImageStack;
    private final ArrayList<Point> centroidList;
    private final int angleStep;
    private final int angleCount;
    private final int maxRadius;
    private final int outputHeight;
    private final int outputWidth;
    private ShortProcessor hybridWallProcessor;

    public UnrollSingleVessel(ImagePlus hybridStack,
                           ImagePlus binaryMaskEdge,
                           ArrayList<Point> centroidList1Vessel,
                           int angleStep) {
        this.hybridStack = hybridStack;
        this.hybridStackImageStack = hybridStack.getImageStack();
        this.binaryMaskEdge = binaryMaskEdge;
        this.binaryMaskEdgeImageStack = binaryMaskEdge.getImageStack();
        this.angleStep = angleStep;
        this.angleCount = (int) Math.ceil(360.0 / angleStep); // 360 degree of a circle
        this.centroidList = centroidList1Vessel;
        this.outputHeight = (int)Math.ceil((double)Math.min(binaryMaskEdge.getHeight(), binaryMaskEdge.getWidth())*3.14); // the vessel must be enclosed inside the image of each slice, which means its diameter cannot be larger than the height(or width depend on which one is smaller) of that image, therefore, the circumference of the vessel cannot excess image_height*3.14
        this.outputWidth = hybridStackImageStack.size();
        this.maxRadius = (int)Math.min(binaryMaskEdge.getHeight(),binaryMaskEdge.getWidth())/4;
    }

    // process function here
    public ImagePlus process(){
        performUnrolling();
        return new ImagePlus("Unrolled Vessel", hybridWallProcessor);
    }

    private void performUnrolling(){
        // create 2D array to store intermediate result
        short[][] intermediateResult = new short[outputHeight][outputWidth];
        // perform main process
        for (int i = 1; i < binaryMaskEdgeImageStack.getSize()+1; i++) { // for each slice in the binary mask stack
            int cx = centroidList.get(i-1).x; // get the x value of centroid at slice i
            int cy = centroidList.get(i-1).y; // get the y value of centroid at slice i
//            System.err.println("before unrolling1Vessel1Slide");
            unrolling1Vessel1Slide(binaryMaskEdgeImageStack.getProcessor(i),
                    cx,cy,
                    maxRadius,
                    intermediateResult,
                    i-1,
                    hybridStackImageStack.getProcessor(i),
                    angleStep,
                    angleCount);
//            System.err.println("after unrolling1Vessel1Slide");
        }
        // find the last row with signal, anything below stores only zero value and should be discarded
        int lastRowWithSignalIndex = findLastRowContainingSignal(intermediateResult);
        // create output processor
        this.hybridWallProcessor = new ShortProcessor(outputWidth,lastRowWithSignalIndex+1);
        // write the intensity to the outputImageProcessor
        short[] resultShortProcessorArray = (short[])hybridWallProcessor.getPixels();
        for (int rowIdx = 0; rowIdx <= lastRowWithSignalIndex; rowIdx++) {
            for (int colIdx = 0; colIdx < outputWidth; colIdx++) {
                resultShortProcessorArray[rowIdx*outputWidth+colIdx] = intermediateResult[rowIdx][colIdx];
            }
        }

    }

    public static void unrolling1Vessel1Slide(ImageProcessor binaryMask,
                                                   int cx, int cy,
                                                   int maxRadius,
                                                   short[][] output2DArray,
                                                   int currentSliceIdx,
                                                   ImageProcessor originalProcessor,
                                                   int angleStep,
                                                   int angleCount) {
        Point previousIntersection= new Point(cx,cy);
        int previousIntersectionIdx = 0;
        Point firstIntersection;
        for (int i = 0; i < angleCount; i ++) { // for each angle
            if(i == 0){ // for the first angle,
                double rad = Math.toRadians(0); // get the value in radian of the current angle, the first angle is 0 degree
                double x2 = cx + maxRadius * Math.cos(rad);
                double y2 = cy - maxRadius * Math.sin(rad);
                x2 = Math.max(0,Math.min(x2, binaryMask.getWidth()-1));
                y2 = Math.max(0,Math.min(y2, binaryMask.getHeight()-1));
                Line line = new Line(cx, cy, x2, y2);
                // Get profile (pixel values along line)
                ImagePlus binaryMaskWithROI = new ImagePlus("binary mask with ROI", binaryMask);
                binaryMaskWithROI.setRoi(line);
                ProfilePlot profile = new ProfilePlot(binaryMaskWithROI);
                double[] values = profile.getProfile();
                for (int j = 0; j < values.length; j++) { // traverse each of the pixel on the line
                    if(values[j] > 0){ // find the signal of the mask
                        output2DArray[0][currentSliceIdx] = selectBestSignal(cx,cy,rad,j,originalProcessor);
                        previousIntersection = new Point((int)(cx + (j+1) * Math.cos(rad)),(int)(cy - (j+1) * Math.sin(rad)));
                        firstIntersection = new Point((int)(cx + (j+1) * Math.cos(rad)),(int)(cy - (j+1) * Math.sin(rad)));
                        break;
                    }
                }
            } else {
                double rad = Math.toRadians(i*angleStep); // get the value in radian of the current angle
                double x2 = cx + maxRadius * Math.cos(rad);
                double y2 = cy - maxRadius * Math.sin(rad);
                x2 = Math.max(0,Math.min(x2, binaryMask.getWidth()-1));
                y2 = Math.max(0,Math.min(y2, binaryMask.getHeight()-1));
                Line line = new Line(cx, cy, x2, y2);
                // Get profile (pixel values along line)
                ImagePlus binaryMaskWithROI = new ImagePlus("binary mask with ROI", binaryMask);
                binaryMaskWithROI.setRoi(line);
                ProfilePlot profile = new ProfilePlot(binaryMaskWithROI);
                double[] values = profile.getProfile();
                for (int j = 0; j < values.length; j++) { // traverse each of the pixel on the line
                    if(values[j] > 0){ // find the signal of the mask
//                        System.err.println("found intersection");
                        // get the pixel intensity at the intersection
                        short pixelIntensityAtIntersection = selectBestSignal(cx,cy,rad,j,originalProcessor);
                        // calculate intersect point
                        Point intersectPoint = new Point((int)(cx + (j+1) * Math.cos(rad)),(int)(cy - (j+1) * Math.sin(rad)));
//                        System.err.println("Intersect point: " + intersectPoint);
                        // get the Line from previous point to current intersect point
                        Line connectLine = new Line(previousIntersection.getX(),
                                previousIntersection.getY(),
                                intersectPoint.getX(),
                                intersectPoint.getY());
//                        System.err.println("Draw line from previous point to current point");
                        Point[] allPointsArray =connectLine.getContainedPoints();
//                        System.err.println("get allPointsArray with length: " + allPointsArray.length);
                        for (int k = 1; k < allPointsArray.length; k++) { // start from 1 to skip the first point(this point intensity has been assigned to output from the previous loop)
                            Point checkedPoint = allPointsArray[k];
                            float intensity = originalProcessor.getf(checkedPoint.x,checkedPoint.y);
                            output2DArray[previousIntersectionIdx+k][currentSliceIdx] = (short)intensity;
                        }
//                        System.err.println("complete for loop to assign points to the outputArray");
                        output2DArray[previousIntersectionIdx+allPointsArray.length][currentSliceIdx] = pixelIntensityAtIntersection;
                        previousIntersection = intersectPoint;
                        previousIntersectionIdx = previousIntersectionIdx + allPointsArray.length;
                        break;
                    }
                }
            }
        }
    }

    private static short selectBestSignal (int cx, int cy, double rad, int currentIndex, ImageProcessor imageProcessor){
        //point at interception
        int px = (int)(cx + (currentIndex+1) * Math.cos(rad));
        int py = (int)(cy - (currentIndex+1) * Math.sin(rad));
        //point before interception
        int px_b = (int)(cx + (currentIndex) * Math.cos(rad));
        int py_b = (int)(cy - (currentIndex) * Math.sin(rad));
        //point after interception
        int px_a = (int)(cx + (currentIndex+2) * Math.cos(rad));
        int py_a = (int)(cy - (currentIndex+2) * Math.sin(rad));
        return (short)Math.max(imageProcessor.getf(px,py),
                Math.max(imageProcessor.getf(px_b,py_b),imageProcessor.getf(px_a,py_a))); // select the highest signal out of the 3 points to ensure the best projection quality
    }

    private static int findLastRowContainingSignal(short[][] array2D){
        int newRowCount = array2D.length; // initially, all rows are considered containing signal
        for (int i = array2D.length-1; i >= 0; i--) { // iterate each row from bottom to top
            boolean allZero = true;
            for (int j = 0; j < array2D[0].length; j++) { // iterate each column in row i
                if(array2D[i][j] != 0){ // found row with signal
                    allZero = false;
                    break; // stop the inner loop
                }
            }
            if(allZero){ // no signal is found for this row
                newRowCount--; // update the result variable, the row with signal must be above
            } else {
                break; // stop the loop when the row signal is found
            }
        }
        return newRowCount;
    }

}

