package schneiderlab.tools.radialprojection.controllers.controllers;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import net.imagej.ImgPlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Context;
import schneiderlab.tools.radialprojection.controllers.uiaction.*;
import schneiderlab.tools.radialprojection.controllers.workers.*;
import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.RotateDirection;
import schneiderlab.tools.radialprojection.imageprocessor.core.segmentation.Reconstruction;
import schneiderlab.tools.radialprojection.models.radialprojection.CziToTifModel;
import schneiderlab.tools.radialprojection.models.radialprojection.VesselSegmentationModel;
import schneiderlab.tools.radialprojection.views.userinterfacecomponents.Radical_Projection_Tool;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

public class MainController {
    private Radical_Projection_Tool mainView;
    private VesselSegmentationModel mainModel;
    private ArrayList<Path> processedFileInCreateSideView;
    private Context context;
    private ArrayList<Point> coordinates = new ArrayList<>() ;
    private ArrayList<Point> coordinatesBatch = new ArrayList<>() ;
    private Overlay overlaySegmentation;
    private ImagePlus impInByte;
    private Path currentFilePath;
    private ImgPlus<UnsignedShortType> sideView;
    private RandomAccessibleInterval<FloatType> hybridStackNonSmoothed;
    private RandomAccessibleInterval<FloatType> hybridStackSmoothed;
    private int hybridStackSmoothedWidth;
    private int hybridStackSmoothedHeight;
    private ImagePlus edgeBinaryMaskImagePlus;
    private ImagePlus finalSegmentation;
    private HashMap<Integer, ArrayList<Point>> centroidHashMap;


    public MainController(Radical_Projection_Tool mainView,
                          Context context) {
        this.mainView = mainView;
        this.context= context;
        
        // add Data and EventListener to mainView
        CziToTifModel cziToTifModel = new CziToTifModel();
        // Action for Browse button in Converting step
        mainView.getButtonBrowseConvertCzi2Tif().addActionListener(new BrowseButtonCZIToTif(
                mainView.getTextFieldConvertCzi2Tif(),
                mainView.getParentFrame()
        ));

        mainView.getTextFieldConvertCzi2Tif().setText(cziToTifModel.getDirPath());
        mainView.getTextFieldConvertCzi2Tif().getDocument().addDocumentListener(
                new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        cziToTifModel.setDirPath(mainView.getTextFieldConvertCzi2Tif().getText());
                    }
                    @Override
                    public void removeUpdate(DocumentEvent e) {
                    }
                    @Override
                    public void changedUpdate(DocumentEvent e) {
                    }
                }
        );

        // Action for checkbox background Subtraction in Converting step
        mainView.getCheckBoxBgSubConvertCzi2Tif().setSelected(cziToTifModel.isBgSub());
        mainView.getCheckBoxBgSubConvertCzi2Tif().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getSpinnerRollingConvertCzi2Tif().setEnabled(mainView.getCheckBoxBgSubConvertCzi2Tif().isSelected());
                cziToTifModel.setIsbgSub(mainView.getCheckBoxBgSubConvertCzi2Tif().isSelected());
            }
        });

        mainView.getSpinnerRollingConvertCzi2Tif().setValue(cziToTifModel.getRollingValue());
        mainView.getSpinnerRollingConvertCzi2Tif().addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        int value = (int) mainView.getSpinnerRollingConvertCzi2Tif().getValue();
                        cziToTifModel.setRollingValue(value);
                    }
                }
        );

        mainView.getSpinnerSaturateConvertCzi2Tif().setValue(cziToTifModel.getSaturationValue());
        mainView.getSpinnerSaturateConvertCzi2Tif().addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        int value = (int) mainView.getSpinnerSaturateConvertCzi2Tif().getValue();
                        cziToTifModel.setSaturationValue(value);
                    }
                }
        );

        // Action for checkbox Rotate in Converting step
        mainView.getCheckBoxRotateConvertCzi2Tif().setSelected(cziToTifModel.isRotate());
        mainView.getCheckBoxRotateConvertCzi2Tif().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getComboBoxRoateDirectionConvertCzi2Tif()
                        .setEnabled(mainView.getCheckBoxRotateConvertCzi2Tif().isSelected());
                cziToTifModel.setRotate(mainView.getCheckBoxRotateConvertCzi2Tif().isSelected());
            }
        });

        mainView.getComboBoxRoateDirectionConvertCzi2Tif().addItemListener(
                new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        RotateDirection rotateDirectionString = (RotateDirection) mainView.getComboBoxRoateDirectionConvertCzi2Tif()
                                .getSelectedItem();
                        cziToTifModel.setRotateDirection(rotateDirectionString);
                    }
                }
        );

        // Action for OK button in Converting step
        //TODO: Check to validate the data flow of this step
        mainView.getButtonOkConvertCzi2Tif().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.err.println(cziToTifModel.getDirPath() + " "
                        + cziToTifModel.isBgSub() + " "
                        + cziToTifModel.getRollingValue() + " "
                        + cziToTifModel.getSaturationValue() + " "
                        + cziToTifModel.isRotate() + " "
                + cziToTifModel.getRotateDirection());
