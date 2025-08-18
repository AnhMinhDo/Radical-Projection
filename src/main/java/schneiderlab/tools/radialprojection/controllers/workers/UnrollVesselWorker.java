package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.unrolling.UnrollSingleVessel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class UnrollVesselWorker extends SwingWorker<Void, Void> {
    private final ImagePlus hybridStack;
    private final ImagePlus edgeBinaryMaskEdge;
    private final ArrayList<Point> centroidListVessel1;
    private final ArrayList<Point> centroidListVessel2;
    private ImagePlus vessel1Unrolled;
    private ImagePlus vessel2Unrolled;

    public UnrollVesselWorker(ImagePlus hybridStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 HashMap<Integer, ArrayList<Point>> centroidHashMap) {
        this.hybridStack = hybridStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.centroidListVessel1 = centroidHashMap.get(1);
        this.centroidListVessel2 = centroidHashMap.get(2);
    }

    public ImagePlus getVessel1Unrolled() {
        return vessel1Unrolled;
    }

    public ImagePlus getVessel2Unrolled() {
        return vessel2Unrolled;
    }

    @Override
    protected Void doInBackground() {
        UnrollSingleVessel unrolled1 = new UnrollSingleVessel(hybridStack,
                edgeBinaryMaskEdge,
                centroidListVessel1,
                5
        );
        vessel1Unrolled=unrolled1.process();
        UnrollSingleVessel unrolled2 = new UnrollSingleVessel(hybridStack,
                edgeBinaryMaskEdge,
                centroidListVessel2,
                5
        );
        vessel2Unrolled=unrolled2.process();
        return null;
    }
}
