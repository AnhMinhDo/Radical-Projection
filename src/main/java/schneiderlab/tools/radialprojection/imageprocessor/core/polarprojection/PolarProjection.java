package schneiderlab.tools.radialprojection.imageprocessor.core.polarprojection;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.util.ArrayList;

public class PolarProjection {
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
    private final ShortProcessor hybridWallProcessor;

    public PolarProjection(ImagePlus hybridStack,
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
        this.outputHeight = angleCount;
        this.outputWidth = hybridStackImageStack.size();
        this.maxRadius = (int)Math.min(binaryMaskEdge.getHeight(),binaryMaskEdge.getWidth())/4;
        this.hybridWallProcessor = new ShortProcessor(outputWidth,outputHeight);
    }

    // process function here
    public ImagePlus process(){
        performPolarProjection();
        return new ImagePlus("polar projected Vessel", hybridWallProcessor);
    }

    private void performPolarProjection(){
        // create 2D array to store intermediate result
        short[][] intermediateResult = new short[outputHeight][outputWidth];
        // perform main process
        for (int i = 1; i < binaryMaskEdgeImageStack.getSize()+1; i++) { // for each slice in the binary mask stack
            int cx = centroidList.get(i-1).x; // get the x value of centroid at slice i
            int cy = centroidList.get(i-1).y; // get the y value of centroid at slice i
            findRadialEdgeIntersections(binaryMaskEdgeImageStack.getProcessor(i),
                    cx,cy,
                    maxRadius,
                    intermediateResult,
                    i-1,
                    hybridStackImageStack.getProcessor(i),
                    angleStep,
                    angleCount);
        }
        // write the intensity to the outputImageProcessor
        short[] resultShortProcessorArray = (short[])hybridWallProcessor.getPixels();
        for (int rowIdx = 0; rowIdx < angleCount; rowIdx++) {
            for (int colIdx = 0; colIdx < outputWidth; colIdx++) {
                resultShortProcessorArray[rowIdx*outputWidth+colIdx] = intermediateResult[rowIdx][colIdx];
            }
        }

    }
    public static void findRadialEdgeIntersections(ImageProcessor binaryMask,
                                                   int cx, int cy,
                                                   int maxRadius,
                                                   short[][] output2DArray,
                                                   int currentSliceIdx,
                                                   ImageProcessor originalProcessor,
                                                   int angleStep,
                                                   int angleCount) {
        for (int i = 0; i < angleCount; i ++) {
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
                    output2DArray[i][currentSliceIdx] = selectBestSignal(cx,cy,rad,j,originalProcessor);
                    break;
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

}
