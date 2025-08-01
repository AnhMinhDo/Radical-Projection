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

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static ij.IJ.debugMode;

public class Reconstruction {
    private ArrayList<Point> coordinatesOutside;
    private final int width;
    private final int height;
    private final int radius;
    private final RandomAccessibleInterval<FloatType> smoothedStack;
    private Point pointForBackground;
    private int slideForTuning;
    private HashMap<Integer,ArrayList<Point>> centroidHashMap;
    private int pixelScaleInNanometer;
    private ImagePlus edgeBinaryMaskImagePlus;
    private ImagePlus segmentedRegionImagePlus;
    private ImagePlus edgeCentroidMaskImgPlus;
    private ImagePlus stackWithVesselEdgeCentroidOverlay;

    public Reconstruction(RandomAccessibleInterval<FloatType> smoothedStack,
                          int width,
                          int height,
                          int radius,
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
        return centroidHashMap;
    }

    public ImagePlus getEdgeBinaryMaskImagePlus() {
        return edgeBinaryMaskImagePlus;
    }

    public ImagePlus getSegmentedRegionImagePlus() {
        return segmentedRegionImagePlus;
    }

    public ImagePlus getEdgeCentroidMaskImgPlus() {
        return edgeCentroidMaskImgPlus;
    }

    public ImagePlus getStackWithVesselEdgeCentroidOverlay() {
        return stackWithVesselEdgeCentroidOverlay;
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
        //TODO: fix the line below, replace the 2.0F and 4.0F with a general approach that capture all the return label
        segmentedImageProcessor.setThreshold((double) 2.0F, (double) 4.0F, ImageProcessor.NO_LUT_UPDATE);
        ThresholdToSelection ts = new ThresholdToSelection();
        Roi maskRoi = ts.convert(segmentedImage.getProcessor());
//        segmentedImage.setOverlay(new Overlay(maskRoi));
        // Apply overlay to original image
        return new Overlay(maskRoi);

    }

