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
    private final double radius;
    private final RandomAccessibleInterval<FloatType> smoothedStack;
    private Point pointForBackground;
    private int slideForTuning;
    private HashMap<Integer, ArrayList<Point>> centroidHashMap;
    private int pixelScaleInNanometer;
    private ImagePlus edgeBinaryMaskImagePlus;
    private ImagePlus segmentedRegionImagePlus;
    private ImagePlus edgeCentroidMaskImgPlus;
    private ImagePlus stackWithVesselEdgeCentroidOverlay;

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

//    public ImagePlus getStackWithVesselEdgeCentroidOverlay() {
//        return stackWithVesselEdgeCentroidOverlay;
//    }

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

    public ImagePlus processWholeStack() {
//        System.err.println("Just start the function- coordinateoutside " + coordinatesOutside);
        ImageStack finalStack = new ImageStack(width, height);
        ImageStack segmentedRegionsStack = new ImageStack(width, height);
        ImageStack edgesStack = new ImageStack(width, height);
        ImageStack edgesCentroidStack = new ImageStack(width, height);
//        ImageStack stackWithVesselEdgeCentroidOverlayStack = new ImageStack(width, height);
        ArrayList<Point> startingClick = new ArrayList<>();
        // Track centroids by ID across slices, this HashMap store the true centroid(if available for that vessel), if there is none in a slice, the value will be null, unlike the clickMap below, which will store the previous centroid as a replacement if no centroid is identified for current vessel
        HashMap<Integer, ArrayList<Point>> centroidMap = new HashMap<>();
        centroidMap.put(1, new ArrayList<>()); // TODO: hard coded the ID might not be a good idea, how to deal with when there are more than 2, think of a better approach
        centroidMap.put(2, new ArrayList<>());
        for (Point p : coordinatesOutside) {
            startingClick.add(new Point(p.x, p.y));
        }
        // hashMap for tracking the click, this HashMap is used for the algorithm as the starting point for watershed segmentation
        HashMap<Integer, ArrayList<Point>> clickMap = new HashMap<>();
        clickMap.put(1, new ArrayList<>()); // TODO: hard coded the ID might not be a good idea, how to deal with when there are more than 2, think of a better approach
        clickMap.put(2, new ArrayList<>());
        // hashMap for tracking the true labels of each ID, will be used when one of the ID fail to be segmented and the previous segmentation is used
        HashMap<Integer, ArrayList<Integer[]>> trueLabelMap = new HashMap<>(); // Important: the int[] store only 2 values, the first for the segmented-labeled-image-slice-index, and the second for the true label in that segmented-labeled-image
        trueLabelMap.put(1, new ArrayList<>()); // TODO: hard coded the ID might not be a good idea, how to deal with when there are more than 2, think of a better approach
        trueLabelMap.put(2, new ArrayList<>());
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
            ArrayList<Point> currentClick = new ArrayList<>();
            // the below if-else is to prepare the "clicks" for this slice
            if (currentSlice == 0) {
                // for the first iteration, we use the input from user
                currentClick = deepCopyPoints(startingClick);
                for (Integer key : centroidMap.keySet()) { // the clicks are considered centroids for the first slide and added to the centroidMap
                    centroidMap.get(key).add((currentClick.get(key - 1)));
                }
                for (Integer key : clickMap.keySet()) { // // the clicks are added to the click HashMap
                    clickMap.get(key).add((currentClick.get(key - 1)));
                }
                currentClick.add(pointForBackground);
//                System.err.println("current Click after added points: " + currentClick);
            } else {
//                ArrayList<Point> list1 = centroidMap.get(1);
//                ArrayList<Point> list2 = centroidMap.get(2);
                ArrayList<Point> list1 = clickMap.get(1); // the clicks are predicted at the end of each loop, here these coordinates are simply retrieved from the arrayList
                ArrayList<Point> list2 = clickMap.get(2);
                currentClick.add(list1.get(list1.size() - 1)); // current click is empty at the end of each loop, so do not need to empty it again that the start of new loop
                currentClick.add(list2.get(list2.size() - 1));
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
//            ImagePlus originalSliceImagePlus = semws.getInputSliceImagePlus();
            // identify the centroid with current segmentation
            ArrayList<Point> allSegmentedCentroids = centroidsCoordinatesGivenSegmentedImage(
                    segmentedImage);
//            System.err.println("all segmented centroid: " + allSegmentedCentroids);
            if (currentSlice == 0) { // if this is the first iteration
                HashMap<Integer, Point> mappedCurrentCentroids = mapCentroidToIDs(clickMap, allSegmentedCentroids);
                // get the true label of each ID
                HashMap<Integer, Integer> desiredLabelsWithIdMap = getTheTrueLabelGivenCoordinate(segmentedImage, mappedCurrentCentroids);
                // create the image with only the desired label
                ImagePlus onlyDesiredRegionLabelImage = extractDesiredLabels(segmentedImage, desiredLabelsWithIdMap);
                segmentedRegionsStack.addSlice(onlyDesiredRegionLabelImage.getProcessor());
                // get the edge mask using the true label
                ImagePlus edgeBinaryMask = extractEdgesFromBinary(onlyDesiredRegionLabelImage);
                edgesStack.addSlice(edgeBinaryMask.getProcessor());
//                // create Edge-Centroid mask
//                ImageProcessor edgeCentroidMaskProcessor = edgeBinaryMask.getProcessor().duplicate();
//                // add centroid to mask
//                for (Integer key : mappedCurrentCentroids.keySet()) {
//                    edgeCentroidMaskProcessor.putPixel((int)mappedCurrentCentroids.get(key).getX(),
//                            (int)mappedCurrentCentroids.get(key).getY(), 255);
//                }
                ImageProcessor edgeCentroidMaskProcessor = createEdgeCentroidMask(edgeBinaryMask, mappedCurrentCentroids);
                edgesCentroidStack.addSlice(edgeCentroidMaskProcessor);
                // update centroidMap, clickMap and trueLabel
                // save the true label, slice index and its ID to hashMap
                addTrueLabelToHashMap(desiredLabelsWithIdMap,
                        trueLabelMap,
                        currentSlice);
                // add the mappedCurrentCentroid to the centroidMap and clickMap
                updateClickMap(mappedCurrentCentroids,clickMap);
                updateCentroidMap(mappedCurrentCentroids,centroidMap);
//                for (Integer key : mappedCurrentCentroids.keySet()) {
//                    centroidMap.get(key).add(mappedCurrentCentroids.get(key));
//                }
//                for (Integer key : mappedCurrentCentroids.keySet()) {
//                    clickMap.get(key).add(mappedCurrentCentroids.get(key));
//                }
//                System.err.println("centroid Map after first iteration: " + centroidMap);
            } else { // If this is not first iteration
                // map new centroids to ID
                HashMap<Integer, Point> mappedCurrentCentroids = mapCentroidToIDs(clickMap, allSegmentedCentroids);
//                System.err.println("Mapped Current Centroids: " + mappedCurrentCentroids);
                // select the label based on the centroid coordinate
                HashMap<Integer, Integer> desiredLabelsWithIdMap = getTheTrueLabelGivenCoordinate(segmentedImage, mappedCurrentCentroids);
//                System.err.println("desiredLabels: " + Arrays.toString(desiredLabels));
                // get the image with only the desired region
                ImagePlus onlyDesiredRegionLabelImage = extractDesiredLabels(segmentedImage, desiredLabelsWithIdMap);
                segmentedRegionsStack.addSlice(onlyDesiredRegionLabelImage.getProcessor());
                // get edge binary mask
                ImagePlus edgeBinaryMask = extractEdgesFromBinary(onlyDesiredRegionLabelImage);
                // add the desiredLabelsWithIdMap to trueLabelMap
                addTrueLabelToHashMap(desiredLabelsWithIdMap, trueLabelMap, currentSlice);
                // check the mappedCurrentCentroid for null values, If there are, reuse the previous edge for this vessel and update the mappedCurrentCentroids,trueLabelMap
                for (Integer id : mappedCurrentCentroids.keySet()) {
                    if (mappedCurrentCentroids.get(id) == null) {
                        ArrayList<Integer[]> trueInfoOfSegmentedRegion = trueLabelMap.get(id);
                        Integer[] infoOfPreviousSlice = trueInfoOfSegmentedRegion.get(trueInfoOfSegmentedRegion.size() - 2);// get the info of the segmented region of this ID from previous slice and use it for this slice, the first number is the slice index, the second number is the label number, -2 because -1 for shift due to array has base 0, -1 again because we update the trueLabelMap earlier than this step
                        int sliceIdx = infoOfPreviousSlice[0];
                        int labelOfVessel = infoOfPreviousSlice[1];
                        // get this vessel ID with only the true label
                        ImageProcessor imageProcessor = finalStack.getProcessor(sliceIdx);
                        // create input for function below
                        HashMap<Integer, Integer> labelHashMap = new HashMap<>();
                        labelHashMap.put(id, labelOfVessel);
                        // get the Image with only the labeled region
                        ImagePlus imgOfTheAboveLabel = extractDesiredLabels(
                                new ImagePlus("segmented Image", imageProcessor),
                                labelHashMap);
                        // extract the edge
                        ImagePlus edgeMaskOfVessel = extractEdgesFromBinary(imgOfTheAboveLabel);
                        // scan the edge Mask and write the white pixel to the edgeBinaryMask
                        byte[] edgeMaskOfVesselPixelArray = (byte[]) edgeMaskOfVessel.getProcessor().getPixels();
                        byte[] edgeBinaryMaskPixelArray = (byte[]) edgeBinaryMask.getProcessor().getPixels();
                        for (int i = 0; i < edgeMaskOfVesselPixelArray.length; i++) {
                            if ((edgeMaskOfVesselPixelArray[i] & 0xFF) != 0) {
                                edgeBinaryMaskPixelArray[i] = (byte) 255;
                            }
                        }
                        // update the new centroid for this vessel
                        Point centroid = centroidMap.get(id).get(sliceIdx);
                        mappedCurrentCentroids.put(id, centroid); // update to ensure there is no null
                        // fix the trueLabelMap
                        replaceNullWithNewValueInTrueLabelHashMap(trueLabelMap,
                                id,
                                labelOfVessel,
                                sliceIdx);
                    }
                }
                // add edge mask to the stack
                edgesStack.addSlice(edgeBinaryMask.getProcessor());
                // binaryMask with both edges and centroids
                ImageProcessor edgeCentroidMaskProcessor = edgeBinaryMask.getProcessor().duplicate();
                // add centroid mask
                for (Integer key : mappedCurrentCentroids.keySet()) {
                    edgeCentroidMaskProcessor.putPixel((int) mappedCurrentCentroids.get(key).getX(),
                            (int) mappedCurrentCentroids.get(key).getY(), 255);
                }
                edgesCentroidStack.addSlice(edgeCentroidMaskProcessor);
//                //Create Overlay with edgeCentroid Mask
//                edgeCentroidMaskProcessor.setThreshold(255,255);
//                ThresholdToSelection ts = new ThresholdToSelection();
//                Roi edgeCentroidRoi = ts.convert(edgeCentroidMaskProcessor);
//                Overlay edgeCentroidOverlay = new Overlay(edgeCentroidRoi);
//                originalSliceImagePlus.getProcessor().setOverlay(edgeCentroidOverlay);
//                stackWithVesselEdgeCentroidOverlayStack.addSlice(originalSliceImagePlus.getProcessor());
                // add the mappedCurrentCentroid to the clickMap and centroidMap
                for (Integer key : mappedCurrentCentroids.keySet()) {
                    clickMap.get(key).add(mappedCurrentCentroids.get(key));
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
//        this.stackWithVesselEdgeCentroidOverlay = new ImagePlus("Image stack with edge and Centroid segmentation Overlay", stackWithVesselEdgeCentroidOverlayStack);
//        stackWithVesselEdgeCentroidOverlay.updateAndDraw();
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
//    private Point nextClickGivenCurrentAndPredictedCentroid(Point currentClick,
//                                                            Point currenCentroid,
//                                                            Point predictedCentroid,
//                                                            int width,
//                                                            int height) {
//        VelocityOfPoint velocity = calculateVelocityGiven2Points(predictedCentroid, currenCentroid);
//        return nextPointGivenVelocity(currentClick, velocity, width, height);
//    }

//    private Point nextPointGivenCurrentAndPreviousPoint(Point currentPoint,
//                                                        Point previousPoint) {
//        VelocityOfPoint velocity = calculateVelocityGiven2Points(currentPoint, previousPoint);
//        return nextPointGivenVelocity(currentPoint, velocity, width, height);
//    }

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

//        public void setxVelocity(int xVelocity) {
//            this.xVelocity = xVelocity;
//        }

//        public void setyVelocity(int yVelocity) {
//            this.yVelocity = yVelocity;
//        }
    }

    public static ArrayList<Point> deepCopyPoints(ArrayList<Point> points) {
        ArrayList<Point> copy = new ArrayList<>();
        for (Point p : points) {
            copy.add(new Point(p.x, p.y));
        }
        return copy;
    }

//    public static ArrayList<ArrayList<Point>> deepCopyNestedPoints(ArrayList<ArrayList<Point>> original) {
//        ArrayList<ArrayList<Point>> copy = new ArrayList<>();
//        for (ArrayList<Point> innerList : original) {
//            ArrayList<Point> innerCopy = new ArrayList<>();
//            for (Point p : innerList) {
//                innerCopy.add(new Point(p.x, p.y));
//            }
//            copy.add(innerCopy);
//        }
//        return copy;
//    }

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
//        for (Point p : centroidCoordinates.values()) {
//            labels[index++] = (int) labelProcessor.getf(p.x, p.y);
//        }

//        int[] labels = new int[labelArrayList.size()];
//        for (int i = 0; i < labels.length; i++) {
//            labels[i] = labelArrayList.get(i);
//        }
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

//    private static ImagePlus applyMask(ImagePlus inputImage, ImagePlus binaryMask){
//        ImagePlus result = inputImage.duplicate();
//        result.getProcessor().copyBits(binaryMask.getProcessor(), 0,0,Blitter.AND);
//        result.resetDisplayRange();
//        result.setTitle("applied mask");
//        return result;
//    }

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




}
