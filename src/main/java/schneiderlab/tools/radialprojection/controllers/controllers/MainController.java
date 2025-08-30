package schneiderlab.tools.radialprojection.controllers.controllers;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.*;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import org.scijava.Context;
import schneiderlab.tools.radialprojection.controllers.uiaction.*;
import schneiderlab.tools.radialprojection.controllers.uiaction.czitotif.BrowseButtonCZIToTif;
import schneiderlab.tools.radialprojection.controllers.uiaction.mainwindow.AddSavingActionWhenMainWindowClosed;
import schneiderlab.tools.radialprojection.controllers.workers.*;
import schneiderlab.tools.radialprojection.imageprocessor.core.Vessel;
import schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement.Tile;
import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.RotateDirection;
import schneiderlab.tools.radialprojection.imageprocessor.core.segmentation.Reconstruction;
import schneiderlab.tools.radialprojection.imageprocessor.core.utils.RadialProjectionUtils;
import schneiderlab.tools.radialprojection.models.czitotifmodel.CziToTifModel;
import schneiderlab.tools.radialprojection.models.radialprojection.RadialProjectionModel;
import schneiderlab.tools.radialprojection.models.radialprojection.VesselsSegmentationModel;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class MainController {
    private final Radical_Projection_Tool mainView;
    private ArrayList<Path> processedFileInCreateSideView;
    private final Context context;
    private ImagePlus finalSegmentation;

    public MainController(Radical_Projection_Tool mainView,
                          Context context) {
        this.mainView = mainView;
        this.context= context;

        //-----------1.CZI to TIF converting Steps-------------------------------
        // create an instance of the czi to TIF model
        CziToTifModel cziToTifModel = new CziToTifModel();
        // get initial values from properties file
        cziToTifModel.initValues("/properties_files/initValues.properties");
        // Action for Browse button in Converting step
        mainView.getButtonBrowseConvertCzi2Tif().addActionListener(new BrowseButtonCZIToTif(
                mainView.getTextFieldConvertCzi2Tif(),
                mainView.getParentFrame()
        ));
        // starting dir for text field
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
        mainView.getCheckBoxBgSubConvertCzi2Tif().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mainView.getSpinnerRollingConvertCzi2Tif().setEnabled(mainView.getCheckBoxBgSubConvertCzi2Tif().isSelected());
                cziToTifModel.setIsbgSub(mainView.getCheckBoxBgSubConvertCzi2Tif().isSelected());
            }
        });
        //add action for the rolling value spinner
        mainView.getSpinnerRollingConvertCzi2Tif().addChangeListener(
                new ChangeListener() {
                    @Override
                    public void stateChanged(ChangeEvent e) {
                        int value = (int) mainView.getSpinnerRollingConvertCzi2Tif().getValue();
                        cziToTifModel.setRollingValue(value);
                    }
                }
        );
        // add action to the saturate value spinner
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
        mainView.getCheckBoxRotateConvertCzi2Tif().addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                mainView.getComboBoxRoateDirectionConvertCzi2Tif()
                        .setEnabled(mainView.getCheckBoxRotateConvertCzi2Tif().isSelected());
                cziToTifModel.setRotate(mainView.getCheckBoxRotateConvertCzi2Tif().isSelected());
            }
        });
        // Update the model when another item in the combobox is selected
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
        // add the values from the czitotif model to the view
        mainView.getTextFieldConvertCzi2Tif().setText(cziToTifModel.getDirPath());// only when the application is started
        mainView.getCheckBoxBgSubConvertCzi2Tif().setSelected(cziToTifModel.isBgSub());// only when the application is started
        mainView.getSpinnerRollingConvertCzi2Tif().setValue(cziToTifModel.getRollingValue());// only when the application is started
        mainView.getSpinnerSaturateConvertCzi2Tif().setValue(cziToTifModel.getSaturationValue());// only when the application is started
        mainView.getCheckBoxRotateConvertCzi2Tif().setSelected(cziToTifModel.isRotate());// only when the application is started
        mainView.getComboBoxRoateDirectionConvertCzi2Tif().setSelectedItem(cziToTifModel.getRotateDirection());
        // Action for OK button in Converting step
        mainView.getButtonOkConvertCzi2Tif().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String folderPath = cziToTifModel.getDirPath();
                int rolling = cziToTifModel.getRollingValue();
                int saturated = cziToTifModel.getSaturationValue();
                RotateDirection rotateDirection = cziToTifModel.getRotateDirection();
                boolean isRotate = cziToTifModel.isRotate();
                boolean isBackgroundSubtraction = cziToTifModel.isBgSub();
                Czi2TifWorker czi2TifWorker = new Czi2TifWorker(folderPath,
                        isBackgroundSubtraction,
                        rolling,
                        saturated,
                        isRotate,
                        rotateDirection);
                czi2TifWorker.execute();
            }
        });
        //----------- 2.Vessel segmentation -------------------------------
        // create an instance of the Vessel segmentation model
        VesselsSegmentationModel vesselsSegmentationModel = new VesselsSegmentationModel();
        // get initial values from properties file
        vesselsSegmentationModel.initValues("/properties_files/initValues.properties");
        mainView.getTableAddedFileVesselSegmentation().setModel(new DefaultTableModel(new String[]{"File Path"}, 0));
        // spinner xy pixel size
        mainView.getSpinnerXYPixelSizeCreateSideView().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int) mainView.getSpinnerXYPixelSizeCreateSideView().getValue();
                vesselsSegmentationModel.setXyPixelSize(value);
            }
        });
        // spinner xy pixel size
        mainView.getSpinnerXYPixelSizeCreateSideView().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int) mainView.getSpinnerXYPixelSizeCreateSideView().getValue();
                vesselsSegmentationModel.setXyPixelSize(value);
            }
        });
        // spinner z pixel size
        mainView.getSpinnerZPixelSizeCreateSideView().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int) mainView.getSpinnerZPixelSizeCreateSideView().getValue();
                vesselsSegmentationModel.setzPixelSize(value);
            }
        });
        // spinner analysis window
        mainView.getSpinnerAnalysisWindow().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int) mainView.getSpinnerAnalysisWindow().getValue();
                vesselsSegmentationModel.setAnalysisWindow(value);
            }
        });
        // spinner pre watershed smoothing
        mainView.getSpinnerPreWatershedSmoothing().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) mainView.getSpinnerPreWatershedSmoothing().getValue();
                vesselsSegmentationModel.setSmoothingSigma(value);
            }
        });
        // spinner slice index for tuning
        mainView.getSpinnerSliceIndexForTuning().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                int value = (int) mainView.getSpinnerSliceIndexForTuning().getValue();
                vesselsSegmentationModel.setSliceIndexForTuning(value);
            }
        });
        // spinner vessel radius
        mainView.getSpinnerInnerVesselRadius().addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                double value = (double) mainView.getSpinnerInnerVesselRadius().getValue();
                vesselsSegmentationModel.setInnerVesselRadius(value);
            }
        });
        // Create Side view button
        mainView.getButtonCreateSideView().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getTextField2StatusVesselSegmentation().setText("Creating Side View...");
                mainView.getButtonProjAndSmooth().setEnabled(false);
                mainView.getButtonSelectCentroid().setEnabled(false);
                mainView.getButtonWatershed().setEnabled(false);
                mainView.getButtonProcessWholeStack().setEnabled(false);
                mainView.getButtonMoveToRadialProjection().setEnabled(false);
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
                    createSideViewWorker.addPropertyChangeListener(new PropertyChangeListener() {
                        @Override
                        public void propertyChange(PropertyChangeEvent evt) {
                            if ("progress".equals(evt.getPropertyName())){
                                mainView.getProgressBarVesselSegmentation().setValue((int)evt.getNewValue());
                            }
                        }
                    });
                    createSideViewWorker.addPropertyChangeListener(propChangeEvent -> {
                        if ("state".equals(propChangeEvent.getPropertyName()) &&
                                propChangeEvent.getNewValue() == SwingWorker.StateValue.DONE) {
                            try {
                                mainView.getTextField2StatusVesselSegmentation().setText("Side View Created");
                                mainView.getButtonProjAndSmooth().setEnabled(true);
                                vesselsSegmentationModel.setSideView(createSideViewWorker.get());
                                ImageJFunctions.show(vesselsSegmentationModel.getSideView());
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
                vesselsSegmentationModel.setCelluloseToLigninRatio(currentValue);
            }
        });

        // button projection and smoothing in segmentation step
        mainView.getButtonProjAndSmooth().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                    mainView.getTextField2StatusVesselSegmentation().setText("Creating hybrid stack and smoothing...");
                    mainView.getButtonSelectCentroid().setEnabled(false);
                    mainView.getButtonWatershed().setEnabled(false);
                    mainView.getButtonProcessWholeStack().setEnabled(false);
                    mainView.getButtonMoveToRadialProjection().setEnabled(false);
                ProjectionAndSmoothingWorker pasw = new ProjectionAndSmoothingWorker(vesselsSegmentationModel.getSideView(),
                        mainView.getSliderHybridWeight().getValue(),
                        (int)mainView.getSpinnerAnalysisWindow().getValue(),
                        (double) mainView.getSpinnerPreWatershedSmoothing().getValue(),
                        (double) mainView.getSpinnerInnerVesselRadius().getValue(),
                        context);
                pasw.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if("progress".equals(evt.getPropertyName())){
                            int currentProgress = (int)evt.getNewValue();
                            mainView.getProgressBarVesselSegmentation().setValue(currentProgress);
                            mainView.getProgressBarVesselSegmentation().setToolTipText(String.valueOf(currentProgress));
                        }
                        if ("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                                vesselsSegmentationModel.setHybridStackNonSmoothed(pasw.getHybridStackNonSmoothed());
                                vesselsSegmentationModel.setHybridStackSmoothed(pasw.getHybridStackSmoothed());
                                vesselsSegmentationModel.setHybridStackSmoothedWidth(pasw.getWidth());
                                vesselsSegmentationModel.setHybridStackSmoothedHeight(pasw.getHeight());
                                vesselsSegmentationModel.setCellulose(pasw.getCellulose());
                                vesselsSegmentationModel.setLignin(pasw.getLignin());
                                ImageJFunctions.show(vesselsSegmentationModel.getHybridStackNonSmoothed());
                                ImageJFunctions.show(vesselsSegmentationModel.getHybridStackSmoothed());
                                // update UI
                                mainView.getTextField2StatusVesselSegmentation().setText("Complete Projection and Smoothing");
                                mainView.getButtonSelectCentroid().setEnabled(true);
                        }
                    }
                });
                pasw.execute();
            }
        });
        //TODO: seperate the below actionListener to its own class
        // button select Centroid to view the image and let user select centroid
        mainView.getButtonSelectCentroid().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainView.getButtonWatershed().setEnabled(false);
                mainView.getButtonProcessWholeStack().setEnabled(false);
                mainView.getButtonMoveToRadialProjection().setEnabled(false);
                RandomAccessibleInterval<FloatType>	smoothedStack = vesselsSegmentationModel.getHybridStackSmoothed();
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
                vesselsSegmentationModel.setImpInByte(new ImagePlus("impInByte", impFloat.getProcessor().convertToByte(true)));
                impFloat.resetDisplayRange();
                vesselsSegmentationModel.getImpInByte().show();
                // Create a new PointRoi to collect points
                PointRoi pointRoi = new PointRoi();
                vesselsSegmentationModel.getImpInByte().setRoi(pointRoi);
                ImageCanvas canvas = vesselsSegmentationModel.getImpInByte().getCanvas();
                double magnificationLevel = 4.0;
                canvas.setMagnification(magnificationLevel);
                // Get screen dimensions
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                int screenWidth = screenSize.width;
                int screenHeight = screenSize.height;
                // Calculate window size for zoom
                int imgWidth = vesselsSegmentationModel.getImpInByte().getWidth() * (int) magnificationLevel;
                int imgHeight = vesselsSegmentationModel.getImpInByte().getHeight() * (int) magnificationLevel;
                // position the window at  bottom left
                int xlocation = 10;
                int ylocation = screenHeight-imgHeight-(screenHeight *4/100); // screenHeight*4/100 to create a little bit space
                ImageWindow window = vesselsSegmentationModel.getImpInByte().getWindow();
                window.setLocationAndSize( xlocation,ylocation ,imgWidth,imgHeight);
                // add eventListener to canvas
                canvas.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        // Add point to the PointRoi
                        int x = canvas.offScreenX(e.getX());
                        int y = canvas.offScreenY(e.getY());
                        Point pointLatest = new Point(x,y);
                        vesselsSegmentationModel.getCoordinates().add(pointLatest);
                        IJ.log(vesselsSegmentationModel.getCoordinates().toString());
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
                        Reconstruction reconstruction = new Reconstruction(
                                vesselsSegmentationModel.getHybridStackSmoothed(),
                                vesselsSegmentationModel.getHybridStackSmoothedWidth(),
                                vesselsSegmentationModel.getHybridStackSmoothedHeight(),
                                (double)mainView.getSpinnerInnerVesselRadius().getValue(),
                                vesselsSegmentationModel.getCoordinates(),
                                (int)mainView.getSpinnerSliceIndexForTuning().getValue(),
                                (int)mainView.getSpinnerXYPixelSizeCreateSideView().getValue());
                        vesselsSegmentationModel.setOverlaySegmentation(reconstruction.process1Slide());
                        return null;
                    }
                };
                segmentationWorker.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                            vesselsSegmentationModel.getCoordinatesBatch().clear();
                            vesselsSegmentationModel.getCoordinatesBatch().addAll(vesselsSegmentationModel.getCoordinates());
                            vesselsSegmentationModel.getCoordinates().clear();
                            mainView.getButtonProcessWholeStack().setEnabled(true);
                            mainView.getButtonWatershed().setEnabled(false);
                            vesselsSegmentationModel.getImpInByte().setOverlay(vesselsSegmentationModel.getOverlaySegmentation());
                            vesselsSegmentationModel.getImpInByte().updateAndDraw();
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
                        vesselsSegmentationModel.getHybridStackSmoothed(),
                        vesselsSegmentationModel.getHybridStackSmoothedWidth(),
                        vesselsSegmentationModel.getHybridStackSmoothedHeight(),
                        (double)mainView.getSpinnerInnerVesselRadius().getValue(),
                        vesselsSegmentationModel.getCoordinatesBatch(),
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
                            vesselsSegmentationModel.setEdgeBinaryMaskImagePlus(batchSegmentationWorker.getEdgeBinaryMaskImagePlus());
                            vesselsSegmentationModel.setCentroidHashMap(batchSegmentationWorker.getCentroidHashMap());
                            vesselsSegmentationModel.setVesselArrayList(batchSegmentationWorker.getVesselArrayList());
