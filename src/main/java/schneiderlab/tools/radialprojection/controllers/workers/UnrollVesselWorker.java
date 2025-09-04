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
    private final ImagePlus ligninStack;
    private final ImagePlus celluloseStack;
    private final ImagePlus edgeBinaryMaskEdge;
    private final ArrayList<Vessel> vesselArrayList;
    private ArrayList<ImagePlus> vesselUnrolledArrayList;

    public UnrollVesselWorker(ImagePlus hybridStack,
                              ImagePlus celluloseStack,
                              ImagePlus ligninStack,
                                 ImagePlus edgeBinaryMaskEdge,
                                 ArrayList<Vessel> vesselArrayList) {
        this.hybridStack = hybridStack;
        this.ligninStack = ligninStack;
        this.celluloseStack = celluloseStack;
        this.edgeBinaryMaskEdge =edgeBinaryMaskEdge;
        this.vesselArrayList= vesselArrayList;
    }

    public ArrayList<ImagePlus> getVesselPolarProjectionArrayList() {
        return vesselUnrolledArrayList;
    }

    @Override
    protected Void doInBackground() {
        vesselUnrolledArrayList = new ArrayList<>(vesselArrayList.size()*3); // 3 for lignin channel, cellulose channel, hybrid
        int currentProgress = 0;
        int increment = (int) 100.0/(vesselArrayList.size()*3);
        setProgress(currentProgress);
        for (int i = 0; i < vesselArrayList.size(); i++) {
            // create objects for Unrolling class
            UnrollSingleVessel unrolledLignin = new UnrollSingleVessel(ligninStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            UnrollSingleVessel unrolledCellulose = new UnrollSingleVessel(celluloseStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            UnrollSingleVessel unrolledHybrid = new UnrollSingleVessel(hybridStack,
                    edgeBinaryMaskEdge,
                    vesselArrayList.get(i).getCentroidArrayList(),
                    5 // 5 degree is considered adequately small angle
            );
            // start unrolling
            ImagePlus vesselUnrolledHybrid=unrolledHybrid.process();
            currentProgress= currentProgress+increment;
            setProgress(currentProgress);
            ImagePlus vesselUnrolledLignin=unrolledLignin.process();
            currentProgress= currentProgress+increment;
            setProgress(currentProgress);
            ImagePlus vesselUnrolledCellulose=unrolledCellulose.process();
            currentProgress= currentProgress+increment;
            setProgress(currentProgress);
            String imageTitleHybrid = "Unrolled Vessel " + (i + 1) + " Hybrid";
            String imageTitleCellulose = "Unrolled Vessel " + (i + 1) + " Cellulose channel";
            String imageTitleLignin = "Unrolled Vessel " + (i + 1) + " Lignin channel";
            vesselUnrolledHybrid.setTitle(imageTitleHybrid);
            vesselUnrolledLignin.setTitle(imageTitleLignin);
            vesselUnrolledCellulose.setTitle(imageTitleCellulose);
            vesselUnrolledArrayList.add(vesselUnrolledHybrid);
            vesselUnrolledArrayList.add(vesselUnrolledLignin);
            vesselUnrolledArrayList.add(vesselUnrolledCellulose);
            vesselArrayList.get(i).setUnrolledVesselHybrid(vesselUnrolledHybrid.duplicate());
            vesselArrayList.get(i).setUnrolledVesselLignin(vesselUnrolledLignin.duplicate());
            vesselArrayList.get(i).setUnrolledVesselCellulose(vesselUnrolledCellulose.duplicate());
        }


        return null;
    }
}
