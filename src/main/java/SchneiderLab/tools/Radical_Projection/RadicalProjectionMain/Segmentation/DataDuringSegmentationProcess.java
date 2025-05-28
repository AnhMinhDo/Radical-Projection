package SchneiderLab.tools.Radical_Projection.RadicalProjectionMain.Segmentation;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;

public class DataDuringSegmentationProcess {
    private  int width;
    private  int height;
    private  int diameter;
    private  RandomAccessibleInterval<FloatType> smoothStack;

    public DataDuringSegmentationProcess(RandomAccessibleInterval<FloatType> smoothStack,
                                         int width,
                                         int height,
                                         int diameter) {
        this.smoothStack = smoothStack;
        this.width = width;
        this.height = height;
        this.diameter = diameter;
    }
    public RandomAccessibleInterval<FloatType> getSmoothStack() {
        return smoothStack;
    }

    public int getDiameter() {
        return diameter;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }







}
