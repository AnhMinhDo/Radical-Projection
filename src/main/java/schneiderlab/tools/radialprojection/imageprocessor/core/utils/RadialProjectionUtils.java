package schneiderlab.tools.radialprojection.imageprocessor.core.utils;

import java.awt.*;
import java.util.ArrayList;

public class RadialProjectionUtils {

    public static ArrayList<Point> deepCopyPoints(ArrayList<Point> points) {
        ArrayList<Point> copy = new ArrayList<>(points.size());
        for (Point p : points) {
            copy.add(new Point(p.x, p.y));
        }
        return copy;
    }
}
