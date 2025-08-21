package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;
import schneiderlab.tools.radialprojection.imageprocessor.core.unrolling.UnrollSingleVessel;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class UnrollVesselWorker extends SwingWorker<Void, Void> {
    private final ImagePlus hybridStack;
    private final ImagePlus edgeBinaryMaskEdge;
    private ArrayList<Vessel> vesselArrayList;
    private ArrayList<ImagePlus> vesselUnrolledArrayList;

    public UnrollVesselWorker(ImagePlus hybridStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 ArrayList<Vessel> vesselArrayList) {
        this.hybridStack = hybridStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.vesselArrayList= vesselArrayList;
    }

    public ArrayList<ImagePlus> getVesselPolarProjectionArrayList() {
        return vesselUnrolledArrayList;
    }

    @Override
    protected Void doInBackground() {
        vesselUnrolledArrayList = new ArrayList<>(vesselArrayList.size());
        for (int i = 0; i < vesselArrayList.size(); i++) {
            UnrollSingleVessel unrolled = new UnrollSingleVessel(hybridStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5
            );
            ImagePlus vesselUnrolled=unrolled.process();
            String imageTitle = "Unrolled Vessel " + (i + 1);
            vesselUnrolled.setTitle(imageTitle);
            vesselUnrolledArrayList.add(vesselUnrolled);
        }


        return null;
    }
}
