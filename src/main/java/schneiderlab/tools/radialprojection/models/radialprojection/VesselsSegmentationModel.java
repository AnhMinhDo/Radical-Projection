package schneiderlab.tools.radialprojection.models.radialprojection;

import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Overlay;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class VesselsSegmentationModel {
    private int xyPixelSize;
    private int zPixelSize;
    private int analysisWindow;
    private double smoothingSigma;
    private int sliceIndexForTuning;
    private double innerVesselRadius;
    private int CelluloseToLigninRatio;
    private ImgPlus<UnsignedShortType> sideView;
    private RandomAccessibleInterval<FloatType> lignin;
    private RandomAccessibleInterval<FloatType> cellulose;
    private RandomAccessibleInterval<FloatType> hybridStackNonSmoothed;
    private RandomAccessibleInterval<FloatType> hybridStackSmoothed;
    private int hybridStackSmoothedWidth;
    private int hybridStackSmoothedHeight;
    private ImagePlus edgeBinaryMaskImagePlus;
    private Overlay overlaySegmentation;
    private ImagePlus impInByte;
    private final ArrayList<Point> coordinates = new ArrayList<>() ;
    private final ArrayList<Point> coordinatesBatch = new ArrayList<>() ;
    private HashMap<Integer, ArrayList<Point>> centroidHashMap;
    private ArrayList<Vessel> vesselArrayList;

    public VesselsSegmentationModel() {
    }

    public void initValues(String propertiesFile){
        // load initial values for cziToTifModel from properties file
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/properties_files/initValues.properties")){
            props.load(input);
            int xyPixelSize = (int)Prefs.get("RadialProjection.VesselsSegmentationModel.xyPixelSize",
                    Integer.parseInt(props.getProperty("VesselsSegmentationModel.xyPixelSize")));
            this.setXyPixelSize(xyPixelSize);
            int zPixelSize = (int)Prefs.get("RadialProjection.VesselsSegmentationModel.zPixelSize",
                    Integer.parseInt(props.getProperty("VesselsSegmentationModel.zPixelSize")));
            this.setzPixelSize(zPixelSize);
            int analysisWindow = (int)Prefs.get("RadialProjection.VesselsSegmentationModel.analysisWindow",
                    Integer.parseInt(props.getProperty("VesselsSegmentationModel.analysisWindow")));
            this.setAnalysisWindow(analysisWindow);
            double smoothingSigma = Prefs.get("RadialProjection.VesselsSegmentationModel.smoothingSigma",
                    Double.parseDouble(props.getProperty("VesselsSegmentationModel.smoothingSigma")));
            this.setSmoothingSigma(smoothingSigma);
            int sliceIndexForTuning = (int)Prefs.get("RadialProjection.VesselsSegmentationModel.sliceIndexForTuning",
                    Integer.parseInt(props.getProperty("VesselsSegmentationModel.sliceIndexForTuning")));
            this.setSliceIndexForTuning(sliceIndexForTuning);
            double innerVesselRadius = Prefs.get("RadialProjection.VesselsSegmentationModel.innerVesselRadius",
                    Double.parseDouble(props.getProperty("VesselsSegmentationModel.innerVesselRadius")));
            this.setInnerVesselRadius(innerVesselRadius);
            int celluloseToLigninRatio= (int)Prefs.get("RadialProjection.VesselsSegmentationModel.celluloseToLigninRatio",
                    Integer.parseInt(props.getProperty("VesselsSegmentationModel.celluloseToLigninRatio")));
            this.setCelluloseToLigninRatio(celluloseToLigninRatio);
        } catch (IOException e){
            System.err.println("Fail to load .properties file");
        }
    }

    public ImgPlus<UnsignedShortType> getSideView() {
        return sideView;
    }

    public void setSideView(ImgPlus<UnsignedShortType> sideView) {
        this.sideView = sideView;
    }

    public RandomAccessibleInterval<FloatType> getHybridStackNonSmoothed() {
        return hybridStackNonSmoothed;
    }

    public void setHybridStackNonSmoothed(RandomAccessibleInterval<FloatType> hybridStackNonSmoothed) {
        this.hybridStackNonSmoothed = hybridStackNonSmoothed;
    }

    public RandomAccessibleInterval<FloatType> getHybridStackSmoothed() {
        return hybridStackSmoothed;
    }

    public void setHybridStackSmoothed(RandomAccessibleInterval<FloatType> hybridStackSmoothed) {
        this.hybridStackSmoothed = hybridStackSmoothed;
    }

    public int getHybridStackSmoothedWidth() {
        return hybridStackSmoothedWidth;
    }

    public void setHybridStackSmoothedWidth(int hybridStackSmoothedWidth) {
        this.hybridStackSmoothedWidth = hybridStackSmoothedWidth;
    }

    public int getHybridStackSmoothedHeight() {
        return hybridStackSmoothedHeight;
    }

    public void setHybridStackSmoothedHeight(int hybridStackSmoothedHeight) {
        this.hybridStackSmoothedHeight = hybridStackSmoothedHeight;
    }

    public ImagePlus getEdgeBinaryMaskImagePlus() {
        return edgeBinaryMaskImagePlus;
    }

    public void setEdgeBinaryMaskImagePlus(ImagePlus edgeBinaryMaskImagePlus) {
        this.edgeBinaryMaskImagePlus = edgeBinaryMaskImagePlus;
    }

    public RandomAccessibleInterval<FloatType> getLignin() {
        return lignin;
    }

    public void setLignin(RandomAccessibleInterval<FloatType> lignin) {
        this.lignin = lignin;
    }

    public RandomAccessibleInterval<FloatType> getCellulose() {
        return cellulose;
    }

    public void setCellulose(RandomAccessibleInterval<FloatType> cellulose) {
        this.cellulose = cellulose;
    }

    public Overlay getOverlaySegmentation() {
        return overlaySegmentation;
    }

    public void setOverlaySegmentation(Overlay overlaySegmentation) {
        this.overlaySegmentation = overlaySegmentation;
    }

    public ImagePlus getImpInByte() {
        return impInByte;
    }

    public void setImpInByte(ImagePlus impInByte) {
        this.impInByte = impInByte;
    }

    public ArrayList<Point> getCoordinates() {
        return coordinates;
    }

    public ArrayList<Point> getCoordinatesBatch() {
        return coordinatesBatch;
    }

    public HashMap<Integer, ArrayList<Point>> getCentroidHashMap() {
        return centroidHashMap;
    }

    public void setCentroidHashMap(HashMap<Integer, ArrayList<Point>> centroidHashMap) {
        this.centroidHashMap = centroidHashMap;
    }

    public ArrayList<Vessel> getVesselArrayList() {
        return vesselArrayList;
    }

    public void setVesselArrayList(ArrayList<Vessel> vesselArrayList) {
        this.vesselArrayList = vesselArrayList;
    }

    public int getXyPixelSize() {
        return xyPixelSize;
    }

    public void setXyPixelSize(int xyPixelSize) {
        this.xyPixelSize = xyPixelSize;
    }

    public int getzPixelSize() {
        return zPixelSize;
    }

    public void setzPixelSize(int zPixelSize) {
        this.zPixelSize = zPixelSize;
    }

    public int getAnalysisWindow() {
        return analysisWindow;
    }

    public void setAnalysisWindow(int analysisWindow) {
        this.analysisWindow = analysisWindow;
    }

    public double getSmoothingSigma() {
        return smoothingSigma;
    }

    public void setSmoothingSigma(double smoothingSigma) {
        this.smoothingSigma = smoothingSigma;
    }

    public int getSliceIndexForTuning() {
        return sliceIndexForTuning;
    }

    public void setSliceIndexForTuning(int sliceIndexForTuning) {
        this.sliceIndexForTuning = sliceIndexForTuning;
    }

    public double getInnerVesselRadius() {
        return innerVesselRadius;
    }

    public void setInnerVesselRadius(double innerVesselRadius) {
        this.innerVesselRadius = innerVesselRadius;
    }

    public int getCelluloseToLigninRatio() {
        return CelluloseToLigninRatio;
    }

    public void setCelluloseToLigninRatio(int celluloseToLigninRatio) {
        CelluloseToLigninRatio = celluloseToLigninRatio;
    }
}