//                            ImagePlus hybridStackWithEdgeCentroidOverlay = batchSegmentationWorker.getStackWithVesselEdgeCentroidOverlay();
                            mainView.getTextField2StatusVesselSegmentation().setText("Complete processing whole stack ");
                            mainView.getButtonMoveToRadialProjection().setEnabled(true);
                            mainView.getProgressBarVesselSegmentation().setValue(100);
                            mainView.getProgressBarVesselSegmentation().setToolTipText(100+"%");
                            batchSegmentationWorker.getFinalSegmentation().show();
                            batchSegmentationWorker.getEdgeCentroidMaskImagePlus().show();
                        }
                    }
                });
                batchSegmentationWorker.execute();
            }
        });

        // button move to radial projection step
        mainView.getButtonMoveToRadialProjection().addActionListener(new MoveCurrentFileToRadialProjectionStep(
                mainView.getTableAddedFileVesselSegmentation(),
                mainView.getTextFieldRadialProjection(),
                mainView.getTabbedPaneMainPane(),
                mainView.getPanel3RadialProjection()));

        // add the values from the vesselSegmentation model to the view
        mainView.getSpinnerXYPixelSizeCreateSideView().setValue(vesselsSegmentationModel.getXyPixelSize());
        mainView.getSpinnerZPixelSizeCreateSideView().setValue(vesselsSegmentationModel.getzPixelSize());
        mainView.getSpinnerAnalysisWindow().setValue(vesselsSegmentationModel.getAnalysisWindow());
        mainView.getSpinnerPreWatershedSmoothing().setValue(vesselsSegmentationModel.getSmoothingSigma());
        mainView.getSpinnerSliceIndexForTuning().setValue(vesselsSegmentationModel.getSliceIndexForTuning());
        mainView.getSpinnerInnerVesselRadius().setValue(vesselsSegmentationModel.getInnerVesselRadius());
        mainView.getSliderHybridWeight().setValue(vesselsSegmentationModel.getCelluloseToLigninRatio());
        //---------- - 3.Radial Projection and Unrolling -------------------------------
        // initial the model for radial projection step
        RadialProjectionModel radialProjectionModel = new RadialProjectionModel();
        // perform Radial Projection
        mainView.getButtonRunRadialProjection().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Create copy of hybrid using cursors
                 ImagePlus impFloat = RadialProjectionUtils.copyAndConvertRandomAccessIntervalToImagePlus(
                         vesselsSegmentationModel.getHybridStackNonSmoothed(), "Non Smoothed Hybrid Stack");
                // Create copy of Lignin using cursors
                ImagePlus lignin = RadialProjectionUtils.copyAndConvertRandomAccessIntervalToImagePlus(
                        vesselsSegmentationModel.getLignin(), "Non Smoothed Hybrid Stack");
                // Create copy of celluose using cursors
                ImagePlus cellulose = RadialProjectionUtils.copyAndConvertRandomAccessIntervalToImagePlus(
                        vesselsSegmentationModel.getCellulose(), "Non Smoothed Hybrid Stack");