    public ImagePlus processWholeStack() throws IOException {
//        System.err.println("Just start the function- coordinateoutside " + coordinatesOutside);
        ImageStack finalStack = new ImageStack(width, height);
        ImageStack segmentedRegionsStack = new ImageStack(width, height);
        ImageStack edgesStack = new ImageStack(width, height);
        ImageStack edgesCentroidStack = new ImageStack(width, height);
        ImageStack stackWithVesselEdgeCentroidOverlayStack = new ImageStack(width, height);

        ArrayList<Point> startingClick = new ArrayList<>();
        // Track centroids by ID across slices
        HashMap<Integer, ArrayList<Point>> centroidMap = new HashMap<>();
        centroidMap.put(1,new ArrayList<>());
        centroidMap.put(2,new ArrayList<>());
        for (Point p : coordinatesOutside) {
            startingClick.add(new Point(p.x, p.y));
        }
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
//        System.err.println("starting click: " + startingClick);
        for (int currentSlice = 0; currentSlice < smoothedStack.dimension(2); currentSlice++) { // 2 is the z channel
            this.setNewCurrentSlice(currentSlice);// this is used for update the currentProgress of this method for outer class who calls this class. Used for updating progressBar.
//            System.err.println("loop: " + currentSlice);
            ArrayList<Point> currentClick = new ArrayList<>();
            if(currentSlice==0){
                // for the first iteration, we use the input from user
                currentClick=deepCopyPoints(startingClick);
                for (Integer key : centroidMap.keySet()){
                    centroidMap.get(key).add((currentClick.get(key-1)));
                }
                currentClick.add(pointForBackground);
//                System.err.println("current Click after added points: " + currentClick);
            } else{
                // for subsequence iteration we use the previous centroid
                ArrayList<Point> list1 = centroidMap.get(1);
                ArrayList<Point> list2 = centroidMap.get(2);
                currentClick.add(list1.get(list1.size()-1));
                currentClick.add(list2.get(list2.size()-1));
                currentClick.add(pointForBackground);
//                System.err.println("current Click after added points: " + currentClick);
            }
            // perform segmentation
            RandomAccessibleInterval<FloatType> slice2D = Views.hyperSlice(smoothedStack, 2, currentSlice); // dimension 2 is Z
            SegmentationExtendMinimaWaterShed semws = new SegmentationExtendMinimaWaterShed(currentClick,
                    slice2D,
                    width,
                    height,
                    radius,
                    pixelScaleInNanometer);
            ImagePlus segmentedImage = semws.performSegmentation();
            ImagePlus originalSliceImagePlus = semws.getInputSliceImagePlus();
            // identify the centroid with current segmentation
            ArrayList<Point> allSegmentedCentroids = centroidsCoordinatesGivenSegmentedImage(
                    segmentedImage);
//            System.err.println("all segmented centroid: " + allSegmentedCentroids);
            if(currentSlice == 0){ // if this is the first iteration
                HashMap<Integer, Point> mappedCurrentCentroids = mapCentroidToIDs(centroidMap, allSegmentedCentroids);
                // add the mappedCurrentCentroid to the centroidMap
                for(Integer key : mappedCurrentCentroids.keySet()){
                    centroidMap.get(key).add(mappedCurrentCentroids.get(key));
                }
//                System.err.println("centroid Map after first iteration: " + centroidMap);
            } else { // If this is not first iteration
                // map new centroids to ID
                HashMap<Integer, Point> mappedCurrentCentroids = mapCentroidToIDs(centroidMap, allSegmentedCentroids);
//                System.err.println("Mapped Current Centroids: " + mappedCurrentCentroids);
                // select the label based on the centroid coordinate
                int[] desiredLabels = getTheTrueLabelGivenCoordinate(segmentedImage,mappedCurrentCentroids);
//                System.err.println("desiredLabels: " + Arrays.toString(desiredLabels));
                // get the image with only the desired region
                ImagePlus onlyDesiredRegionLabelImage = extractDesiredLabels(segmentedImage,desiredLabels);
                segmentedRegionsStack.addSlice(onlyDesiredRegionLabelImage.getProcessor());
                // get edge binary mask
                ImagePlus edgeBinaryMask = extractEdgesFromBinary(onlyDesiredRegionLabelImage);
                // binaryMask with both edges and centroids
                ImageProcessor edgeCentroidMaskProcessor = edgeBinaryMask.getProcessor().duplicate();
                // add centroid mask
                for (Integer key : mappedCurrentCentroids.keySet()) {
                    edgeCentroidMaskProcessor.putPixel((int)mappedCurrentCentroids.get(key).getX(),
                            (int)mappedCurrentCentroids.get(key).getY(), 255);
                }
                // add slice to result stack
                edgesStack.addSlice(edgeBinaryMask.getProcessor());
                edgesCentroidStack.addSlice(edgeCentroidMaskProcessor);
                //Create Overlay with edgeCentroid Mask
                edgeCentroidMaskProcessor.setThreshold(255,255);
                ThresholdToSelection ts = new ThresholdToSelection();
                Roi edgeCentroidRoi = ts.convert(edgeCentroidMaskProcessor);
                Overlay edgeCentroidOverlay = new Overlay(edgeCentroidRoi);
                originalSliceImagePlus.getProcessor().setOverlay(edgeCentroidOverlay);
                stackWithVesselEdgeCentroidOverlayStack.addSlice(originalSliceImagePlus.getProcessor());
                // add the mappedCurrentCentroid to the centroidMap
                for(Integer key : mappedCurrentCentroids.keySet()){
                    centroidMap.get(key).add(mappedCurrentCentroids.get(key));
                }
            }
            currentClick.clear();
            // add the image to the stack
            finalStack.addSlice(segmentedImage.getProcessor());
        }
        this.segmentedRegionImagePlus = new ImagePlus("binary image of segmented vessels", segmentedRegionsStack);
        this.edgeBinaryMaskImagePlus = new ImagePlus("binary mask of edge", edgesStack);
        this.edgeCentroidMaskImgPlus = new ImagePlus("binary mask of edge and centroid", edgesCentroidStack);
        this.stackWithVesselEdgeCentroidOverlay = new ImagePlus("Image stack with edge and Centroid segmentation Overlay", stackWithVesselEdgeCentroidOverlayStack);
        stackWithVesselEdgeCentroidOverlay.updateAndDraw();
        this.centroidHashMap = centroidMap;
        return new ImagePlus("Final Stack", finalStack);
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

    //    private Point nextClickGivenCurrentAndPredictedCentroid(Point startingClick,
//                                                      Point startingCentroid,
//                                                      Point predictedCentroid,
//                                                           int width,
//                                                           int height){
//        VelocityOfPoint velocity = calculateVelocityGiven2Points(predictedCentroid, startingCentroid);
//        return nextPointGivenVelocity(startingClick,velocity,width,height);
//    }
    private Point nextClickGivenCurrentAndPredictedCentroid(Point currentClick,
                                                            Point currenCentroid,
                                                            Point predictedCentroid,
                                                            int width,
                                                            int height) {
        VelocityOfPoint velocity = calculateVelocityGiven2Points(predictedCentroid, currenCentroid);
        return nextPointGivenVelocity(currentClick, velocity, width, height);
    }

    private Point nextPointGivenCurrentAndPreviousPoint(Point currentPoint,
                                                        Point previousPoint) {
        VelocityOfPoint velocity = calculateVelocityGiven2Points(currentPoint, previousPoint);
        return nextPointGivenVelocity(currentPoint, velocity, width, height);
    }

    private VelocityOfPoint calculateVelocityGiven2Points(Point currentPoint, Point prevPoint) {
        int xVelocity = currentPoint.x - prevPoint.x;
        int yVelocity = currentPoint.y - prevPoint.y;
        return new VelocityOfPoint(xVelocity, yVelocity);
    }

    private Point nextPointGivenVelocity(Point currentPoint, VelocityOfPoint velocity, int imageWidth, int imageHeight) {
        return new Point(nextCoorGivenVelocity1D(currentPoint.x, velocity.getxVelocity(), imageWidth, 0),
                nextCoorGivenVelocity1D(currentPoint.y, velocity.getyVelocity(), imageHeight, 0));
    }

    private int nextCoorGivenVelocity1D(int position1D, int velocity1D, int upperBound, int lowerBound) {
        int nextPosition;
        if ((position1D + velocity1D) < lowerBound) {
            nextPosition = lowerBound;
        } else if ((position1D + velocity1D) > upperBound) {
            nextPosition = upperBound;
        } else {
            nextPosition = position1D + velocity1D;
        }
        return nextPosition;
    }

    private class VelocityOfPoint {
        private int xVelocity;
        private int yVelocity;

        public VelocityOfPoint(int xVelocity, int yVelocity) {
            this.xVelocity = xVelocity;
            this.yVelocity = yVelocity;
        }

        public int getxVelocity() {
            return xVelocity;
        }

        public int getyVelocity() {
            return yVelocity;
        }

        public void setxVelocity(int xVelocity) {
            this.xVelocity = xVelocity;
        }

        public void setyVelocity(int yVelocity) {
            this.yVelocity = yVelocity;
        }
    }

    public static ArrayList<Point> deepCopyPoints(ArrayList<Point> points) {
        ArrayList<Point> copy = new ArrayList<>();
        for (Point p : points) {
            copy.add(new Point(p.x, p.y));
        }
        return copy;
    }

    public static ArrayList<ArrayList<Point>> deepCopyNestedPoints(ArrayList<ArrayList<Point>> original) {
        ArrayList<ArrayList<Point>> copy = new ArrayList<>();
        for (ArrayList<Point> innerList : original) {
            ArrayList<Point> innerCopy = new ArrayList<>();
            for (Point p : innerList) {
                innerCopy.add(new Point(p.x, p.y));
            }
            copy.add(innerCopy);
        }
        return copy;
    }

    public static HashMap<Integer, Point> mapCentroidToIDs(HashMap<Integer, ArrayList<Point>> centroidsHashMap,
                                                           ArrayList<Point> centroidCaptured) {
        HashMap<Integer, Point> result = new HashMap<>();

        // Handle edge cases
        if (centroidsHashMap == null || centroidCaptured == null || centroidCaptured.isEmpty()) {
            return result;
        }

        for (Integer id : centroidsHashMap.keySet()) {
            ArrayList<Point> recordedCentroids = centroidsHashMap.get(id);
            if (recordedCentroids == null || recordedCentroids.isEmpty()) {
                continue;  // Skip IDs with no centroids
            }

            Point lastCentroid = recordedCentroids.get(recordedCentroids.size() - 1);
            Point closestCentroid = null;
            int minDistance = Integer.MAX_VALUE;

            for (Point current : centroidCaptured) {
                int distance = manhattanDistance(lastCentroid, current);
                if (distance < minDistance) {
                    minDistance = distance;
                    closestCentroid = current;
                }
            }

            if (closestCentroid != null) {
                result.put(id, closestCentroid);
            }
        }

        return result;
    }

    public  static Integer manhattanDistance (Point referencePoint, Point measurePoint){
        int refX = (int) referencePoint.getX();
        int refY = (int) referencePoint.getY();
        int msX = (int) measurePoint.getX();
        int msY = (int) measurePoint.getY();
        return Math.abs(refX - msX) + Math.abs(refY-msY);
    }

    public static int[] getTheTrueLabelGivenCoordinate(ImagePlus labelImage, HashMap<Integer, Point> centroidCoordinates) {
        ImageProcessor labelProcessor = labelImage.getProcessor();
        int[] labels = new int[centroidCoordinates.size()];
        int index = 0;
        for (Point p : centroidCoordinates.values()) {
            labels[index++] = (int) labelProcessor.getf(p.x, p.y);
        }
        return labels;
    }

    public static ImagePlus extractDesiredLabels(ImagePlus labeledImage, int[] desiredLabels) {
        ImageProcessor labelProcessor = labeledImage.getProcessor();
        int width = labelProcessor.getWidth();
        int height = labelProcessor.getHeight();
        // Create binary output
        ByteProcessor binaryProcessor = new ByteProcessor(width, height);
        Set<Integer> labelSet = Arrays.stream(desiredLabels).boxed().collect(Collectors.toSet());
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
        eroded = Morphology.erosion(bp,strel);
        // perform original - erosion to get edge
        ImageProcessor edgeMaskProcessor = ImageCalculator.combineImages(bp,eroded, ImageCalculator.Operation.MINUS);
        return new ImagePlus("Edges", edgeMaskProcessor);
    }

    private static ImagePlus applyMask(ImagePlus inputImage, ImagePlus binaryMask){
        ImagePlus result = inputImage.duplicate();
        result.getProcessor().copyBits(binaryMask.getProcessor(), 0,0,Blitter.AND);
        result.resetDisplayRange();
        result.setTitle("applied mask");
        return result;
    }



}
