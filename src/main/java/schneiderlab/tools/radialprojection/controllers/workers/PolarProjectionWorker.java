package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;
import schneiderlab.tools.radialprojection.imageprocessor.core.polarprojection.PolarProjection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PolarProjectionWorker extends SwingWorker<Void, Void> {
    private final ImagePlus hybridStack;
    private final ImagePlus edgeBinaryMaskEdge;
    private ArrayList<Vessel> vesselArrayList;
    private ArrayList<ImagePlus> vesselPolarProjectionArrayList;

    public PolarProjectionWorker(ImagePlus hybridStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 ArrayList<Vessel> vesselArrayList) {
        this.hybridStack = hybridStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.vesselArrayList= vesselArrayList;
    }

    public ArrayList<ImagePlus> getVesselPolarProjectionArrayList() {
        return vesselPolarProjectionArrayList;
    }

    @Override
    protected Void doInBackground() {
        vesselPolarProjectionArrayList = new ArrayList<>(vesselArrayList.size());
        for (int i = 0; i < vesselArrayList.size(); i++) {
            PolarProjection polarProjection = new PolarProjection(hybridStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            ImagePlus vesselPolarProjection=polarProjection.process();
            String imageTitle = "Radial Projection Vessel " + (i + 1);
            vesselPolarProjection.setTitle(imageTitle);
            vesselPolarProjectionArrayList.add(vesselPolarProjection);
            vesselArrayList.get(i).setRadialProjection(vesselPolarProjection);
        }
        return null;
    }
}