//                String folderPath = mainView.getTextFieldConvertCzi2Tif().getText();
//                int rolling = (int) mainView.getSpinnerRollingConvertCzi2Tif().getValue();
//                int saturated = (int) mainView.getSpinnerSaturateConvertCzi2Tif().getValue();
//                String rotateDirectionString = (String) mainView.getComboBoxRoateDirectionConvertCzi2Tif()
//                                                                    .getSelectedItem();
//                boolean isRotate = mainView.getCheckBoxRotateConvertCzi2Tif().isSelected();
//                boolean isBackgroundSubtraction = mainView.getCheckBoxBgSubConvertCzi2Tif().isSelected();
//                Czi2TifWorker czi2TifWorker = new Czi2TifWorker(folderPath,
//                        isBackgroundSubtraction,
//                        rolling,
//                        saturated,
//                        isRotate,
//                        RotateDirection.fromLabel(rotateDirectionString));
//                czi2TifWorker.execute();
            }
        });

        // initialize the model for the vessel segmentation step
        mainView.getTableAddedFileVesselSegmentation().setModel(new DefaultTableModel(new String[]{"File Path"}, 0));

        // Create Side view button
        mainView.getButtonCreateSideView().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getTextField2StatusVesselSegmentation().setText("Creating Side View...");
                try {
                    String fileToProcess = (String) mainView.getTableAddedFileVesselSegmentation()
                            .getModel()
                            .getValueAt(0, 0);
                    CreateSideViewWorker createSideViewWorker = new CreateSideViewWorker(
                            (int) mainView.getSpinnerXYPixelSizeCreateSideView().getValue(),
                            (int) mainView.getSpinnerZPixelSizeCreateSideView().getValue(),
                            Paths.get(fileToProcess),
                            context
                    );
                    createSideViewWorker.addPropertyChangeListener(propChangeEvent -> {
                        if ("state".equals(propChangeEvent.getPropertyName()) &&
                                propChangeEvent.getNewValue() == SwingWorker.StateValue.DONE) {
                            try {
                                mainView.getTextField2StatusVesselSegmentation().setText("Side View Created");
                                mainView.getButtonProjAndSmooth().setEnabled(true);
                                sideView = createSideViewWorker.get();
                                ImageJFunctions.show(sideView);
                                // use result here
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                    createSideViewWorker.execute();
                } catch (Exception ex) {
                    mainView.getButtonAddFile().doClick();
                }
            }
        });

        // Action for ADD button in segmentation step
        mainView.getButtonAddFile().addActionListener(new AddFilePathToTable(
                mainView.getTableAddedFileVesselSegmentation(),mainView));

        // Action for REMOVE Button in segmentation step
        mainView.getButtonRemove().addActionListener(new RemoveFilePathFromTable(mainView.getTableAddedFileVesselSegmentation()));

        // tooltip to view full file path in segmentation step
        mainView.getTableAddedFileVesselSegmentation().addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int row = mainView.getTableAddedFileVesselSegmentation().rowAtPoint(e.getPoint());
                int col = mainView.getTableAddedFileVesselSegmentation().columnAtPoint(e.getPoint());
                if (row > -1 && col > -1) {
                    Object value = mainView.getTableAddedFileVesselSegmentation().getValueAt(row, col);
                    mainView.getTableAddedFileVesselSegmentation().setToolTipText(value != null ? value.toString() : null);
                }
            }
        });
        // button add folder for segmentation step
        mainView.getButtonAddFolder().addActionListener(new AddFilePathFromDirToTable(mainView.getTableAddedFileVesselSegmentation(), mainView));
        // button clear all in table for segmentation step
        mainView.getButtonClear().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultTableModel model = (DefaultTableModel) mainView.getTableAddedFileVesselSegmentation().getModel();
                model.setRowCount(0);
            }
        });
        // Slider update the percentage when the value change
        mainView.getSliderHybridWeight().addChangeListener(new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent e){
                int currentValue = mainView.getSliderHybridWeight().getValue();
                mainView.getLabelLigninHybridWeight().setText("Lignin " + (100-currentValue) + "%");
                mainView.getLabelCelluloseHybridWeight().setText("Cellulose " + currentValue + "%");
            }
        });

        // button projection and smoothing in segmentation step
        mainView.getButtonProjAndSmooth().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    mainView.getTextField2StatusVesselSegmentation().setText("Creating hybrid stack and smoothing...");
                ProjectionAndSmoothingWorker pasw = new ProjectionAndSmoothingWorker(sideView,
                        mainView.getSliderHybridWeight().getValue(),
                        (int)mainView.getSpinnerAnalysisWindow().getValue(),
                        (double) mainView.getSpinnerPreWatershedSmoothing().getValue(),
                        (int) mainView.getSpinnerInnerVesselRadius().getValue(),
                        context);
                pasw.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                                //TODO: get the result
                                hybridStackNonSmoothed = pasw.getHybridStackNonSmoothed();
                                hybridStackSmoothed = pasw.getHybridStackSmoothed();
                                hybridStackSmoothedWidth = pasw.getWidth();
                                hybridStackSmoothedHeight = pasw.getHeight();
                                ImageJFunctions.show(hybridStackNonSmoothed);
                                ImageJFunctions.show(hybridStackSmoothed);
                                // update UI
                                mainView.getTextField2StatusVesselSegmentation().setText("Complete Projection and Smoothing");
                                mainView.getButtonSelectCentroid().setEnabled(true);
                        }
                    }
                });
                pasw.execute();
            }
        });

        // button select Centroid to view the image and let user select centroid
        mainView.getButtonSelectCentroid().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RandomAccessibleInterval<FloatType>	smoothedStack = hybridStackSmoothed;
                int slideForTuning = (int)mainView.getSpinnerSliceIndexForTuning().getValue();
                RandomAccessibleInterval<FloatType> just1Slide = Views.hyperSlice(smoothedStack,2,slideForTuning);
                // Copy the view to a new Img<FloatType>
                // Create copy using cursors
                Img<FloatType> copy = ArrayImgs.floats(Intervals.dimensionsAsLongArray(just1Slide));
                net.imglib2.Cursor<FloatType> srcCursor = Views.flatIterable(just1Slide).cursor();
                net.imglib2.Cursor<FloatType> dstCursor = copy.cursor();
                while (srcCursor.hasNext()) {
                    dstCursor.next().set(srcCursor.next());
                }
                // Convert to ImagePlus
                ImagePlus impFloat = ImageJFunctions.wrap(copy, "Copied RAI");
                impFloat.resetDisplayRange();
                impInByte = new ImagePlus("impInByte", impFloat.getProcessor().convertToByte(true));
                impFloat.resetDisplayRange();
                impInByte.show();
                // Create a new PointRoi to collect points
                PointRoi pointRoi = new PointRoi();
                impInByte.setRoi(pointRoi);
                ImageCanvas canvas = impInByte.getCanvas();
                double magnificationLevel = 4.0;
                canvas.setMagnification(magnificationLevel);
                // Get screen dimensions
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int screenWidth = screenSize.width;
                int screenHeight = screenSize.height;
                // Calculate window size for zoom
                int imgWidth = impInByte.getWidth() * (int) magnificationLevel;
                int imgHeight = impInByte.getHeight() * (int) magnificationLevel;
                // position the window at  bottom left
                int xlocation = 10;
                int ylocation = screenHeight-imgHeight-((int)screenHeight*4/100); // screenHeight*4/100 to create a little bit space
                ImageWindow window = impInByte.getWindow();
                window.setLocationAndSize( xlocation,ylocation ,imgWidth,imgHeight);
                // add eventListener to canvas
                canvas.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Add point to the PointRoi
                        int x = canvas.offScreenX(e.getX());
                        int y = canvas.offScreenY(e.getY());
                        Point pointLatest = new Point(x,y);
                        coordinates.add(pointLatest);
                        IJ.log(coordinates.toString());
                        mainView.getButtonWatershed().setEnabled(true);
                    }
                });
            }
        });

        // button Watershed to segment the image
        mainView.getButtonWatershed().addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                SwingWorker<Void, Void> segmentationWorker = new SwingWorker<Void, Void>() {
                    @Override
                    protected Void doInBackground() throws Exception {
                        int slideForTuning = (int)mainView.getSpinnerSliceIndexForTuning().getValue();
                        Reconstruction reconstruction = new Reconstruction(hybridStackSmoothed,
                                hybridStackSmoothedWidth,
                                hybridStackSmoothedHeight,
                                (int)mainView.getSpinnerInnerVesselRadius().getValue(),
                                coordinates,
                                (int)mainView.getSpinnerSliceIndexForTuning().getValue(),
                                (int)mainView.getSpinnerXYPixelSizeCreateSideView().getValue());
                        overlaySegmentation = reconstruction.process1Slide();
                        return null;
                    }
                };
                segmentationWorker.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                            coordinatesBatch.clear();
                            coordinatesBatch.addAll(coordinates);
                            coordinates.clear();
                            mainView.getButtonProcessWholeStack().setEnabled(true);
                            mainView.getButtonWatershed().setEnabled(false);
                            impInByte.setOverlay(overlaySegmentation);
                            impInByte.updateAndDraw();
                        }
                    }
                });
                segmentationWorker.execute();
            }
        });

        // button processing wholeStack
        mainView.getButtonProcessWholeStack().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getTextField2StatusVesselSegmentation().setText("Processing whole stack...");
                mainView.getProgressBarVesselSegmentation().setValue(0);
                SegmentWholeStackWorker batchSegmentationWorker = new SegmentWholeStackWorker(
                        hybridStackSmoothed,
                        hybridStackSmoothedWidth,
                        hybridStackSmoothedHeight,
                        (int)mainView.getSpinnerInnerVesselRadius().getValue(),
                        coordinatesBatch,
                        (int)mainView.getSpinnerSliceIndexForTuning().getValue(),
                        (int)mainView.getSpinnerXYPixelSizeCreateSideView().getValue()
                );
                batchSegmentationWorker.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                        if("progress".equals(propertyChangeEvent.getPropertyName())){
                            int currentProgressValue = (int) propertyChangeEvent.getNewValue();
                            mainView.getProgressBarVesselSegmentation().setValue(currentProgressValue);
                            mainView.getProgressBarVesselSegmentation().setToolTipText(currentProgressValue + "%");
                        }
                        if ("state".equals(propertyChangeEvent.getPropertyName()) &&
                                propertyChangeEvent.getNewValue() == SwingWorker.StateValue.DONE){
                            finalSegmentation=batchSegmentationWorker.getFinalSegmentation();
                            edgeBinaryMaskImagePlus=batchSegmentationWorker.getEdgeBinaryMaskImagePlus();
                            centroidHashMap=batchSegmentationWorker.getCentroidHashMap();
                            ImagePlus hybridStackWithEdgeCentroidOverlay = batchSegmentationWorker.getStackWithVesselEdgeCentroidOverlay();
                            mainView.getTextField2StatusVesselSegmentation().setText("Complete processing whole stack ");
                            mainView.getButtonMoveToRadialProjection().setEnabled(true);
                            mainView.getProgressBarVesselSegmentation().setValue(100);
                            mainView.getProgressBarVesselSegmentation().setToolTipText(100+"%");
                            batchSegmentationWorker.getFinalSegmentation().show();
                            batchSegmentationWorker.getEdgeCentroidMaskImagePlus().show();
//                            hybridStackWithEdgeCentroidOverlay.show(); //TODO:
                        }
                    }
                });
                batchSegmentationWorker.execute();
            }
        });

        // button move to radial projection step
        mainView.getButtonMoveToRadialProjection().addActionListener(new MoveCurrentFileToRadialProjectionStep(
                mainView.getTableAddedFileVesselSegmentation(),
                mainView.getTextFieldRadialProjection()));

        // perform Radial Projection
        mainView.getButtonRunRadialProjection().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create copy using cursors
                Img<FloatType> copy = ArrayImgs.floats(Intervals.dimensionsAsLongArray(hybridStackNonSmoothed));
                net.imglib2.Cursor<FloatType> srcCursor = Views.flatIterable(hybridStackNonSmoothed).cursor();
                net.imglib2.Cursor<FloatType> dstCursor = copy.cursor();
                while (srcCursor.hasNext()) {
                    dstCursor.next().set(srcCursor.next());
                }
                // Convert to ImagePlus
                ImagePlus impFloat = ImageJFunctions.wrap(copy, "Copied RAI");
                impFloat.resetDisplayRange();
                PolarProjectionWorker polarProjection = new PolarProjectionWorker(
                        impFloat,
                        edgeBinaryMaskImagePlus,
                        centroidHashMap
                );
                polarProjection.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                            ImagePlus vessel1PolarProjection= polarProjection.getVessel1PolarProjection();
                            ImagePlus vessel2PolarProjection= polarProjection.getVessel2PolarProjection();
                            vessel1PolarProjection.setTitle("Vessel 1 Radial Projection");
                            vessel2PolarProjection.setTitle("Vessel 2 Radial Projection");
                            vessel1PolarProjection.show();
                            vessel2PolarProjection.show();
                        }
                    }
                });
                polarProjection.execute();
            }
        });

    }


}

