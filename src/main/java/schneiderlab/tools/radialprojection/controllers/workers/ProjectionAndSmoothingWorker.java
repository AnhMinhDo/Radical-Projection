package schneiderlab.tools.radialprojection.controllers.workers;

import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import org.scijava.Context;
import schneiderlab.tools.radialprojection.imageprocessor.core.segmentation.CreateHybridStack;

import javax.swing.*;
import java.beans.PropertyChangeListener;

public class ProjectionAndSmoothingWorker extends SwingWorker<RandomAccessibleInterval<FloatType>, Void> {
    private final ImgPlus<UnsignedShortType> sideView;
    private final int ligninToCelluloseWeight;
    private final int windowSizeinMicroMeter;
    private int windowSizeinSlideNumber;
    private final double sigmaValueFilter;
    private double radius;
    private final  Context context;
    private RandomAccessibleInterval<FloatType> hybridStackNonSmoothed;
    private RandomAccessibleInterval<FloatType> hybridStackSmoothed;
    private RandomAccessibleInterval<FloatType> lignin;
    private RandomAccessibleInterval<FloatType> cellulose;
    private int width;
    private int height;

    public ProjectionAndSmoothingWorker(ImgPlus<UnsignedShortType> sideView,
                                        int ligninToCelluloseWeight,
                                        int windowSizeinMicroMeter,
                                        double sigmaValueFilter,
                                        double radius,
                                        Context context) {
        this.sideView = sideView;
        this.ligninToCelluloseWeight = ligninToCelluloseWeight;
        this.windowSizeinMicroMeter = windowSizeinMicroMeter;
        this.sigmaValueFilter = sigmaValueFilter;
        this.radius = radius;
        this.context = context;
    }

    public RandomAccessibleInterval<FloatType> getHybridStackNonSmoothed() {
        return hybridStackNonSmoothed;
    }

    public RandomAccessibleInterval<FloatType> getHybridStackSmoothed() {
        return hybridStackSmoothed;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public RandomAccessibleInterval<FloatType> getLignin() {return lignin;}

    public RandomAccessibleInterval<FloatType> getCellulose() {return cellulose;}

    @Override
    protected RandomAccessibleInterval<FloatType> doInBackground() throws Exception {
        windowSizeinSlideNumber = Math.round(windowSizeinMicroMeter/0.2f);
        CreateHybridStack chs = new CreateHybridStack(context,
                sideView,
                ligninToCelluloseWeight,
                windowSizeinSlideNumber,
                sigmaValueFilter,
                radius);
        PropertyChangeListener listener = evt -> {
            if ("progress".equals(evt.getPropertyName())) {
                setProgress((int) evt.getNewValue());
            }
        };
        chs.addPropertyChangeListener(listener);
        hybridStackSmoothed = chs.process();
//        chs.removePropertyChangeListener(listener);
        hybridStackNonSmoothed = chs.getHybridNonSmoothedStack();
        this.radius = chs.getRadius();
        this.width = chs.getSmoothedStackWidth();
        this.height = chs.getGetSmoothedStackHeight();
        this.cellulose = chs.getCellulose();
        this.lignin = chs.getLignin();
        return hybridStackSmoothed;
    }
}
