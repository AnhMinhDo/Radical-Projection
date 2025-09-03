package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import ij.ImagePlus;
import ij.gui.Roi;
import ij.process.AutoThresholder;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import xsbti.api.Public;

import java.util.ArrayList;
import java.util.List;

public class Tile {
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private ShortProcessor shortProcessor;
    private ByteProcessor thresholdedMask;

    public Tile(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    @Override
    public String toString() {
        return "X: " + x + " Y: " + y + " width: " + width + " height: " + height;
    }

    public static ArrayList<Tile> divideIntoEqualSize(int imageWidth, int imageHeight, double percentage){
        int sizeOf1Portion = (int)(imageHeight * (percentage / 100));
        int numberOfEqualPortion = (int) imageHeight/sizeOf1Portion;
        int remainder = imageHeight - numberOfEqualPortion*sizeOf1Portion;
        ArrayList<Tile> result = new ArrayList<>(numberOfEqualPortion+1);
        int indexTracking = 0;
        for (int i = 0 ; i < numberOfEqualPortion; i++) {
            Tile portion = new Tile(0, indexTracking,imageWidth,sizeOf1Portion);
            indexTracking+=sizeOf1Portion;
            result.add(portion);
        }
        if(remainder != 0){
            Tile remainderPortion = new Tile(0,indexTracking,imageWidth,remainder);
            result.add(remainderPortion); // add the reminder portion
        }
        return result;
    }


    public static void splitImage(ImagePlus imagePlus, List<Tile> tiles) {
        for (Tile t : tiles) {
            Roi roi = new Roi(t.getX(),t.getY(),t.getWidth(),t.getHeight());
            ShortProcessor shortProcessor = (ShortProcessor) imagePlus.getProcessor();
            shortProcessor.setRoi(roi);
            ShortProcessor cropped = (ShortProcessor) shortProcessor.crop();
            t.setImageProcessor(cropped);
        }
    }

    public static ByteProcessor combineTiles(List<Tile> tiles, int fullWidth, int fullHeight) {
        ByteProcessor combined = new ByteProcessor(fullWidth, fullHeight);
        for (Tile t : tiles) {
            combined.insert(t.getThresholdedMask(), t.getX(), t.getY());
        }
        return combined;
    }

    public void autoThreshold(AutoThresholder.Method autoThresholdingMethods, int gaussianBlurValue){
        shortProcessor.blurGaussian(gaussianBlurValue);
        shortProcessor.setAutoThreshold(autoThresholdingMethods,true);
        thresholdedMask = shortProcessor.createMask();
    }


    public ShortProcessor getImageProcessor() {
        return shortProcessor;
    }

    public ByteProcessor getThresholdedMask() {
        return thresholdedMask;
    }

    private void setImageProcessor(ShortProcessor shortProcessor) {
        this.shortProcessor = shortProcessor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
