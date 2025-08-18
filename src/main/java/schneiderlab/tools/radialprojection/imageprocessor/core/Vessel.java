package schneiderlab.tools.radialprojection.imageprocessor.core;

import ij.ImagePlus;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;

public class Vessel {
    private ArrayList<Point> centroidList;
    private ArrayList<Point> clickList;
    private ArrayList<Integer[]> trueLabelList;
    private ImagePlus radialProjected;
    private ImagePlus unrolled;

    public Vessel(int numberOfPossibleCentroids) {
        this.centroidList= new ArrayList<>(numberOfPossibleCentroids);
        this.clickList= new ArrayList<>(numberOfPossibleCentroids);
        this.trueLabelList= new ArrayList<>(numberOfPossibleCentroids);
    }

    public ArrayList<Point> getCentroidList() {
        return centroidList;
    }

    public ArrayList<Point> getClickList() {
        return clickList;
    }

    public ArrayList<Integer[]> getTrueLabelList() {
        return trueLabelList;
    }

    public ImagePlus getRadialProjected() {
        return radialProjected;
    }

    public ImagePlus getUnrolled() {
        return unrolled;
    }

    public void setRadialProjected(ImagePlus radialProjected) {
        this.radialProjected = radialProjected;
    }

    public void setUnrolled(ImagePlus unrolled) {
        this.unrolled = unrolled;
    }
}
