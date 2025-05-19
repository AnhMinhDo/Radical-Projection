package RadicalProjectionMain.Segmentation;

import ij.process.ImageProcessor;
import ij.process.FloatProcessor;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Standalone implementation of Chamfer distance transform using Borgefors weights (3,4).
 * Computes 2D distance maps on binary images using floating-point precision.
 */
public class StandaloneChamferDistanceTransform {

    public static FloatProcessor compute(ImageProcessor binaryImage, boolean normalize) {
        int w = binaryImage.getWidth();
        int h = binaryImage.getHeight();
        FloatProcessor dist = new FloatProcessor(w, h);

        // Initialize
        for (int i = 0; i < w*h; i++) {
            dist.setf(i, binaryImage.getf(i) > 0 ? Float.MAX_VALUE : 0);
        }

        // Forward pass
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                if (binaryImage.getf(x, y) == 0) continue;
                updateDistance(x, y, -1, 0, 3f, dist, binaryImage);
                updateDistance(x, y, 0, -1, 3f, dist, binaryImage);
                updateDistance(x, y, -1, -1, 4f, dist, binaryImage);
                updateDistance(x, y, 1, -1, 4f, dist, binaryImage);
            }
        }

        // Backward pass
        for (int y = h-1; y >= 0; y--) {
            for (int x = w-1; x >= 0; x--) {
                if (binaryImage.getf(x, y) == 0) continue;
                updateDistance(x, y, 1, 0, 3f, dist, binaryImage);
                updateDistance(x, y, 0, 1, 3f, dist, binaryImage);
                updateDistance(x, y, -1, 1, 4f, dist, binaryImage);
                updateDistance(x, y, 1, 1, 4f, dist, binaryImage);
            }
        }

        // Normalize if needed
        if (normalize) {
            for (int i = 0; i < w*h; i++) {
                if (binaryImage.getf(i) > 0) dist.setf(i, dist.getf(i)/3f);
            }
        }

        return dist;
    }

    private static void updateDistance(int x, int y, int dx, int dy, float weight,
                                       FloatProcessor dist, ImageProcessor binary) {
        int x2 = x + dx, y2 = y + dy;
        if (x2 < 0 || x2 >= dist.getWidth() || y2 < 0 || y2 >= dist.getHeight()) return;

        float newDist = binary.getf(x2, y2) == 0 ? weight : dist.getf(x2, y2) + weight;
        if (newDist < dist.getf(x, y)) dist.setf(x, y, newDist);
    }

}
