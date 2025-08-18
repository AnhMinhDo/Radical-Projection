//package schneiderlab.tools.radialprojection.controllers.uiaction;
//
//import net.imagej.ImgPlus;
//import net.imglib2.img.display.imagej.ImageJFunctions;
//import net.imglib2.type.numeric.integer.UnsignedShortType;
//import schneiderlab.tools.radialprojection.controllers.workers.ProjectionAndSmoothingWorker;
//import schneiderlab.tools.radialprojection.views.userinterfacecomponents.Radical_Projection_Tool;
//
//import javax.naming.Context;
//import javax.swing.*;
//import java.awt.event.ActionEvent;
//import java.awt.event.ActionListener;
//import java.beans.PropertyChangeEvent;
//import java.beans.PropertyChangeListener;
//
//public class ButtonProjectionAndSmoothing implements ActionListener {
//    private JTextField statusbar;
//    private ImgPlus<UnsignedShortType> sideView;
//    private int ligninToCelluloseWeight;
//    private double sigmaValueFilter;
//    private int radius;
//    private Context context;
//
//    @Override
//    public void actionPerformed(ActionEvent e) {
//        mainView.getTextField2StatusVesselSegmentation().setText("Creating hybrid stack and smoothing...");
//        ProjectionAndSmoothingWorker pasw = new ProjectionAndSmoothingWorker(sideView,
//                mainView.getSliderHybridWeight().getValue(),
//                (int)mainView.getSpinnerAnalysisWindow().getValue(),
//                (double) mainView.getSpinnerPreWatershedSmoothing().getValue(),
//                (int) mainView.getSpinnerInnerVesselRadius().getValue(),
//                context);
//        pasw.addPropertyChangeListener(new PropertyChangeListener() {
//            @Override
//            public void propertyChange(PropertyChangeEvent evt) {
//                if ("state".equals(evt.getPropertyName()) &&
//                        evt.getNewValue() == SwingWorker.StateValue.DONE){
//                    hybridStackNonSmoothed = pasw.getHybridStackNonSmoothed();
//                    hybridStackSmoothed = pasw.getHybridStackSmoothed();
//                    hybridStackSmoothedWidth = pasw.getWidth();
//                    hybridStackSmoothedHeight = pasw.getHeight();
//                    // show result to user
//                    ImageJFunctions.show(hybridStackNonSmoothed);
//                    ImageJFunctions.show(hybridStackSmoothed);
//                    // update UI
//                    mainView.getTextField2StatusVesselSegmentation().setText("Complete Projection and Smoothing");
//                    mainView.getButtonSelectCentroid().setEnabled(true);
//                }
//            }
//        });
//        pasw.execute();
//    }
//}
