package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import ij.process.ByteProcessor;
import ij.process.ShortProcessor;

import java.util.ArrayList;
import java.util.Arrays;

public class Utils {
    public static byte[] findLocalMaxima(short[] inputArray){
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
    
    public static ByteProcessor detectBandPath(ShortProcessor inputImageProcessor, int width, int height){
        short[] inputShortArray = (short[]) inputImageProcessor.getPixels();
        byte[] resultByteArray = new byte[inputShortArray.length];
        for (int i = 0; i < inputShortArray.length; i+= width) {
            short[] sampledShortArray = Arrays.copyOfRange(inputShortArray,i,i+width);
            byte[] detectedPeak = findLocalMaxima(sampledShortArray);
            System.arraycopy(detectedPeak,0,resultByteArray,i,width);
        }
        return new ByteProcessor(width,height,resultByteArray);
    }

    public static ShortProcessor applyMask(ByteProcessor mask, ShortProcessor originalImage){
        ShortProcessor result = new ShortProcessor(originalImage.getWidth(),originalImage.getHeight());
        short[] resultPixelArray = (short[]) result.getPixels();
        byte[] maskPixelArray = (byte[]) mask.getPixels();
        short[] originalPixelArray = (short[]) originalImage.getPixels();
        for (int i = 0; i < maskPixelArray.length; i++) {
            if(maskPixelArray[i] != 0){
                resultPixelArray[i] = originalPixelArray[i];
            }
        }
        return result;
    }

}