//                Img<FloatType> copy = ArrayImgs.floats(Intervals.dimensionsAsLongArray(vesselsSegmentationModel.getHybridStackNonSmoothed()));
//                net.imglib2.Cursor<FloatType> srcCursor = Views.flatIterable(vesselsSegmentationModel.getHybridStackNonSmoothed()).cursor();
//                net.imglib2.Cursor<FloatType> dstCursor = copy.cursor();
//                while (srcCursor.hasNext()) {
//                    dstCursor.next().set(srcCursor.next());
//                }
//                // Convert to ImagePlus
//                ImagePlus impFloat = ImageJFunctions.wrap(copy, "Copied RAI");
//                impFloat.resetDisplayRange();
                PolarProjectionWorker polarProjection = new PolarProjectionWorker(
                        impFloat,
                        cellulose,
                        lignin,
                        vesselsSegmentationModel.getEdgeBinaryMaskImagePlus(),
                        vesselsSegmentationModel.getVesselArrayList()
                );
                polarProjection.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                            ArrayList<ImagePlus> vesselRadialProjectionList = polarProjection.getVesselPolarProjectionArrayList();
                            radialProjectionModel.setVesselArrayList(vesselsSegmentationModel.getVesselArrayList()); // transfer the vesselArrayList to from segmentation Model to radialProjectionModel
                            for(ImagePlus radialProjectedImage : vesselRadialProjectionList){
                                radialProjectedImage.show();
                            }
                        }
                    }
                });
                polarProjection.execute();
            }
        });
        // Unrolling Vessels
        mainView.getButtonUnrollVessel().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImagePlus impFloat = RadialProjectionUtils.copyAndConvertRandomAccessIntervalToImagePlus(
                        vesselsSegmentationModel.getHybridStackNonSmoothed(), "Non Smoothed Hybrid Stack");
                UnrollVesselWorker unrollVesselWorker = new UnrollVesselWorker(
                        impFloat,
                        vesselsSegmentationModel.getEdgeBinaryMaskImagePlus(),
                        vesselsSegmentationModel.getVesselArrayList()
                );
                unrollVesselWorker.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent evt) {
                        if ("state".equals(evt.getPropertyName()) &&
                                evt.getNewValue() == SwingWorker.StateValue.DONE){
                            ArrayList<ImagePlus> vesselUnrolledList = unrollVesselWorker.getVesselPolarProjectionArrayList();
                            for(ImagePlus unrolledImage : vesselUnrolledList){
                                unrolledImage.show();
                            }
                        }
                    }
                });
                unrollVesselWorker.execute();
            }
        });
        //-------------------Analysis----------------------------------------------
        mainView.getButtonAnalysisSkeletonize().addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //TODO: get the list of vessel, get the size, split them, create a List of Tiles object, use instance method to threshold and skeletonze
                // using the
                ArrayList<Vessel> vesselArrayList = vesselsSegmentationModel.getVesselArrayList();
                Vessel vessel1 = vesselArrayList.get(0);
                ImagePlus vessel1Img = vessel1.getRadialProjectionHybrid();
                ArrayList<Tile> tileArrayList = Tile.divideIntoEqualSize(vessel1Img.getWidth(),
                                                                    vessel1Img.getHeight(),
                                                                            1);
                Tile.splitImage(vessel1Img,tileArrayList);
                for (Tile t: tileArrayList){
                    t.thresholdOtsu();
                }
                ByteProcessor thresholdedProcessor = Tile.combineTiles(tileArrayList,vessel1Img.getWidth(),vessel1Img.getHeight());
                ImagePlus thresholdedRadialProjection = new ImagePlus("skeletonized radial projection", thresholdedProcessor);
                ByteProcessor skeletonizedRadialProjectionProcessor = (ByteProcessor) thresholdedProcessor.duplicate();
//                skeletonizedRadialProjectionProcessor.invert();
                skeletonizedRadialProjectionProcessor.skeletonize(255);
                ImagePlus skeletonizedRadialProjection = new ImagePlus("Skeletonized Radial Projection", skeletonizedRadialProjectionProcessor);
                thresholdedRadialProjection.show();
                skeletonizedRadialProjection.show();
            }
        });


        //--------------MAIN WINDOW-----------------------------------------
        // TODO:Save all the parameters of current session to imagej/Fiji Prefs
        mainView.getParentFrame().addWindowListener(
                new AddSavingActionWhenMainWindowClosed(cziToTifModel,
                                                        vesselsSegmentationModel));
    }


}

