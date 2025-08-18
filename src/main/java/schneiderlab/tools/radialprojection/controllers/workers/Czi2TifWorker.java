package schneiderlab.tools.radialprojection.controllers.workers;

import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.CZIProcessor;
import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.RotateDirection;

import javax.swing.*;

public class Czi2TifWorker extends SwingWorker <Void, Void>{
    private final String folderPath;
    private final boolean backgroundSubtraction;
    private final int rolling;
    private final int saturated;
    private final boolean isRotate;
    private final RotateDirection rotateDirection;

    public Czi2TifWorker(String folderPath,
                         boolean backgroundSubtraction,
                         int rolling,
                         int saturated,
                         boolean isRotate,
                         RotateDirection rotateDirection) {
        this.folderPath = folderPath;
        this.backgroundSubtraction = backgroundSubtraction;
        this.rolling = rolling;
        this.saturated = saturated;
        this.isRotate = isRotate;
        this.rotateDirection = rotateDirection;
    }

    @Override
    protected Void doInBackground() {
        CZIProcessor.convertingCZItoTIFF(folderPath,
                backgroundSubtraction,
                rolling,
                saturated,
                isRotate,
                rotateDirection,
                false);
        return null;
    }
}
