package schneiderlab.tools.radialprojection.imageprocessor.core.utils;

import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;

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

    public static ImagePlus copyAndConvertRandomAccessIntervalToImagePlus(RandomAccessibleInterval<FloatType> input, String name){
        // Create copy using cursors
        Img<FloatType> copy = ArrayImgs.floats(Intervals.dimensionsAsLongArray(input));
        net.imglib2.Cursor<FloatType> srcCursor = Views.flatIterable(input).cursor();
        net.imglib2.Cursor<FloatType> dstCursor = copy.cursor();
        while (srcCursor.hasNext()) {
            dstCursor.next().set(srcCursor.next());
        }
        // Convert to ImagePlus
        ImagePlus impFloat = ImageJFunctions.wrap(copy, name);
        impFloat.resetDisplayRange();
        return impFloat;
    }
}
