//package schneiderlab.tools.radialprojection.imageprocessor.core.segmentation;
//
//import org.junit.jupiter.api.Test;
//
//import java.awt.*;
//import java.util.ArrayList;
//import java.util.HashMap;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static schneiderlab.tools.radialprojection.imageprocessor.core.segmentation.Reconstruction.mapCentroidToIDs;
//
//class ReconstructionTest {
//
//
//            @Test
//            public void testBasicFunctionality() {
//                // Setup
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                ArrayList<Point> id1Centroids = new ArrayList<>();
//                id1Centroids.add(new Point(10, 10));
//                id1Centroids.add(new Point(15, 15)); // Last centroid for ID 1
//                centroids.put(1, id1Centroids);
//
//                ArrayList<Point> id2Centroids = new ArrayList<>();
//                id2Centroids.add(new Point(30, 30));
//                id2Centroids.add(new Point(35, 35)); // Last centroid for ID 2
//                centroids.put(2, id2Centroids);
//
//                ArrayList<Point> captured = new ArrayList<>();
//                captured.add(new Point(16, 16)); // Closest to ID 1
//                captured.add(new Point(20, 20));
//                captured.add(new Point(36, 36)); // Closest to ID 2
//
//                // Execute
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, captured);
//
//                // Verify
//                assertEquals(2, result.size());
//                assertEquals(new Point(16, 16), result.get(1));
//                assertEquals(new Point(36, 36), result.get(2));
//            }
//
//            @Test
//            public void testEmptyCapturedCentroids() {
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                ArrayList<Point> idCentroids = new ArrayList<>();
//                idCentroids.add(new Point(10, 10));
//                centroids.put(1, idCentroids);
//
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, new ArrayList<>());
//                assertTrue(result.isEmpty());
//            }
//
//            @Test
//            public void testEmptyCentroidsHashMap() {
//                HashMap<Integer, Point> result = mapCentroidToIDs(new HashMap<>(), new ArrayList<>());
//                assertTrue(result.isEmpty());
//            }
//
//            @Test
//            public void testNullInputs() {
//                // Test null centroidsHashMap
//                HashMap<Integer, Point> result1 = mapCentroidToIDs(null, new ArrayList<>());
//                assertTrue(result1.isEmpty());
//
//                // Test null centroidCaptured
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                centroids.put(1, new ArrayList<>());
//                HashMap<Integer, Point> result2 = mapCentroidToIDs(centroids, null);
//                assertTrue(result2.isEmpty());
//            }
//
//            @Test
//            public void testEmptyCentroidListForID() {
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                centroids.put(1, new ArrayList<>()); // Empty list for ID 1
//                ArrayList<Point> id2Centroids = new ArrayList<>();
//                id2Centroids.add(new Point(20, 20));
//                centroids.put(2, id2Centroids);
//
//                ArrayList<Point> captured = new ArrayList<>();
//                captured.add(new Point(21, 21));
//
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, captured);
//                assertEquals(1, result.size());
//                assertEquals(new Point(21, 21), result.get(2));
//                assertFalse(result.containsKey(1));
//            }
//
//            @Test
//            public void testEqualDistance() {
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                ArrayList<Point> idCentroids = new ArrayList<>();
//                idCentroids.add(new Point(10, 10));
//                centroids.put(1, idCentroids);
//
//                ArrayList<Point> captured = new ArrayList<>();
//                captured.add(new Point(12, 12)); // Distance = 4
//                captured.add(new Point(8, 8));   // Distance = 4
//
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, captured);
//                assertEquals(1, result.size());
//                // Should pick the first one with minimum distance (implementation specific)
//                assertTrue(result.get(1).equals(new Point(12, 12)) ||
//                        result.get(1).equals(new Point(8, 8)));
//            }
//
//            @Test
//            public void testSinglePointInCaptured() {
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                ArrayList<Point> id1Centroids = new ArrayList<>();
//                id1Centroids.add(new Point(5, 5));
//                centroids.put(1, id1Centroids);
//
//                ArrayList<Point> id2Centroids = new ArrayList<>();
//                id2Centroids.add(new Point(10, 10));
//                centroids.put(2, id2Centroids);
//
//                ArrayList<Point> captured = new ArrayList<>();
//                captured.add(new Point(6, 6)); // Closer to ID 1
//
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, captured);
//                assertEquals(2, result.size());
//                assertEquals(new Point(6, 6), result.get(1));
//                assertEquals(new Point(6, 6), result.get(2));
//            }
//
//            @Test
//            public void testMultiplePreviousCentroids() {
//                HashMap<Integer, ArrayList<Point>> centroids = new HashMap<>();
//                ArrayList<Point> idCentroids = new ArrayList<>();
//                idCentroids.add(new Point(5, 5));
//                idCentroids.add(new Point(10, 10));
//                idCentroids.add(new Point(15, 15)); // Should use this last one
//                centroids.put(1, idCentroids);
//
//                ArrayList<Point> captured = new ArrayList<>();
//                captured.add(new Point(14, 14)); // Distance = 2 from last centroid
//                captured.add(new Point(16, 16)); // Distance = 2 from last centroid
//                captured.add(new Point(20, 20)); // Distance = 10 from last centroid
//
//                HashMap<Integer, Point> result = mapCentroidToIDs(centroids, captured);
//                assertEquals(1, result.size());
//                // Should pick one of the closest points
//                assertTrue(result.get(1).equals(new Point(14, 14)) ||
//                        result.get(1).equals(new Point(16, 16)));
//            }
//}
