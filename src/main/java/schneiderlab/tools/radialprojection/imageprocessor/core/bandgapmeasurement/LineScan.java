package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import ij.gui.Line;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LineScan {
    private List<Band> bandList;
    private final Point leftEdge;
    private final Point rightEdge;
    private final double[] pixelArray;
    private byte[] bandMask;

    public LineScan(Point leftEdge, Point rightEdge, double[] pixelArray) {
        this.leftEdge = leftEdge;
        this.rightEdge = rightEdge;
        this.pixelArray = pixelArray;
    }

    public List<Band> getBandList() {
        return bandList;
    }

    public Point getLeftEdge() {
        return leftEdge;
    }

    public Point getRightEdge() {
        return rightEdge;
    }

    public byte[] getBandMask() {
        return bandMask;
    }

    public void process(){
        // find Peak
        byte[] peakArray = findLocalMaxima(pixelArray);
        // find trough
        byte[]troughArray = findLocalMinima(pixelArray);
        // find Prominence
        double[] prominence = calculatePeakProminence(pixelArray,peakArray,troughArray);
        // find Band and add to bandList
        bandList = bandsDetection(pixelArray,
                peakArray,
                prominence,
                leftEdge.x,
                leftEdge.y);
    }

    private ArrayList<Band> bandsDetection(double[] pixelIntensityArray, byte[] peakMaskArray, double[] prominenceArray, int lineLeftEdgeX, int lineLeftEdgeY){
        ArrayList<Band> bandArrayList = new ArrayList<>();
        byte[] result = new byte[peakMaskArray.length]; // I just left it here in case it is used later
        Arrays.fill(result, (byte)0);
        // for each peak, use the prominence/2, go to left and any pixel with intensity greater than prominence/2, mark 255; do the same for right
        for (int i = 0; i < peakMaskArray.length; i++) {
            if ((peakMaskArray[i] & 0xFF) == 255) { // check if this is the peak
                result[i] = (byte)255;
                double currentProminence = prominenceArray[i];
                int baseline = (int)Math.round(currentProminence/2);
                int leftEdgeIndex= i-1;
                int rightEdgeIndex=i+1;
                for (int leftPointer = i-1; leftPointer >= 0; leftPointer--) { // walk to the left, from i-1 to 0
                    if(pixelIntensityArray[leftPointer] > baseline){
                        result[leftPointer] = (byte)255;
                        leftEdgeIndex = leftPointer;
                    } else {
                        break;
                    }
                }
                for (int rightPointer = i+1; rightPointer < peakMaskArray.length; rightPointer++) { // walk to the right, from i+1 to the end of array
                    if(pixelIntensityArray[rightPointer] > baseline){
                        result[rightPointer] = (byte)255;
                        rightEdgeIndex = rightPointer;
                    } else {
                        break;
                    }
                }
                // Note for below: the line scan is always horizontal so y position is the same
                Band band = new Band(new Point(lineLeftEdgeX+i,lineLeftEdgeY),
                        new Point(lineLeftEdgeX+leftEdgeIndex,lineLeftEdgeY),
                        new Point(lineLeftEdgeX+rightEdgeIndex,lineLeftEdgeY),
                        pixelIntensityArray[i],
                        rightEdgeIndex-leftEdgeIndex,
                        prominenceArray[i]);
                bandArrayList.add(band);
            }
        }
        return bandArrayList;
    }
    private static double[] calculatePeakProminence(double[] pixelIntensityArray, byte[] peakMaskArray, byte[] troughMaskArray) {
        // TODO: check the input to have the same size
        double[] result = new double[pixelIntensityArray.length];
        Arrays.fill(result, 0);
        // get the for every peak, step to the left to find the trough, or the left  edge(index 0), step to the right to find the trough, or the right edge (index = array.length-1)
        for (int i = 0; i < peakMaskArray.length; i++) {
            int markValue = peakMaskArray[i] & 0xFF;
            if (markValue == 255) { // this is the peak with index i
                int leftTroughIdx = 0;
                int rightTroughIdx = peakMaskArray.length - 1;
                for (int leftPointer = i -1; leftPointer >= 0; leftPointer--) { // walk to the left, from i-1 to 0
                    if ((troughMaskArray[leftPointer]&0xFF) == 255) { // check the first trough to the left
                        leftTroughIdx = leftPointer;
                        break;
                    }
                }
                for (int rightPointer = i +1; rightPointer < peakMaskArray.length; rightPointer++) { // walk to the right, from i+1 to the end of array
                    if ((troughMaskArray[rightPointer]&0xFF) == 255) { // check the first trough to the right
                        rightTroughIdx = rightPointer;
                        break;
                    }
                }
                // compare and select the trough with higher intensity value
                double selectedTroughValue = Math.max(pixelIntensityArray[leftTroughIdx],pixelIntensityArray[rightTroughIdx]);
                // calculate the prominence peakValue - troughValue
                result[i] = pixelIntensityArray[i] - selectedTroughValue;
            }
        }
        return result;
    }
    private static byte[] findLocalMaxima(double[] inputArray){
        byte[] result = new byte[inputArray.length];
        Arrays.fill(result, (byte)0);
        ArrayList<Integer> leftEdge = new ArrayList<>((int)inputArray.length/2);
        ArrayList<Integer> rightEdge = new ArrayList<>((int)inputArray.length/2);
        ArrayList<Integer> midPoint = new ArrayList<>((int)inputArray.length/2);
        int currentIndex = 1; // start from the second element
        int MaxIndex = inputArray.length-1; // limit of loopVariable
        while(currentIndex<MaxIndex){
            if(inputArray[currentIndex] > inputArray[currentIndex-1]){ // check if the previous element is smaller
                int iAhead = currentIndex + 1;
                while(iAhead < MaxIndex && inputArray[iAhead] == inputArray[currentIndex]){ // find the next element that is not equal to inputArray[currentIndex]
                    iAhead++;
                }
                if(inputArray[iAhead] < inputArray[currentIndex]){
                    // record the left edge, right edge, midpoint
                    leftEdge.add(currentIndex);
                    rightEdge.add(iAhead);
                    midPoint.add((currentIndex+(iAhead-1))/2);
                    currentIndex = iAhead;
                }
            }
            currentIndex++;
        }
        for (Integer index: midPoint){
            result[index] = (byte)255;
        }
        return result;
    }
    private static byte[] findLocalMinima(double[] inputArray){
        byte[] result = new byte[inputArray.length];
        Arrays.fill(result, (byte)0);
        ArrayList<Integer> leftEdge = new ArrayList<>((int)inputArray.length/2);
        ArrayList<Integer> rightEdge = new ArrayList<>((int)inputArray.length/2);
        ArrayList<Integer> midPoint = new ArrayList<>((int)inputArray.length/2);
        int currentIndex = 1; // start from the second element
        int MaxIndex = inputArray.length-1; // limit of loopVariable
        while(currentIndex<MaxIndex){
            if(inputArray[currentIndex] < inputArray[currentIndex-1]){ // check if the previous element is smaller
                int iAhead = currentIndex + 1;
                while(iAhead < MaxIndex && inputArray[iAhead] == inputArray[currentIndex]){ // find the next element that is not equal to inputArray[currentIndex]
                    iAhead++;
                }
                if(inputArray[iAhead] > inputArray[currentIndex]){
                    // record the left edge, right edge, midpoint
                    leftEdge.add(currentIndex);
                    rightEdge.add(iAhead);
                    midPoint.add((currentIndex+(iAhead-1))/2);
                    currentIndex = iAhead;
                }
            }
            currentIndex++;
        }
        for (Integer index: midPoint){
            result[index] = (byte)255;
        }
        return result;
    }
}
