package schneiderlab.tools.radialprojection.controllers.workers;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;
import schneiderlab.tools.radialprojection.imageprocessor.core.polarprojection.PolarProjection;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class PolarProjectionWorker extends SwingWorker<Void, Void> {
    private final ImagePlus hybridStack;
    private final ImagePlus celluloseStack;
    private final ImagePlus ligninStack;
    private final ImagePlus edgeBinaryMaskEdge;
    private ArrayList<Vessel> vesselArrayList;
    private ArrayList<ImagePlus> vesselPolarProjectionArrayList;

    public PolarProjectionWorker(ImagePlus hybridStack,
                                 ImagePlus celluloseStack,
                                 ImagePlus ligninStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 ArrayList<Vessel> vesselArrayList) {
        this.hybridStack = hybridStack;
        this.celluloseStack = celluloseStack;
        this.ligninStack = ligninStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.vesselArrayList= vesselArrayList;
    }

    public ArrayList<ImagePlus> getVesselPolarProjectionArrayList() {
        return vesselPolarProjectionArrayList;
    }

    @Override
    protected Void doInBackground() {
        vesselPolarProjectionArrayList = new ArrayList<>(vesselArrayList.size());
        for (int i = 0; i < vesselArrayList.size(); i++) { // for each vessel
            PolarProjection polarProjectionHybrid = new PolarProjection(hybridStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            PolarProjection polarProjectionCellulose = new PolarProjection(celluloseStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            PolarProjection polarProjectionLignin = new PolarProjection(ligninStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            ImagePlus vesselPolarProjectionHybrid=polarProjectionHybrid.process();
            ImagePlus vesselPolarProjectionCellulose=polarProjectionCellulose.process();
            ImagePlus vesselPolarProjectionLignin=polarProjectionLignin.process();
            String imageTitleHybrid = "Radial Projection Vessel " + (i + 1) + " Hybrid";
            String imageTitleCellulose = "Radial Projection Vessel " + (i + 1) + " Cellulose channel";
            String imageTitleLignin = "Radial Projection Vessel " + (i + 1) + " Lignin channel";
            vesselPolarProjectionHybrid.setTitle(imageTitleHybrid);
            vesselPolarProjectionCellulose.setTitle(imageTitleCellulose);
            vesselPolarProjectionLignin.setTitle(imageTitleLignin);
            vesselPolarProjectionArrayList.add(vesselPolarProjectionHybrid);
            vesselPolarProjectionArrayList.add(vesselPolarProjectionCellulose);
            vesselPolarProjectionArrayList.add(vesselPolarProjectionLignin);
            vesselArrayList.get(i).setRadialProjectionHybrid(vesselPolarProjectionHybrid);
        }
        return null;
    }
}
