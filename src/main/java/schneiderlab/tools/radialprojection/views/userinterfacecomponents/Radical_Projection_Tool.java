package schneiderlab.tools.radialprojection.views.userinterfacecomponents;

import java.awt.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.*;
import org.scijava.Context;
import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.RotateDirection;

/*
 * Created by JFormDesigner on Fri Apr 04 02:15:10 CEST 2025
 */

/**
 * @author anhminh
 */
public class Radical_Projection_Tool extends JFrame {

	private JFrame parentFrame;

	public Radical_Projection_Tool(Context context, JFrame parentFrame) {
		initComponents();
		this.parentFrame=parentFrame;
		this.getComboBoxRoateDirectionConvertCzi2Tif().setSelectedIndex(0);
	}

	public JFrame getParentFrame() {
		return parentFrame;
	}

	public JTabbedPane getTabbedPaneMainPane() {
		return tabbedPaneMainPane;
	}

	public JPanel getPanelConvertCzi2Tif() {
		return panelConvertCzi2Tif;
	}

	public JButton getButtonBrowseConvertCzi2Tif() {
		return buttonBrowseConvertCzi2Tif;
	}

	public JTextField getTextFieldConvertCzi2Tif() {
		return textFieldConvertCzi2Tif;
	}

	public JCheckBox getCheckBoxBgSubConvertCzi2Tif() {
		return checkBoxBgSubConvertCzi2Tif;
	}

	public JLabel getLabelRollingConvertCzi2Tif() {
		return labelRollingConvertCzi2Tif;
	}

	public JSpinner getSpinnerRollingConvertCzi2Tif() {
		return spinnerRollingConvertCzi2Tif;
	}

	public JLabel getLabelEnhanceConstConvertCzi2Tif() {
		return labelEnhanceConstConvertCzi2Tif;
	}

	public JLabel getLabelSaturateConvertCzi2Tif() {
		return labelSaturateConvertCzi2Tif;
	}

	public JSpinner getSpinnerSaturateConvertCzi2Tif() {
		return spinnerSaturateConvertCzi2Tif;
	}

	public JLabel getLabelpercentSignConvertCzi2Tif() {
		return labelpercentSignConvertCzi2Tif;
	}

	public JCheckBox getCheckBoxRotateConvertCzi2Tif() {
		return checkBoxRotateConvertCzi2Tif;
	}

	public JComboBox<String> getComboBoxRoateDirectionConvertCzi2Tif() {
		return comboBoxRoateDirectionConvertCzi2Tif;
	}

	public JButton getButtonOkConvertCzi2Tif() {
		return buttonOkConvertCzi2Tif;
	}

	public JTextField getTextFieldStatusConvertCzi2Tif() {
		return textFieldStatusConvertCzi2Tif;
	}

	public JProgressBar getProgressBarConvertCzi2Tif() {
		return progressBarConvertCzi2Tif;
	}

	public JPanel getPanelVesselsSegmentation() {
		return panelVesselsSegmentation;
	}

	public JTabbedPane getTabbedPaneVesselSegmentation() {
		return tabbedPaneVesselSegmentation;
	}

	public JPanel getPanelImageListVesselSegmentation() {
		return panelImageListVesselSegmentation;
	}

	public JButton getButtonAddFile() {
		return buttonAddFile;
	}

	public JButton getButtonAddFolder() {
		return buttonAddFolder;
	}

	public JButton getButtonRemove() {
		return buttonRemove;
	}

	public JButton getButtonClear() {
		return buttonClear;
	}

	public JScrollPane getScrollPaneVesselSegmentation() {
		return scrollPaneVesselSegmentation;
	}

	public JPanel getPanelParametersVesselSegmentation() {
		return panelParametersVesselSegmentation;
	}

	public JLabel getLabelTargetXYPixelSize() {
		return labelTargetXYPixelSize;
	}

	public JSpinner getSpinnerXYPixelSizeCreateSideView() {
		return spinnerXYPixelSizeCreateSideView;
	}

	public JLabel getLabelTargetZPixelSize() {
		return labelTargetZPixelSize;
	}

	public JSpinner getSpinnerZPixelSizeCreateSideView() {
		return spinnerZPixelSizeCreateSideView;
	}

