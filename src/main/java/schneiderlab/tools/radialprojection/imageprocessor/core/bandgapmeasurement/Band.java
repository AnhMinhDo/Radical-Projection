package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import ij.gui.Line;

import java.awt.*;

public class Band {
    private final Point peak;
    private final double peakIntensity;
    private final int width;
    private final Point leftEdge;
    private final Point rightEdge;
    private final double prominence;

    public Band(Point peak, Point leftEdge, Point rightEdge, double peakIntensity, int width, double prominence) {
        this.peak = peak;
        this.leftEdge=leftEdge;
        this.rightEdge=rightEdge;
        this.peakIntensity = peakIntensity;
        this.width = width;
        this.prominence = prominence;
    }

    public Point getPeak() {
        return peak;
    }

    public Point getLeftEdge() {
        return leftEdge;
    }

    public Point getRightEdge() {
        return rightEdge;
    }

    public double getPeakIntensity() {
        return peakIntensity;
    }

    public int getWidth() {
        return width;
    }

    public double getProminence() {
        return prominence;
    }
}
