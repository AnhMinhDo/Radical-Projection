package schneiderlab.tools.radialprojection.controllers.workers;

import ij.process.ShortProcessor;
import schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement.BandAndGapMeasurementByRandomScan;
import schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement.LineScan;

import javax.swing.*;
import java.util.List;

public class RandomLineScanWorker extends SwingWorker {
    private final int numberOfRandomLineScan;
    private final int lineScanLengthInMicroMeter;
    private final int pixelSizeInNm;
    private final ShortProcessor inputImage;
    private final short[] inputImagePixelArray;
    private List<LineScan> lineScanList;
    private ShortProcessor imageWithOnlyScanBand;

    public RandomLineScanWorker(int numberOfRandomLineScan,
                                int lineScanLengthInMicroMeter,
                                int pixelSizeInNm,
                                ShortProcessor inputImage) {
        this.numberOfRandomLineScan = numberOfRandomLineScan;
        this.lineScanLengthInMicroMeter = lineScanLengthInMicroMeter;
        this.pixelSizeInNm=pixelSizeInNm;
        this.inputImage = inputImage;
        this.inputImagePixelArray = (short[]) inputImage.getPixels();
    }

    public ShortProcessor getImageWithOnlyScanBand() {
        return imageWithOnlyScanBand;
    }

    @Override
    protected Object doInBackground() throws Exception {
        BandAndGapMeasurementByRandomScan bagmbrs = new BandAndGapMeasurementByRandomScan(inputImage,
                numberOfRandomLineScan,
                lineScanLengthInMicroMeter,
                pixelSizeInNm);
        bagmbrs.process();
        imageWithOnlyScanBand=bagmbrs.getImageWithOnlyScannedBands();
        return null;
    }
}