	public JLabel getLabelAnalysisWindow() {
		return labelAnalysisWindow;
	}

	public JSpinner getSpinnerAnalysisWindow() {
		return spinnerAnalysisWindow;
	}

	public JLabel getLabelPreWatershedSmoothing() {
		return labelPreWatershedSmoothing;
	}

	public JSpinner getSpinnerPreWatershedSmoothing() {
		return spinnerPreWatershedSmoothing;
	}

	public JLabel getLabelSliceIndexforTuning() {
		return labelSliceIndexforTuning;
	}

	public JSpinner getSpinnerSliceIndexForTuning() {
		return spinnerSliceIndexForTuning;
	}

	public JLabel getLabelInnerVesselRadius() {
		return labelInnerVesselRadius;
	}

	public JSpinner getSpinnerInnerVesselRadius() {
		return spinnerInnerVesselRadius;
	}

	public JLabel getLabelHybridWeight() {
		return labelHybridWeight;
	}

	public JLabel getLabelLigninHybridWeight() {
		return labelLigninHybridWeight;
	}

	public JSlider getSliderHybridWeight() {
		return sliderHybridWeight;
	}

	public JLabel getLabelCelluloseHybridWeight() {
		return labelCelluloseHybridWeight;
	}

	public JButton getButtonCreateSideView() {
		return buttonCreateSideView;
	}

	public JButton getButtonProjAndSmooth() {
		return buttonProjAndSmooth;
	}

	public JButton getButtonSelectCentroid() {
		return buttonSelectCentroid;
	}

	public JButton getButtonWatershed() {
		return buttonWatershed;
	}

	public JButton getButtonProcessWholeStack() {
		return buttonProcessWholeStack;
	}

	public JButton getButtonMoveToRadialProjection() {
		return buttonMoveToRadialProjection;
	}

	public JTextField getTextField2StatusVesselSegmentation() {
		return textField2StatusVesselSegmentation;
	}

	public JButton getButtonRunRadialProjection() {
		return buttonRunRadialProjection;
	}

	public JTable getTableAddedFileVesselSegmentation() {
		return tableAddedFileVesselSegmentation;
	}

	public JTextField getTextFieldRadialProjection() {return textFieldRadialProjection;}

	public JProgressBar getProgressBarVesselSegmentation(){return progressBarVesselSegmentation;}
	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
		// Generated using JFormDesigner Educational license - Anh Minh Do
		tabbedPaneMainPane = new JTabbedPane();
		panelConvertCzi2Tif = new JPanel();
		buttonBrowseConvertCzi2Tif = new JButton();
		textFieldConvertCzi2Tif = new JTextField();
		checkBoxBgSubConvertCzi2Tif = new JCheckBox();
		labelRollingConvertCzi2Tif = new JLabel();
		spinnerRollingConvertCzi2Tif = new JSpinner();
		labelEnhanceConstConvertCzi2Tif = new JLabel();
		labelSaturateConvertCzi2Tif = new JLabel();
		spinnerSaturateConvertCzi2Tif = new JSpinner();
		labelpercentSignConvertCzi2Tif = new JLabel();
		checkBoxRotateConvertCzi2Tif = new JCheckBox();
		comboBoxRoateDirectionConvertCzi2Tif = new JComboBox<>(RotateDirection.values());
		buttonOkConvertCzi2Tif = new JButton();
		textFieldStatusConvertCzi2Tif = new JTextField();
		progressBarConvertCzi2Tif = new JProgressBar();
		panelVesselsSegmentation = new JPanel();
		tabbedPaneVesselSegmentation = new JTabbedPane();
		panelImageListVesselSegmentation = new JPanel();
		buttonAddFile = new JButton();
		buttonAddFolder = new JButton();
		buttonRemove = new JButton();
		buttonClear = new JButton();
		scrollPaneVesselSegmentation = new JScrollPane();
		tableAddedFileVesselSegmentation = new JTable();
		panelParametersVesselSegmentation = new JPanel();
		labelTargetXYPixelSize = new JLabel();
		spinnerXYPixelSizeCreateSideView = new JSpinner();
		labelTargetZPixelSize = new JLabel();
		spinnerZPixelSizeCreateSideView = new JSpinner();
		labelAnalysisWindow = new JLabel();
		spinnerAnalysisWindow = new JSpinner();
		labelPreWatershedSmoothing = new JLabel();
		spinnerPreWatershedSmoothing = new JSpinner();
		labelSliceIndexforTuning = new JLabel();
		spinnerSliceIndexForTuning = new JSpinner();
		labelInnerVesselRadius = new JLabel();
		spinnerInnerVesselRadius = new JSpinner();
		labelHybridWeight = new JLabel();
		labelLigninHybridWeight = new JLabel();
		sliderHybridWeight = new JSlider();
		labelCelluloseHybridWeight = new JLabel();
		buttonCreateSideView = new JButton();
		buttonProjAndSmooth = new JButton();
		buttonSelectCentroid = new JButton();
		buttonWatershed = new JButton();
		buttonProcessWholeStack = new JButton();
		buttonMoveToRadialProjection = new JButton();
		separator1 = new JSeparator();
		textField2StatusVesselSegmentation = new JTextField();
		progressBarVesselSegmentation = new JProgressBar();
		panel3RadialProjection = new JPanel();
		labelFileNameRadialProjection = new JLabel();
		textFieldRadialProjection = new JTextField();
		buttonRunRadialProjection = new JButton();
		button2 = new JButton();
		button5 = new JButton();
		panel4Analysis = new JPanel();
		tabbedPane1 = new JTabbedPane();
		panel1 = new JPanel();
		button11 = new JButton();
		button12 = new JButton();
		button13 = new JButton();
		scrollPane3 = new JScrollPane();
		table3 = new JTable();
		panel2 = new JPanel();
		checkBox1 = new JCheckBox();
		panel8 = new JPanel();
		checkBox2 = new JCheckBox();

