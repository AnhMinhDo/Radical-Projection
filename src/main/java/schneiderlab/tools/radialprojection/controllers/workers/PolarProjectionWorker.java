package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.polarprojection.PolarProjection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PolarProjectionWorker extends SwingWorker<Void, Void> {
    private ImagePlus hybridStack;
    private ImagePlus edgeBinaryMaskEdge;
    private ArrayList<Point> centroidListVessel1;
    private ArrayList<Point> centroidListVessel2;
    private ImagePlus vessel1PolarProjection;
    private ImagePlus vessel2PolarProjection;

    public PolarProjectionWorker(ImagePlus hybridStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 HashMap<Integer, ArrayList<Point>> centroidHashMap) {
        this.hybridStack = hybridStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.centroidListVessel1 = centroidHashMap.get(1);
        this.centroidListVessel2 = centroidHashMap.get(2);
    }

    public ImagePlus getVessel1PolarProjection() {
        return vessel1PolarProjection;
    }

    public ImagePlus getVessel2PolarProjection() {
        return vessel2PolarProjection;
    }

    @Override
    protected Void doInBackground() throws Exception {
        PolarProjection polarProjection1 = new PolarProjection(hybridStack,
                edgeBinaryMaskEdge,
                centroidListVessel1,
                5
                );
        vessel1PolarProjection=polarProjection1.process();
        PolarProjection polarProjection2 = new PolarProjection(hybridStack,
                edgeBinaryMaskEdge,
                centroidListVessel2,
                5
        );
        vessel2PolarProjection=polarProjection2.process();
        return null;
    }
}
