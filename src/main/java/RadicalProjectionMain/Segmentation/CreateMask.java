package RadicalProjectionMain.Segmentation;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedByteType;

import java.awt.*;
import java.util.ArrayList;

public class CreateMask {
    private final ArrayList<Point> coordinates;
    private final int width;
    private final int height;
    private final int diameter;
    private final int radius;

    public CreateMask(ArrayList<Point> coordinates,
                      int width,
                      int height,
                      int diameter){
        this.coordinates = coordinates;
        this.width = width;
        this.height = height;
        this.diameter = diameter;
        this.radius = diameter/2;
    }

    public ImagePlus drawMaskWithCoordinate(){
        ImagePlus imp = IJ.createImage("Marker", "8-bit black", width, height, 1);
        ImageProcessor ip = imp.getProcessor();
        ip.setColor(255); // white fill
        for(Point point : coordinates){
            int centerX = (int)point.getX();
            int centerY = (int)point.getY();
            ip.drawDot(centerX,centerY);
//            ip.drawOval(centerX-radius,centerY-radius,diameter,diameter);
            imp.updateAndDraw();
        }
        return imp;
    }
}