		//======== this ========
		Container contentPane = getContentPane();
		contentPane.setLayout(new BorderLayout());

		//======== tabbedPaneMainPane ========
		{
			tabbedPaneMainPane.setTabPlacement(SwingConstants.LEFT);
			tabbedPaneMainPane.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabbedPaneMainPane.setPreferredSize(null);

			//======== panelConvertCzi2Tif ========
			{
				panelConvertCzi2Tif.setLayout(new MigLayout(
					"hidemode 3,align center top",
					// columns
					"[41,fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]",
					// rows
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]"));

				//---- buttonBrowseConvertCzi2Tif ----
				buttonBrowseConvertCzi2Tif.setText("Browse");
				panelConvertCzi2Tif.add(buttonBrowseConvertCzi2Tif, "cell 0 1");

				//---- textFieldConvertCzi2Tif ----
				textFieldConvertCzi2Tif.setText("file/Path");
				textFieldConvertCzi2Tif.setEditable(false);
				panelConvertCzi2Tif.add(textFieldConvertCzi2Tif, "cell 1 1 8 1");

				//---- checkBoxBgSubConvertCzi2Tif ----
				checkBoxBgSubConvertCzi2Tif.setText("background subtraction: ");
				panelConvertCzi2Tif.add(checkBoxBgSubConvertCzi2Tif, "cell 0 2");

				//---- labelRollingConvertCzi2Tif ----
				labelRollingConvertCzi2Tif.setText("Rolling");
				panelConvertCzi2Tif.add(labelRollingConvertCzi2Tif, "cell 1 2");

				//---- spinnerRollingConvertCzi2Tif ----
				spinnerRollingConvertCzi2Tif.setModel(new SpinnerNumberModel(10, 1, null, 1));
				spinnerRollingConvertCzi2Tif.setEnabled(false);
				panelConvertCzi2Tif.add(spinnerRollingConvertCzi2Tif, "cell 2 2");

				//---- labelEnhanceConstConvertCzi2Tif ----
				labelEnhanceConstConvertCzi2Tif.setText("Enhance Constrast: ");
				panelConvertCzi2Tif.add(labelEnhanceConstConvertCzi2Tif, "cell 0 3");

				//---- labelSaturateConvertCzi2Tif ----
				labelSaturateConvertCzi2Tif.setText("Saturated");
				panelConvertCzi2Tif.add(labelSaturateConvertCzi2Tif, "cell 1 3");

				//---- spinnerSaturateConvertCzi2Tif ----
				spinnerSaturateConvertCzi2Tif.setModel(new SpinnerNumberModel(35, 0, 100, 1));
				panelConvertCzi2Tif.add(spinnerSaturateConvertCzi2Tif, "cell 2 3");

				//---- labelpercentSignConvertCzi2Tif ----
				labelpercentSignConvertCzi2Tif.setText("%");
				panelConvertCzi2Tif.add(labelpercentSignConvertCzi2Tif, "cell 3 3");

				//---- checkBoxRotateConvertCzi2Tif ----
				checkBoxRotateConvertCzi2Tif.setText("Rotate:");
				panelConvertCzi2Tif.add(checkBoxRotateConvertCzi2Tif, "cell 0 4");

				//---- comboBoxRoateDirectionConvertCzi2Tif ----
				comboBoxRoateDirectionConvertCzi2Tif.setEnabled(false);
				panelConvertCzi2Tif.add(comboBoxRoateDirectionConvertCzi2Tif, "cell 1 4 2 1");

				//---- buttonOkConvertCzi2Tif ----
				buttonOkConvertCzi2Tif.setText("OK");
				panelConvertCzi2Tif.add(buttonOkConvertCzi2Tif, "cell 0 5");

				//---- textFieldStatusConvertCzi2Tif ----
				textFieldStatusConvertCzi2Tif.setEditable(false);
				panelConvertCzi2Tif.add(textFieldStatusConvertCzi2Tif, "cell 0 6");
				panelConvertCzi2Tif.add(progressBarConvertCzi2Tif, "cell 1 6 3 1");
			}
			tabbedPaneMainPane.addTab("0. CZI to TIFF", panelConvertCzi2Tif);

			//======== panelVesselsSegmentation ========
			{
				panelVesselsSegmentation.setLayout(new MigLayout(
					"hidemode 3,align center top",
					// columns
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]",
					// rows
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]"));

				//======== tabbedPaneVesselSegmentation ========
				{

					//======== panelImageListVesselSegmentation ========
					{
						panelImageListVesselSegmentation.setLayout(new MigLayout(
							"hidemode 3,align center top",
							// columns
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]",
							// rows
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- buttonAddFile ----
						buttonAddFile.setText("ADD");
						panelImageListVesselSegmentation.add(buttonAddFile, "cell 0 0");

						//---- buttonAddFolder ----
						buttonAddFolder.setText("ADD FOLDER");
						panelImageListVesselSegmentation.add(buttonAddFolder, "cell 1 0");

						//---- buttonRemove ----
						buttonRemove.setText("REMOVE");
						panelImageListVesselSegmentation.add(buttonRemove, "cell 2 0");

						//---- buttonClear ----
						buttonClear.setText("CLEAR");
						panelImageListVesselSegmentation.add(buttonClear, "cell 3 0");

						//======== scrollPaneVesselSegmentation ========
						{

							//---- tableAddedFileVesselSegmentation ----
							tableAddedFileVesselSegmentation.setModel(new DefaultTableModel(
								new Object[][] {
									{null},
								},
								new String[] {
									"FILE PATHS"
								}
							));
							scrollPaneVesselSegmentation.setViewportView(tableAddedFileVesselSegmentation);
						}
						panelImageListVesselSegmentation.add(scrollPaneVesselSegmentation, "cell 0 1 4 1");
					}
					tabbedPaneVesselSegmentation.addTab("Images List", panelImageListVesselSegmentation);

					//======== panelParametersVesselSegmentation ========
					{
						panelParametersVesselSegmentation.setLayout(new MigLayout(
							"hidemode 3",
							// columns
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]",
							// rows
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- labelTargetXYPixelSize ----
						labelTargetXYPixelSize.setText("<html>target_xy pixel size(nm)</html>");
						panelParametersVesselSegmentation.add(labelTargetXYPixelSize, "cell 0 0");

						//---- spinnerXYPixelSizeCreateSideView ----
						spinnerXYPixelSizeCreateSideView.setModel(new SpinnerNumberModel(200, 0, null, 1));
						panelParametersVesselSegmentation.add(spinnerXYPixelSizeCreateSideView, "cell 1 0");

						//---- labelTargetZPixelSize ----
						labelTargetZPixelSize.setText("<html>target_z pixel size(nm)</html>");
						panelParametersVesselSegmentation.add(labelTargetZPixelSize, "cell 0 1");

						//---- spinnerZPixelSizeCreateSideView ----
						spinnerZPixelSizeCreateSideView.setModel(new SpinnerNumberModel(200, 0, null, 1));
						panelParametersVesselSegmentation.add(spinnerZPixelSizeCreateSideView, "cell 1 1");

						//---- labelAnalysisWindow ----
						labelAnalysisWindow.setText("<html>Analysis Window (\u03bcm)</html>");
						panelParametersVesselSegmentation.add(labelAnalysisWindow, "cell 0 2");

						//---- spinnerAnalysisWindow ----
						spinnerAnalysisWindow.setModel(new SpinnerNumberModel(1, 0, null, 1));
						panelParametersVesselSegmentation.add(spinnerAnalysisWindow, "cell 1 2");

						//---- labelPreWatershedSmoothing ----
						labelPreWatershedSmoothing.setText("<html>Pre-watershed <br>Smoothing</html>");
						panelParametersVesselSegmentation.add(labelPreWatershedSmoothing, "cell 0 3");

						//---- spinnerPreWatershedSmoothing ----
						spinnerPreWatershedSmoothing.setModel(new SpinnerNumberModel(2.0, 0.0, 5.0, 0.1));
						panelParametersVesselSegmentation.add(spinnerPreWatershedSmoothing, "cell 1 3");

						//---- labelSliceIndexforTuning ----
						labelSliceIndexforTuning.setText("<html>slice index for tuning</html>");
						panelParametersVesselSegmentation.add(labelSliceIndexforTuning, "cell 0 4");

						//---- spinnerSliceIndexForTuning ----
						spinnerSliceIndexForTuning.setModel(new SpinnerNumberModel(0, 0, null, 1));
						panelParametersVesselSegmentation.add(spinnerSliceIndexForTuning, "cell 1 4");

						//---- labelInnerVesselRadius ----
						labelInnerVesselRadius.setText("<html>Inner Vessel Radius (\u03bcm)</html>");
						panelParametersVesselSegmentation.add(labelInnerVesselRadius, "cell 0 5");

						//---- spinnerInnerVesselRadius ----
						spinnerInnerVesselRadius.setModel(new SpinnerNumberModel(1.0, 0.25, 30.0, 0.05));
						panelParametersVesselSegmentation.add(spinnerInnerVesselRadius, "cell 1 5");

						//---- labelHybridWeight ----
						labelHybridWeight.setText("<html>Hybrid-weighting of <br> lignin-to-cellulose(%)</html>");
						panelParametersVesselSegmentation.add(labelHybridWeight, "cell 0 6");

						//---- labelLigninHybridWeight ----
						labelLigninHybridWeight.setText("Lignin 100%");
						labelLigninHybridWeight.setHorizontalAlignment(SwingConstants.RIGHT);
						panelParametersVesselSegmentation.add(labelLigninHybridWeight, "cell 1 6");

						//---- sliderHybridWeight ----
						sliderHybridWeight.setValue(0);
						sliderHybridWeight.setPaintTicks(true);
						sliderHybridWeight.setMajorTickSpacing(25);
						panelParametersVesselSegmentation.add(sliderHybridWeight, "cell 2 6");

						//---- labelCelluloseHybridWeight ----
						labelCelluloseHybridWeight.setText("Cellulose 0%");
						labelCelluloseHybridWeight.setHorizontalAlignment(SwingConstants.LEFT);
						panelParametersVesselSegmentation.add(labelCelluloseHybridWeight, "cell 3 6");

						//---- buttonCreateSideView ----
						buttonCreateSideView.setText("Create Side View");
						panelParametersVesselSegmentation.add(buttonCreateSideView, "cell 0 7");

						//---- buttonProjAndSmooth ----
						buttonProjAndSmooth.setText("<html>Projection and <br> smoothing</html>");
						buttonProjAndSmooth.setEnabled(false);
						panelParametersVesselSegmentation.add(buttonProjAndSmooth, "cell 1 7");

						//---- buttonSelectCentroid ----
						buttonSelectCentroid.setText("Select Centroid");
						buttonSelectCentroid.setEnabled(false);
						panelParametersVesselSegmentation.add(buttonSelectCentroid, "cell 0 8");

						//---- buttonWatershed ----
						buttonWatershed.setText("Watershed");
						buttonWatershed.setEnabled(false);
						panelParametersVesselSegmentation.add(buttonWatershed, "cell 1 8");

						//---- buttonProcessWholeStack ----
						buttonProcessWholeStack.setText("Process Whole Stack");
						buttonProcessWholeStack.setEnabled(false);
						panelParametersVesselSegmentation.add(buttonProcessWholeStack, "cell 0 9");

						//---- buttonMoveToRadialProjection ----
						buttonMoveToRadialProjection.setText("Move to next Step");
						buttonMoveToRadialProjection.setEnabled(false);
						panelParametersVesselSegmentation.add(buttonMoveToRadialProjection, "cell 1 9");
						panelParametersVesselSegmentation.add(separator1, "cell 0 10 2 1");

						//---- textField2StatusVesselSegmentation ----
						textField2StatusVesselSegmentation.setEditable(false);
						textField2StatusVesselSegmentation.setBorder(null);
						panelParametersVesselSegmentation.add(textField2StatusVesselSegmentation, "cell 0 11 2 1");
						panelParametersVesselSegmentation.add(progressBarVesselSegmentation, "cell 2 11 2 1");
					}
					tabbedPaneVesselSegmentation.addTab("Parameters", panelParametersVesselSegmentation);
				}
				panelVesselsSegmentation.add(tabbedPaneVesselSegmentation, "cell 0 0 12 7");
			}
			tabbedPaneMainPane.addTab("1. Vessels Segmentation", panelVesselsSegmentation);

			//======== panel3RadialProjection ========
			{
				panel3RadialProjection.setLayout(new MigLayout(
					"hidemode 3,align center top",
					// columns
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]",
					// rows
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]"));

				//---- labelFileNameRadialProjection ----
				labelFileNameRadialProjection.setText("File name ");
				panel3RadialProjection.add(labelFileNameRadialProjection, "cell 0 0");

				//---- textFieldRadialProjection ----
				textFieldRadialProjection.setEditable(false);
				panel3RadialProjection.add(textFieldRadialProjection, "cell 1 0 5 1");

				//---- buttonRunRadialProjection ----
				buttonRunRadialProjection.setText("Radial Projection");
				panel3RadialProjection.add(buttonRunRadialProjection, "cell 0 1 2 1");

				//---- button2 ----
				button2.setText("Unrolling Vessel");
				panel3RadialProjection.add(button2, "cell 0 2 2 1");

				//---- button5 ----
				button5.setText("Move to Analysis");
				button5.setEnabled(false);
				panel3RadialProjection.add(button5, "cell 0 3 2 1");
			}
			tabbedPaneMainPane.addTab("2. Radial Projection", panel3RadialProjection);

			//======== panel4Analysis ========
			{
				panel4Analysis.setLayout(new MigLayout(
					"hidemode 3,align center top",
					// columns
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]" +
					"[fill]",
					// rows
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]" +
					"[]"));

				//======== tabbedPane1 ========
				{

					//======== panel1 ========
					{
						panel1.setLayout(new MigLayout(
							"hidemode 3,align center top",
							// columns
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]",
							// rows
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- button11 ----
						button11.setText("add");
						panel1.add(button11, "cell 0 0");

						//---- button12 ----
						button12.setText("Remove");
						panel1.add(button12, "cell 1 0");

						//---- button13 ----
						button13.setText("OK");
						panel1.add(button13, "cell 2 0");

						//======== scrollPane3 ========
						{
							scrollPane3.setViewportView(table3);
						}
						panel1.add(scrollPane3, "cell 0 1 3 4");
					}
					tabbedPane1.addTab("Image", panel1);

					//======== panel2 ========
					{
						panel2.setLayout(new MigLayout(
							"hidemode 3,align center top",
							// columns
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]",
							// rows
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- checkBox1 ----
						checkBox1.setText("use this parameter");
						panel2.add(checkBox1, "cell 0 0");
					}
					tabbedPane1.addTab("Parameter_1", panel2);

					//======== panel8 ========
					{
						panel8.setLayout(new MigLayout(
							"hidemode 3,align center top",
							// columns
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]" +
							"[fill]",
							// rows
							"[]" +
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- checkBox2 ----
						checkBox2.setText("use this parameter");
						panel8.add(checkBox2, "cell 0 0");
					}
					tabbedPane1.addTab("Parameter_2", panel8);
				}
				panel4Analysis.add(tabbedPane1, "cell 0 1");
			}
			tabbedPaneMainPane.addTab("3. Analysis", panel4Analysis);
		}
		contentPane.add(tabbedPaneMainPane, BorderLayout.NORTH);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
	// Generated using JFormDesigner Educational license - Anh Minh Do
	private JTabbedPane tabbedPaneMainPane;
	private JPanel panelConvertCzi2Tif;
	private JButton buttonBrowseConvertCzi2Tif;
	private JTextField textFieldConvertCzi2Tif;
	private JCheckBox checkBoxBgSubConvertCzi2Tif;
	private JLabel labelRollingConvertCzi2Tif;
	private JSpinner spinnerRollingConvertCzi2Tif;
	private JLabel labelEnhanceConstConvertCzi2Tif;
	private JLabel labelSaturateConvertCzi2Tif;
	private JSpinner spinnerSaturateConvertCzi2Tif;
	private JLabel labelpercentSignConvertCzi2Tif;
	private JCheckBox checkBoxRotateConvertCzi2Tif;
	private JComboBox comboBoxRoateDirectionConvertCzi2Tif;
	private JButton buttonOkConvertCzi2Tif;
	private JTextField textFieldStatusConvertCzi2Tif;
	private JProgressBar progressBarConvertCzi2Tif;
	private JPanel panelVesselsSegmentation;
	private JTabbedPane tabbedPaneVesselSegmentation;
	private JPanel panelImageListVesselSegmentation;
	private JButton buttonAddFile;
	private JButton buttonAddFolder;
	private JButton buttonRemove;
	private JButton buttonClear;
	private JScrollPane scrollPaneVesselSegmentation;
	private JTable tableAddedFileVesselSegmentation;
	private JPanel panelParametersVesselSegmentation;
	private JLabel labelTargetXYPixelSize;
	private JSpinner spinnerXYPixelSizeCreateSideView;
	private JLabel labelTargetZPixelSize;
	private JSpinner spinnerZPixelSizeCreateSideView;
	private JLabel labelAnalysisWindow;
	private JSpinner spinnerAnalysisWindow;
	private JLabel labelPreWatershedSmoothing;
	private JSpinner spinnerPreWatershedSmoothing;
	private JLabel labelSliceIndexforTuning;
	private JSpinner spinnerSliceIndexForTuning;
	private JLabel labelInnerVesselRadius;
	private JSpinner spinnerInnerVesselRadius;
	private JLabel labelHybridWeight;
	private JLabel labelLigninHybridWeight;
	private JSlider sliderHybridWeight;
	private JLabel labelCelluloseHybridWeight;
	private JButton buttonCreateSideView;
	private JButton buttonProjAndSmooth;
	private JButton buttonSelectCentroid;
	private JButton buttonWatershed;
	private JButton buttonProcessWholeStack;
	private JButton buttonMoveToRadialProjection;
	private JSeparator separator1;
	private JTextField textField2StatusVesselSegmentation;
	private JProgressBar progressBarVesselSegmentation;
	private JPanel panel3RadialProjection;
	private JLabel labelFileNameRadialProjection;
	private JTextField textFieldRadialProjection;
	private JButton buttonRunRadialProjection;
	private JButton button2;
	private JButton button5;
	private JPanel panel4Analysis;
	private JTabbedPane tabbedPane1;
	private JPanel panel1;
	private JButton button11;
	private JButton button12;
	private JButton button13;
	private JScrollPane scrollPane3;
	private JTable table3;
	private JPanel panel2;
	private JCheckBox checkBox1;
	private JPanel panel8;
	private JCheckBox checkBox2;
	// JFormDesigner - End of variables declaration  //GEN-END:variables  @formatter:on
}
