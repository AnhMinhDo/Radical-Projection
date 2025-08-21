package schneiderlab.tools.radialprojection.imageprocessor.core.segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.plugin.filter.ThresholdToSelection;
import ij.process.*;
import inra.ijpb.label.LabelImages;
import inra.ijpb.math.ImageCalculator;
import inra.ijpb.measure.region2d.Centroid;
import inra.ijpb.morphology.Morphology;
import inra.ijpb.morphology.Strel;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;
import schneiderlab.tools.radialprojection.imageprocessor.core.utils.RadialProjectionUtils;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ij.IJ.debugMode;

public class Reconstruction {
    private final ArrayList<Point> coordinatesOutside;
    private final int width;
    private final int height;
    private final double radius;
    private final RandomAccessibleInterval<FloatType> smoothedStack;
    private final Point pointForBackground;
    private final int slideForTuning;
    private HashMap<Integer, ArrayList<Point>> centroidHashMap;
    private final int pixelScaleInNanometer;
    private ImagePlus edgeBinaryMaskImagePlus;
    private ImagePlus edgeCentroidMaskImgPlus;
    private ArrayList<Vessel> vesselsArray;

    public Reconstruction(RandomAccessibleInterval<FloatType> smoothedStack,
                          int width,
                          int height,
                          double radius,
                          ArrayList<Point> coordinates,
                          int slideForTuning,
                          int pixelScaleInNanometer) {
        this.smoothedStack = smoothedStack;
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.coordinatesOutside = coordinates;
        pointForBackground = new Point((int) width - 1, (int) height - 1);
        this.slideForTuning = slideForTuning;
        this.pixelScaleInNanometer = pixelScaleInNanometer;
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }

    private int currentSliceProcess;

    public int getCurrentSlice() {
        return this.currentSliceProcess;
    }

    public void setNewCurrentSlice(int newCurrentSlice) {
        int previousSlice = this.currentSliceProcess;
        this.currentSliceProcess = newCurrentSlice;
        this.pcs.firePropertyChange("currentSlice", previousSlice, currentSliceProcess);
    }

    public HashMap<Integer, ArrayList<Point>> getCentroidHashMap() {
        if(centroidHashMap.isEmpty()){
            generateCentroidHashMap();
            return centroidHashMap;
        }else {
            return centroidHashMap;
        }
    }
    public ArrayList<Vessel> getVesselsArray(){
        return vesselsArray;
    }

    private void generateCentroidHashMap(){
        centroidHashMap = new HashMap<>();
        for (int i = 0; i < vesselsArray.size(); i++) {
            centroidHashMap.put(i+1,vesselsArray.get(i).getCentroidArrayList());
        }
    }

    public ImagePlus getEdgeBinaryMaskImagePlus() {
        return edgeBinaryMaskImagePlus;
    }

    public ImagePlus getEdgeCentroidMaskImgPlus() {
        return edgeCentroidMaskImgPlus;
    }

    public Overlay process1Slide() {
        // extract 1 slice and convert to Imagej1 Format
        RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, slideForTuning); // dimension 2 is Z
        SegmentationExtendMinimaWaterShed semws = new SegmentationExtendMinimaWaterShed(coordinatesOutside,
                slice2D,
                width,
                height,
                radius,
                pixelScaleInNanometer);
        ImagePlus segmentedImage = semws.performSegmentation();
        // Overlay the segmentation on to the original image
        ImageProcessor segmentedImageProcessor = segmentedImage.getProcessor();
        // get the selected label
        int[] desiredLabel =process1SliceDesiredLabel(segmentedImageProcessor,coordinatesOutside);
        // sort array
        Arrays.sort(desiredLabel);
        // the first value is min threshold and the last value is max threshold
        double min = desiredLabel[0];
        double max = desiredLabel[desiredLabel.length-1];
        segmentedImageProcessor.setThreshold((double) min, (double) max, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection ts = new ThresholdToSelection();
        Roi maskRoi = ts.convert(segmentedImage.getProcessor());
//        segmentedImage.setOverlay(new Overlay(maskRoi));
        // Apply overlay to original image
        return new Overlay(maskRoi);

    }

