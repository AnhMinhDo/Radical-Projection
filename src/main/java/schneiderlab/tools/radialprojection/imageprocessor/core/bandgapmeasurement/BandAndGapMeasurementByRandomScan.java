package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.ProfilePlot;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class BandAndGapMeasurementByRandomScan {
    private final int numberOfRandomLineScan;
    private final int lineScanLengthInPixel;
    private final ShortProcessor inputImage;
    private final short[] inputImagePixelArray;
    private List<LineScan> lineScanList;
    private ShortProcessor imageWithOnlyScannedBands;


    public BandAndGapMeasurementByRandomScan(ShortProcessor inputImageProcessor,
                                             int numberOfRandomLineScan,
                                             int lineScanLengthInMicroMeter,
                                             int pixelSizeInNm){
        this.numberOfRandomLineScan = numberOfRandomLineScan;
        this.inputImage = inputImageProcessor;
        this.inputImagePixelArray = (short[]) inputImage.getPixels();
        int inputLineScanLengthInPixel = lineScanLengthInMicroMeter*1000/pixelSizeInNm;
        if(inputLineScanLengthInPixel <= inputImageProcessor.getWidth()){
            lineScanLengthInPixel = inputLineScanLengthInPixel;
        } else {
            lineScanLengthInPixel = inputImageProcessor.getWidth();
        }
    }

    public ShortProcessor getImageWithOnlyScannedBands() {
        if(this.imageWithOnlyScannedBands == null){
            process();
            return imageWithOnlyScannedBands;
        } else {
            return imageWithOnlyScannedBands;
        }
    }

    public void process(){
        this.lineScanList = generateRandomLineScan(numberOfRandomLineScan,
                lineScanLengthInPixel,
                inputImage.getWidth(),
                inputImage.getHeight(),
                inputImage);
        this.imageWithOnlyScannedBands = imageWithOnlyDetectedBand();
    }

    private ShortProcessor imageWithOnlyDetectedBand(){
        ShortProcessor outputImage = new ShortProcessor(inputImage.getWidth(),inputImage.getHeight());
        short[] outputImagePixelArray = (short[]) outputImage.getPixels();
        int width = inputImage.getWidth();
        for(LineScan lineScan : lineScanList){
            for (Band band : lineScan.getBandList()){
                int yLeft = band.getLeftEdge().y;
                int xLeft = band.getLeftEdge().x;
                int yRight = band.getRightEdge().y;
                int xRight = band.getRightEdge().x;
                int startIdx = yLeft*width+xLeft;
                int endIdx = yRight*width+xRight;
                for (int i = startIdx; i <= endIdx; i++) {
                    outputImagePixelArray[i] = inputImagePixelArray[i];
                }
            }
        }
        return outputImage;
    }

    private static List<LineScan> generateRandomLineScan(int numberOfRandomLineScan,
                                                         int lineScanLengthInPixel,
                                                         int imageWidth,
                                                         int imageHeight,
                                                         ShortProcessor inputImageProcessor){
        List<LineScan> lineScanList = new ArrayList<>(numberOfRandomLineScan);
        // the lineScan is to both side of the random point
        int halfTheLength = (int)Math.round(lineScanLengthInPixel/2.0);
        int xLowerBound = halfTheLength;
        int xUpperBound = imageWidth - halfTheLength;
        int yLowerBound = 0;
        int yUpperBound = imageHeight-1;
        for (int i = 0; i < numberOfRandomLineScan; i++) {
            int xRandom = ThreadLocalRandom.current().nextInt(xLowerBound,xUpperBound+1); // +1 for the exclusion
            int yRandom = ThreadLocalRandom.current().nextInt(yLowerBound,yUpperBound+1); // +1 for the exclusion
            // find the left point and right point
            int xLeft = Math.max(0,xRandom-halfTheLength);
            int yLeft = yRandom;
            int xRight = Math.min(xRandom+halfTheLength,imageWidth-1);
            int yRight = yRandom;
            double[] lineScanArray = inputImageProcessor.getLine(xLeft,yLeft,xRight,yRight);
            LineScan lineScan = new LineScan(new Point(xLeft, yLeft), new Point(xRight,yRight),lineScanArray);
            lineScan.process();
            lineScanList.add(lineScan);
        }
        return lineScanList;
    }
}
