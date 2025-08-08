package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import net.imagej.ops.Ops;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import schneiderlab.tools.radialprojection.imageprocessor.core.segmentation.Reconstruction;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;

public class SegmentWholeStackWorker extends SwingWorker<Void, Void> {
    private RandomAccessibleInterval<FloatType> hybridStackSmoothed;
    private int hybridStackSmoothedWidth;
    private int hybridStackSmoothedHeight;
    private double vesselRadius;
    private ArrayList<Point> coordinatesBatch;
    private int slideForTuning;
    private int pixelScaleINNanometer;
    private Reconstruction recon;
    private ImagePlus finalSegmentation;
    private ImagePlus edgeBinaryMaskImagePlus;
    private HashMap<Integer, ArrayList<Point>> centroidHashMap;
    private ImagePlus stackWithVesselEdgeCentroidOverlay;
    private ImagePlus edgeCentroidImagePlus;

    public SegmentWholeStackWorker(RandomAccessibleInterval<FloatType> hybridStackSmoothed,
                                   int hybridStackSmoothedWidth,
                                   int hybridStackSmoothedHeight,
                                   double vesselRadius,
                                   ArrayList<Point> coordinatesBatch,
                                   int slideForTuning,
                                   int pixelScaleINNanometer) {
        this.hybridStackSmoothed = hybridStackSmoothed;
        this.hybridStackSmoothedWidth = hybridStackSmoothedWidth;
        this.hybridStackSmoothedHeight = hybridStackSmoothedHeight;
        this.vesselRadius = vesselRadius;
        this.coordinatesBatch = coordinatesBatch;
        this.slideForTuning = slideForTuning;
        this.pixelScaleINNanometer = pixelScaleINNanometer;

    }

    public ImagePlus getFinalSegmentation() {
        return finalSegmentation;
    }

    public ImagePlus getEdgeBinaryMaskImagePlus() {
        return edgeBinaryMaskImagePlus;
    }
    public ImagePlus getEdgeCentroidMaskImagePlus() {
        return edgeCentroidImagePlus;
    }

    public HashMap<Integer, ArrayList<Point>> getCentroidHashMap() {
        return centroidHashMap;
    }

    public ImagePlus getStackWithVesselEdgeCentroidOverlay() {
        return stackWithVesselEdgeCentroidOverlay;
    }

    @Override
    protected Void doInBackground() throws Exception {
        recon = new Reconstruction(hybridStackSmoothed,
                hybridStackSmoothedWidth,
                hybridStackSmoothedHeight,
                vesselRadius,
                coordinatesBatch,
                slideForTuning,
                pixelScaleINNanometer
        );
        int totalNumOfSlice = (int)hybridStackSmoothed.dimension(2);
        recon.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("currentSlice".equals(evt.getPropertyName())){
                    int currentSlice = (int) evt.getNewValue();
                    int currentProgress=(int)Math.floor(currentSlice*(double)(100.0/totalNumOfSlice));
                    setProgress(currentProgress);
                }
            }
        });
        this.finalSegmentation = recon.processWholeStack();
        this.edgeBinaryMaskImagePlus = recon.getEdgeBinaryMaskImagePlus();
        this.centroidHashMap = recon.getCentroidHashMap();
        this.stackWithVesselEdgeCentroidOverlay = recon.getStackWithVesselEdgeCentroidOverlay();
        this.edgeCentroidImagePlus = recon.getEdgeCentroidMaskImgPlus();
        return null;
    }
}
