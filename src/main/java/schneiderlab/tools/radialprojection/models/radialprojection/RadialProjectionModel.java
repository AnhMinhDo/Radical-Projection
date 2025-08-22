package schneiderlab.tools.radialprojection.models.radialprojection;

import ij.ImagePlus;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;

import java.util.ArrayList;

public class RadialProjectionModel {
    private ImagePlus lignin;
    private ImagePlus hybrid;
    private ImagePlus cellulose;
    private ArrayList<Vessel> vesselArrayList;

    public RadialProjectionModel() {
    }

    public ImagePlus getLignin() {
        return lignin;
    }

    public void setLignin(ImagePlus lignin) {
        this.lignin = lignin;
    }

    public ImagePlus getHybrid() {
        return hybrid;
    }

    public void setHybrid(ImagePlus hybrid) {
        this.hybrid = hybrid;
    }

    public ImagePlus getCellulose() {
        return cellulose;
    }

    public void setCellulose(ImagePlus cellulose) {
        this.cellulose = cellulose;
    }

    public ArrayList<Vessel> getVesselArrayList() {
        return vesselArrayList;
    }

    public void setVesselArrayList(ArrayList<Vessel> vesselArrayList) {
        this.vesselArrayList = vesselArrayList;
    }
}