    public ImagePlus processWholeStack() {
        ImageStack finalStack = new ImageStack(width, height);
        ImageStack edgesStack = new ImageStack(width, height);
        ImageStack edgesCentroidStack = new ImageStack(width, height);
        int numberOfSlicesInStack = (int)smoothedStack.dimension(2); // 2 is the z axis
        ArrayList<Point> startingClick = RadialProjectionUtils.deepCopyPoints(coordinatesOutside);
        // rearrange the user click points to follow imagej scan order
        Comparator<Point> scanOrderComparator = (p1, p2) -> {
            // First sort by Y (top-to-bottom)
            if (p1.y != p2.y) {
                return Integer.compare(p1.y, p2.y);
            }
            // If same Y, sort by X (left-to-right)
            return Integer.compare(p1.x, p2.x);
        };
        startingClick.sort(scanOrderComparator);
        // arrayList to store all the vessel object
        this.vesselsArray = new ArrayList<>();
        // based on the number of clicks, create an equivalent number of vessels object
        for (int i = 0; i < startingClick.size(); i++) {
            Vessel vessel = new Vessel(numberOfSlicesInStack);
            vesselsArray.add(vessel);
        }
        for (int currentSlice = 0; currentSlice < smoothedStack.dimension(2); currentSlice++) { // 2 is the z channel
            this.setNewCurrentSlice(currentSlice);// this is used for update the currentProgress of this method for outer class who calls this class. Used for updating progressBar.
            ArrayList<Point> currentClick = prepareCurrentList(currentSlice,
                                                                vesselsArray,
                                                                startingClick,
                                                                pointForBackground);

            // perform segmentation
            RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, currentSlice); // dimension 2 is Z
            SegmentationExtendMinimaWaterShed semws = new SegmentationExtendMinimaWaterShed(currentClick,
                    slice2D,
                    width,
                    height,
                    radius,
                    pixelScaleInNanometer);
            ImagePlus segmentedImage = semws.performSegmentation();
            // add the image to the stack
            finalStack.addSlice(segmentedImage.getProcessor());
            // identify the centroid with current segmentation
            ArrayList<Point> allSegmentedCentroids = centroidsCoordinatesGivenSegmentedImage(
                    segmentedImage);
            ByteProcessor combinedEdgeMask = new ByteProcessor(segmentedImage.getWidth(),
                                                                segmentedImage.getHeight());
            // loop through all the current vessel objects
            for (int i = 0; i < vesselsArray.size(); i++) {
                Vessel vessel = vesselsArray.get(i);
                updateTrueLabel(vessel,
                        segmentedImage.getProcessor(),
                        currentSlice,
                        currentClick.get(i),
                        allSegmentedCentroids);
                ByteProcessor edgeBinaryMask= getEdgeBinaryMask(finalStack,
                        vessel.getTrueSliceIndex(currentSlice),
                        vessel.getTrueLabel(currentSlice));
                combinedEdgeMask.copyBits(edgeBinaryMask,0,0,Blitter.OR);
            }
            edgesStack.addSlice(combinedEdgeMask);
            // create binary of both edge and centroid
            ByteProcessor edgeCentroidMask = (ByteProcessor) combinedEdgeMask.duplicate();
            // add centroid to edgeCentroidMask
            for (Vessel vessel: vesselsArray){
                edgeCentroidMask.set(vessel.getCentroid(currentSlice).x,
                        vessel.getCentroid(currentSlice).y,
                        255);
            }
            edgesCentroidStack.addSlice(edgeCentroidMask);
            currentClick.clear();
        }
        this.edgeBinaryMaskImagePlus = new ImagePlus("edge mask ", edgesStack);
        this.edgeCentroidMaskImgPlus = new ImagePlus("edge and centroid mask", edgesCentroidStack);
        this.generateCentroidHashMap();
        return new ImagePlus("Segmentation Stack", finalStack);
    }

    private ArrayList<Point> centroidsCoordinatesGivenSegmentedImage(ImagePlus segmentedImg) {
        // array of labels; 1 is not included because in this class, it is corresponding to background
        int[] labels = LabelImages.findAllLabels(segmentedImg);
        // extract the centroid of the newly segmented
        double[][] centroids = Centroid.centroids(segmentedImg.getProcessor(), labels);
        if (debugMode) {
            IJ.log(Arrays.deepToString(centroids));
        }
        // Container for results
        ArrayList<Point> result = new ArrayList<>();
        // round the centroid values, convert to int and Points
        for (int row = 0; row < centroids.length; row++) {
            // Skip invalid centroids (NaN or out of bounds)
            if (Double.isNaN(centroids[row][0]) || Double.isNaN(centroids[row][1])) {
                continue; // Skip corrupted entries
            }
            result.add(new Point((int) Math.round(centroids[row][0]),
                    (int) Math.round(centroids[row][1])));
        }
        return result;
    }

    public static ArrayList<Point> deepCopyPoints(ArrayList<Point> points) {
        ArrayList<Point> copy = new ArrayList<>();
        for (Point p : points) {
            copy.add(new Point(p.x, p.y));
        }
        return copy;
    }


    /**
     * Maps each object ID to the closest newly captured centroid based on the last known centroid position.
     *
     * @param centroidsHashMap A HashMap where each key is an object ID and the value is a list of previously recorded centroids (Points).
     * @param centroidCaptured A list of newly captured centroids (Points) for the current frame.
     * @return A HashMap mapping each object ID to the closest captured centroid based on Manhattan distance.
     */
    public static HashMap<Integer, Point> mapCentroidToIDs(HashMap<Integer, ArrayList<Point>> centroidsHashMap,
                                                           ArrayList<Point> centroidCaptured) {
        HashMap<Integer, Point> result = new HashMap<>();
        // Handle edge cases
        if (centroidsHashMap == null || centroidCaptured == null || centroidCaptured.isEmpty()) {
            return result;
        }

        for (Integer id : centroidsHashMap.keySet()) {
            ArrayList<Point> recordedCentroids = centroidsHashMap.get(id);
            if (recordedCentroids == null || recordedCentroids.isEmpty()) { // if this arrayList of Points is empty, skip it
                continue;  // Skip IDs with no centroids
            }
            Point lastCentroid = recordedCentroids.get(recordedCentroids.size() - 1);
            Point closestCentroid = null;
            // calculate the max value based on the previous Points
            int minDistance = 10; // 10 is magic number, based on observation, the distance between the next centroid and the current one never excess more 10 pixels, this is subjective and a better approach is required
            for (Point current : centroidCaptured) {
                int distance = manhattanDistance(lastCentroid, current);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = current;
                }
            }
            if (closestCentroid != null) {
                result.put(id, closestCentroid);
            } else {
                result.put(id, null); // if no centroid is identified for this ID, insert null
            }
        }
        return result;
    }

    public static Integer manhattanDistance(Point referencePoint, Point measurePoint) {
        int refX = (int) referencePoint.getX();
        int refY = (int) referencePoint.getY();
        int msX = (int) measurePoint.getX();
        int msY = (int) measurePoint.getY();
        return Math.abs(refX - msX) + Math.abs(refY - msY);
    }

    public static HashMap<Integer, Integer> getTheTrueLabelGivenCoordinate(ImagePlus labelImage,
                                                                           HashMap<Integer, Point> centroidCoordinates) {
        ImageProcessor labelProcessor = labelImage.getProcessor();
        HashMap<Integer, Integer> trueLabelWithIdMap = new HashMap<>();
        for (Integer id : centroidCoordinates.keySet()) {
            if (centroidCoordinates.get(id) == null) {
                trueLabelWithIdMap.put(id, null);
            } else {
                int trueLabelValue = (int) labelProcessor.getf(centroidCoordinates.get(id).x, centroidCoordinates.get(id).y);
                trueLabelWithIdMap.put(id, trueLabelValue);
//                labels[index] = (int) labelProcessor.getf(centroidCoordinates.get(id).x, centroidCoordinates.get(id).y);
            }
        }
        return trueLabelWithIdMap;
    }

    public static ImagePlus extractDesiredLabels(ImagePlus labeledImage,
                                                 HashMap<Integer, Integer> desiredLabelsWithIdMap) {
        ImageProcessor labelProcessor = labeledImage.getProcessor();
        int width = labelProcessor.getWidth();
        int height = labelProcessor.getHeight();
        Set<Integer> labelSet = new HashSet<>();
        // Create binary output
        ByteProcessor binaryProcessor = new ByteProcessor(width, height);
        for (Integer id : desiredLabelsWithIdMap.keySet()) {
            labelSet.add(desiredLabelsWithIdMap.get(id));
        }
//        Set<Integer> labelSet = Arrays.stream(desiredLabels).boxed().collect(Collectors.toSet());
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int label = (int) labelProcessor.getf(x, y);
                if (labelSet.contains(label)) {
                    binaryProcessor.set(x, y, 255);
                } else {
                    binaryProcessor.set(x, y, 0);
                }
            }
        }
        return new ImagePlus("Filtered Labels", binaryProcessor);
    }

    public static ImagePlus extractEdgesFromBinary(ImagePlus binaryImage) {
        ImageProcessor bp = new BinaryProcessor((ByteProcessor) binaryImage.getProcessor().duplicate());
        ImageProcessor eroded = new BinaryProcessor((ByteProcessor) binaryImage.getProcessor().duplicate());
        // Create a structuring element (e.g., 3x3 square)
        Strel strel = Strel.Shape.SQUARE.fromDiameter(3);
        // perform erosion
        eroded = Morphology.erosion(bp, strel);
        // perform original - erosion to get edge
        ImageProcessor edgeMaskProcessor = ImageCalculator.combineImages(bp, eroded, ImageCalculator.Operation.MINUS);
        return new ImagePlus("Edges", edgeMaskProcessor);
    }


    private static void addTrueLabelToHashMap(HashMap<Integer, Integer> currentTrueLabelHashMap,
                                              HashMap<Integer, ArrayList<Integer[]>> wholeStackTrueLabelHashMap,
                                              int currentSlice) {
        for (Integer id : wholeStackTrueLabelHashMap.keySet()) {
            ArrayList<Integer[]> arrayListTrueLabel = wholeStackTrueLabelHashMap.get(id);
            arrayListTrueLabel.add(new Integer[]{currentSlice, currentTrueLabelHashMap.get(id)});
        }
    }

    private static void replaceNullWithNewValueInTrueLabelHashMap(HashMap<Integer, ArrayList<Integer[]>> wholeStackTrueLabelHashMap,
                                                                  Integer id,
                                                                  int label,
                                                                  int trueSlice) {
        ArrayList<Integer[]> arrayList = wholeStackTrueLabelHashMap.get(id);
        arrayList.get(arrayList.size() - 1)[0] = trueSlice;
        arrayList.get(arrayList.size() - 1)[1] = label;
    }

    private static ImageProcessor createEdgeCentroidMask(ImagePlus edgeMask, HashMap<Integer, Point> centroidMap) {
        // create Edge-Centroid mask
        ImageProcessor edgeCentroidMaskProcessor = edgeMask.getProcessor().duplicate();
        // add centroid to mask
        for (Integer key : centroidMap.keySet()) {
            edgeCentroidMaskProcessor.putPixel((int) centroidMap.get(key).getX(),
                    (int) centroidMap.get(key).getY(), 255);
        }
        return edgeCentroidMaskProcessor;
    }

    private static void updateCentroidMap(HashMap<Integer, Point> thisSliceCentroidMap, HashMap<Integer, ArrayList<Point>> wholeStackCentroidMap){
        for (Integer key : thisSliceCentroidMap.keySet()) {
            wholeStackCentroidMap.get(key).add(thisSliceCentroidMap.get(key));
        }
    }

    private static void updateClickMap(HashMap<Integer, Point> thisSliceCentroidMap, HashMap<Integer, ArrayList<Point>> wholeStackClickMap){
        for (Integer key : thisSliceCentroidMap.keySet()) {
            wholeStackClickMap.get(key).add(thisSliceCentroidMap.get(key));
        }
    }

    // function given the labeled image and the initial click, return the desired label
    private static int[] process1SliceDesiredLabel (ImageProcessor labeledImageProcessor, ArrayList<Point> initialClicks){
        int[] result = new int[initialClicks.size()];
        for (int i = 0; i < initialClicks.size(); i++) {
            Point point = initialClicks.get(i);
            int label = (int)labeledImageProcessor.getf(point.x,point.y);
            result[i] = label;
        }
        return result;
    }

    // function to prepare new click
    private static ArrayList<Point> prepareCurrentList(int currentSlice,
                                                       ArrayList<Vessel> vesselsArray,
                                                       ArrayList<Point> startingClick,
                                                       Point pointForBackground){
        // the below if-else is to prepare the "clicks" for this slice
        if (currentSlice == 0) {
            // for the first iteration, we use the input from user
            ArrayList<Point> currentClick = deepCopyPoints(startingClick);
            currentClick.add(pointForBackground);
            return  currentClick;
        } else {
            // not the first iteration, use previous centroid as the starting click
            ArrayList<Point> currentClick = new ArrayList<>(vesselsArray.size());
            // get the click for this vessel from the previous Centroid of the VesselSliceData
            for (int i = 0; i < vesselsArray.size(); i++) {
                Vessel vessel = vesselsArray.get(i);
                currentClick.add(vessel.getCentroid(currentSlice-1));
            }
            currentClick.add(pointForBackground);
            return currentClick;
        }
    }

    // function to find the true slice index and true label based
    // centroid, click, trueIndex, trueLabel
    private static void updateTrueLabel(Vessel vessel,
                                        ImageProcessor currentSegmentedImageProcessor,
                                        int currentSlice,
                                        Point currentClick,
                                        ArrayList<Point> centroidCapturedList){
        // compare the click with segmented centroid, choose the closest
        Point closestCentroid = null;
        // calculate the max value based on the previous Points
        int minDistance = currentSlice==0 ? 100 : 10; // 10 is magic number, based on observation, the distance between the next centroid and the current one never excess more 10 pixels, this is subjective and a better approach is required
        for (Point centroid : centroidCapturedList) {
            int distance = manhattanDistance(currentClick, centroid);
            if (distance < minDistance) {
                minDistance = distance;
                closestCentroid = centroid;
            }
        }
        if(closestCentroid != null){
            // Get the trueLabel and trueIndex using currentSegmentedImageProcessor
            int trueLabel = (int)currentSegmentedImageProcessor.getf(closestCentroid.x,closestCentroid.y);
            // create new entry in Vessel
            vessel.addVesselSliceData(currentClick,closestCentroid, currentSlice,trueLabel);
        } else {
            // reuse the trueIndex and trueLabel from previous entry
            int trueIndex = vessel.getTrueSliceIndex(currentSlice-1);
            int trueLabel = vessel.getTrueLabel(currentSlice-1);
            Point consideredCentroid = vessel.getCentroid(currentSlice-1);
            vessel.addVesselSliceData(currentClick,consideredCentroid, trueIndex,trueLabel);
        }
    }

    private static ByteProcessor getEdgeBinaryMask(ImageStack segmentedStack,
                                                    int sliceIndex,
                                                    int labelValue){
        ImageProcessor imageProcessor = segmentedStack.getProcessor(sliceIndex+1);
        ByteProcessor regionMask = new ByteProcessor(imageProcessor.getWidth(),imageProcessor.getHeight());
        // parse all pixel and create mask
        float[] pixelArrayLabeledImage = (float[])imageProcessor.getPixels();
        byte[] pixelArrayMask = (byte[]) regionMask.getPixels();
        // create mask
        for (int i = 0; i < pixelArrayLabeledImage.length; i++) {
            if(pixelArrayLabeledImage[i] == labelValue){
                pixelArrayMask[i] = (byte)255;
            }
        }
        // convert to binaryProcessor for morphological operation
        BinaryProcessor regionBinaryMask = new BinaryProcessor(regionMask);
        // perform erosion
        // Create a structuring element, e.g., 3x3 square
        Strel strel = Strel.Shape.SQUARE.fromDiameter(3);
        ByteProcessor eroded = (ByteProcessor) Morphology.erosion(regionMask, strel);
        // perform original - erosion to get edge
        return (ByteProcessor) ImageCalculator.combineImages(regionBinaryMask, eroded, ImageCalculator.Operation.MINUS);
    }

}
