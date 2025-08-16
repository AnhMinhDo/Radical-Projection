package schneiderlab.tools.radialprojection.controllers.uiaction.mainwindow;

import schneiderlab.tools.radialprojection.models.czitotifmodel.CziToTifModel;
import schneiderlab.tools.radialprojection.models.radialprojection.VesselsSegmentationModel;
import schneiderlab.tools.radialprojection.views.userinterfacecomponents.Radical_Projection_Tool;
import ij.Prefs;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

public class AddSavingActionWhenMainWindowClosed implements WindowListener {
    private final CziToTifModel cziToTifModel;
    private final VesselsSegmentationModel vesselsSegmentationModel;

    public AddSavingActionWhenMainWindowClosed(CziToTifModel cziToTifModel,
                                               VesselsSegmentationModel vesselsSegmentationModel) {
        this.cziToTifModel=cziToTifModel;
        this.vesselsSegmentationModel=vesselsSegmentationModel;
    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {
        //TODO: save parameter from models to Prefs of Imagej/Fiji
        Prefs.set("RadialProjection.CziToTifModel.dirPath",cziToTifModel.getDirPath());
        Prefs.set("RadialProjection.CziToTifModel.isBgSub",cziToTifModel.isBgSub());
        Prefs.set("RadialProjection.CziToTifModel.rollingValue",cziToTifModel.getRollingValue());
        Prefs.set("RadialProjection.CziToTifModel.saturationValue",cziToTifModel.getSaturationValue());
        Prefs.set("RadialProjection.CziToTifModel.isRotate",cziToTifModel.isRotate());
        Prefs.set("RadialProjection.CziToTifModel.rotateDirection",cziToTifModel.getRotateDirection().toString());
        Prefs.set("RadialProjection.VesselsSegmentationModel.xyPixelSize", vesselsSegmentationModel.getXyPixelSize());
        Prefs.set("RadialProjection.VesselsSegmentationModel.zPixelSize", vesselsSegmentationModel.getzPixelSize());
        Prefs.set("RadialProjection.VesselsSegmentationModel.analysisWindow", vesselsSegmentationModel.getAnalysisWindow());
        Prefs.set("RadialProjection.VesselsSegmentationModel.smoothingSigma", vesselsSegmentationModel.getSmoothingSigma());
        Prefs.set("RadialProjection.VesselsSegmentationModel.sliceIndexForTuning", vesselsSegmentationModel.getSliceIndexForTuning());
        Prefs.set("RadialProjection.VesselsSegmentationModel.innerVesselRadius", vesselsSegmentationModel.getInnerVesselRadius());
        Prefs.set("RadialProjection.VesselsSegmentationModel.celluloseToLigninRatio", vesselsSegmentationModel.getCelluloseToLigninRatio());
    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }
}
