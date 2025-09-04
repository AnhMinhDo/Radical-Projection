package schneiderlab.tools.radialprojection.imageprocessor.core;

import ij.ImagePlus;

import java.awt.*;
import java.util.ArrayList;

public class Vessel {
    private final ArrayList<VesselSliceData>  vesselSliceDataArrayList;
    private final ArrayList<Point> centroidArrayList ;
    private ImagePlus radialProjectionHybrid;
    private ImagePlus radialProjectionCellulose;
    private ImagePlus radialProjectionLignin;
//    private ImagePlus unrolledVessel;
    private ImagePlus unrolledVesselHybrid;
    private ImagePlus unrolledVesselCellulose;
    private ImagePlus unrolledVesselLignin;


    public Vessel(int numberOfSliceInStack) {
        this.vesselSliceDataArrayList= new ArrayList<>(numberOfSliceInStack);
        this.centroidArrayList = new ArrayList<>(numberOfSliceInStack);
    }

    public void addVesselSliceData(Point clickPoint, Point centroid, int trueSliceIndex, int trueLabel){
        vesselSliceDataArrayList.add(new VesselSliceData(centroid,clickPoint, trueSliceIndex,trueLabel));
    }

    public Point getClickPoint(int index){
        return vesselSliceDataArrayList.get(index).getClickPoint();
    }
    public Point getCentroid(int index){
        return vesselSliceDataArrayList.get(index).getCentroid();
    }
    public int getTrueSliceIndex(int index){
        return vesselSliceDataArrayList.get(index).getTrueSliceIndex();
    }
    public int getTrueLabel(int index){
        return vesselSliceDataArrayList.get(index).getTrueLabel();
    }

    public ArrayList<Point> getCentroidArrayList(){
        if (centroidArrayList.isEmpty()){
            generateCentroidArrayList();
            return this.centroidArrayList;
        } else {
            return this.centroidArrayList;
        }
    }

    public void generateCentroidArrayList(){
        for (VesselSliceData vesselSliceData:vesselSliceDataArrayList){
            centroidArrayList.add(vesselSliceData.getCentroid());
        }
    }

    public ImagePlus getRadialProjectionHybrid() {
        return radialProjectionHybrid;
    }

    public void setRadialProjectionHybrid(ImagePlus radialProjectionHybrid) {
        this.radialProjectionHybrid = radialProjectionHybrid;
    }

    public ImagePlus getRadialProjectionCellulose() {
        return radialProjectionCellulose;
    }

    public void setRadialProjectionCellulose(ImagePlus radialProjectionCellulose) {
        this.radialProjectionCellulose = radialProjectionCellulose;
    }

    public ImagePlus getRadialProjectionLignin() {
        return radialProjectionLignin;
    }

    public void setRadialProjectionLignin(ImagePlus radialProjectionLignin) {
        this.radialProjectionLignin = radialProjectionLignin;
    }

    public ImagePlus getUnrolledVesselHybrid() {
        return unrolledVesselHybrid;
    }

    public void setUnrolledVesselHybrid(ImagePlus unrolledVesselHybrid) {
        this.unrolledVesselHybrid = unrolledVesselHybrid;
    }

    public ImagePlus getUnrolledVesselCellulose() {
        return unrolledVesselCellulose;
    }

    public void setUnrolledVesselCellulose(ImagePlus unrolledVesselCellulose) {
        this.unrolledVesselCellulose = unrolledVesselCellulose;
    }

    public ImagePlus getUnrolledVesselLignin() {
        return unrolledVesselLignin;
    }

    public void setUnrolledVesselLignin(ImagePlus unrolledVesselLignin) {
        this.unrolledVesselLignin = unrolledVesselLignin;
    }

//    public ImagePlus getUnrolledVessel() {
//        return unrolledVessel;
//    }
//
//    public void setUnrolledVessel(ImagePlus unrolledVessel) {
//        this.unrolledVessel = unrolledVessel;
//    }

    private static class VesselSliceData{
        private final Point centroid;
        private final Point clickPoint;
        private final int trueSliceIndex;
        private final int trueLabel;

        public VesselSliceData(Point centroid, Point clickPoint, int sliceIndex, int label) {
            this.centroid = centroid;
            this.clickPoint = clickPoint;
            this.trueSliceIndex = sliceIndex;
            this.trueLabel = label;
        }
        public Point getCentroid() {
            return centroid;
        }
        public Point getClickPoint() {
            return clickPoint;
        }
        public int getTrueSliceIndex() {
            return trueSliceIndex;
        }
        public int getTrueLabel() {
            return trueLabel;
        }
    }

}
