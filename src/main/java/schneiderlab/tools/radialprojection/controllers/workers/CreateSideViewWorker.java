package schneiderlab.tools.radialprojection.controllers.workers;

import net.imagej.ImgPlus;
import net.imagej.axis.Axes;
import net.imagej.axis.DefaultLinearAxis;
import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import org.scijava.Context;
import schneiderlab.tools.radialprojection.imageprocessor.core.createsideview.CreateSideView;

import javax.swing.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;

public class CreateSideViewWorker extends SwingWorker<ImgPlus<UnsignedShortType>, Void> {
    private final int targetXYpixelSize;
    private final int targetZpixelSize;
    private final Path filePath;
    private final Context context;
    private Img<UnsignedShortType> sideViewImg;

    public CreateSideViewWorker(int targetXYpixelSize, int targetZpixelSize, Path filePath, Context context) {
        this.targetXYpixelSize = targetXYpixelSize;
        this.targetZpixelSize = targetZpixelSize;
        this.filePath = filePath;
        this.context = context;
    }

    @Override
    protected ImgPlus<UnsignedShortType> doInBackground() throws Exception {
        CreateSideView createSideView = new CreateSideView(context,
                filePath,
                targetXYpixelSize,
                targetZpixelSize);
        PropertyChangeListener listener = new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                if("progress".equals(evt.getPropertyName())){
                    setProgress((int)evt.getNewValue());
                }
            }
        };
        createSideView.addPropertyChangeListener(listener);
        sideViewImg = createSideView.process();
        ImgPlus<UnsignedShortType> sideView = new ImgPlus<>(sideViewImg);
        // Add meta data
        sideView.setAxis(new DefaultLinearAxis(Axes.X, "µm",targetXYpixelSize*0.001),0);
        sideView.setAxis(new DefaultLinearAxis(Axes.Y, "µm",targetXYpixelSize*0.001),1);
        sideView.setAxis(new DefaultLinearAxis(Axes.Z, "µm",targetZpixelSize*0.001),3);
        sideView.setAxis(new DefaultLinearAxis(Axes.CHANNEL, "",1.0),2);
        return sideView;
    }
}
