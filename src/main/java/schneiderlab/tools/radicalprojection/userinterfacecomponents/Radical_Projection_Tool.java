package schneiderlab.tools.radicalprojection.userinterfacecomponents;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import schneiderlab.tools.radicalprojection.imageprocessor.core.convertczitotif.CZIProcessor;
import schneiderlab.tools.radicalprojection.imageprocessor.core.createsideview.CreateSideView;
import schneiderlab.tools.radicalprojection.imageprocessor.core.segmentation.CreateHybridStack;
import schneiderlab.tools.radicalprojection.imageprocessor.core.segmentation.DataDuringSegmentationProcess;
import schneiderlab.tools.radicalprojection.imageprocessor.core.segmentation.Reconstruction;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.PointRoi;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.array.ArrayImgs;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.Intervals;
import net.imglib2.view.Views;
import net.miginfocom.swing.*;
import org.scijava.Context;
import schneiderlab.tools.radicalprojection.uiaction.AddFilePathToTable;
import schneiderlab.tools.radicalprojection.uiaction.RemoveFilePathFromTable;

/*
 * Created by JFormDesigner on Fri Apr 04 02:15:10 CEST 2025
 */

/**
 * @author anhminh
 */
public class Radical_Projection_Tool extends JFrame {

	private ArrayList<Path> processedFileInCreateSideView;
	private Context context;
	private DataDuringSegmentationProcess dataAfterSmoothed;
	private ArrayList<Point> coordinates = new ArrayList<>() ;
	private ArrayList<Point> coordinatesBatch = new ArrayList<>() ;
	private Overlay overlaySegmentation;
	private ImagePlus impInByte;

	public Radical_Projection_Tool(Context context) {
		initComponents();
		table1.setModel(new DefaultTableModel(new Object[]{"File Path"}, 0));
		table4.setModel(new DefaultTableModel(new Object[]{"File Path"}, 0));
		
		// Action for Browse button in Converting step
		button5.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(Radical_Projection_Tool.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					textField1.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}
		});

		// Action for checkbox backgroundSubstraction in Converting step
		checkBox3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinner5.setEnabled(checkBox3.isSelected());
			}
		});
		// Action for checkbox Rotate in Converting step
		checkBox4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboBox1.setEnabled(checkBox4.isSelected());
			}
		});

		// Action for OK button in Converting step
		button1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String folderPath = textField1.getText();
				int rolling = (int) spinner5.getValue();
				int saturated = (int) spinner6.getValue();
				String rotateDirection = (String) comboBox1.getSelectedItem();
				boolean isRotate = checkBox4.isSelected();
				boolean isBackgroundSubtraction = checkBox3.isSelected();
				boolean fixArtifact = checkBox5.isSelected();
				CZIProcessor.processCZItoTIFF(folderPath,
						isBackgroundSubtraction,
						rolling,
						saturated,
						isRotate,
						rotateDirection,
						fixArtifact);
			}
		});
		// Action for ADD button in step 1
		button2.addActionListener(new AddFilePathToTable(table1, Radical_Projection_Tool.this));

		// Action for REMOVE Button in step 1
//		button6.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				int selectedRow = table1.getSelectedRow();
//				if (selectedRow != -1) {
//					DefaultTableModel model = (DefaultTableModel) table1.getModel();
//					model.removeRow(selectedRow);
//				}
//			}
//		});
		button6.addActionListener(new RemoveFilePathFromTable(table1));

		// tooltip to view full file path in step 1
		table1.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = table1.rowAtPoint(e.getPoint());
				int col = table1.columnAtPoint(e.getPoint());
				if (row > -1 && col > -1) {
					Object value = table1.getValueAt(row, col);
					table1.setToolTipText(value != null ? value.toString() : null);
				}
			}
		});

		// button add folder for create side view, step 1
		button15.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(Radical_Projection_Tool.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File dir = chooser.getSelectedFile();
					File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".tif"));
					if (files != null) {
						DefaultTableModel model = (DefaultTableModel) table1.getModel();
						for (File file : files) {
							model.addRow(new Object[]{file.getAbsolutePath()});
						}
					}
				}
			}
		});
		// button clear all in table for create side view, step 1
		button16.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table1.getModel();
				model.setRowCount(0);
			}
		});

		// Process button in create side view step
		button7.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				IJ.log("Processing ....");
				label14.setText("Processing ....");
				SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
					ArrayList<Path> outFilePaths = new ArrayList<>();
					@Override
					protected Void doInBackground() throws Exception {
						// get all file paths from table
						DefaultTableModel model = (DefaultTableModel) table1.getModel();
						int rowCount = model.getRowCount();
						ArrayList<Path> filePaths = new ArrayList<>();
						for (int i = 0; i < rowCount; i++) {
							filePaths.add(Paths.get(model.getValueAt(i, 0).toString()));
						}
						// get input value for the targeted pixel size
						int targetXYpixelSize = (int) spinner9.getValue();
						int targetZpixelSize = (int) spinner10.getValue();
						// perform the processing
						for (Path filePath : filePaths){
							try {
								CreateSideView crsv = new CreateSideView(context,filePath,targetXYpixelSize, targetZpixelSize);
								Path outputFilePath = crsv.process();
								outFilePaths.add(outputFilePath);
							} catch (Exception ex) {
								throw new RuntimeException(ex);
							}

						}
						return null;
					}
					@Override
					protected void done() {
						label14.setText("Finish processing all files");
						IJ.log("Finish processing all files");
						IJ.log(outFilePaths.toString());
						processedFileInCreateSideView = outFilePaths;
					}
				};
				worker.execute();
            }
		});

		// button move file to next stage
		button23.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table4.getModel();
				for (Path path : processedFileInCreateSideView) {
					model.addRow(new Object[]{path.toAbsolutePath().toString()});
				}
			}
		});

		// Action for ADD button in segmentation step
		button17.addActionListener(new AddFilePathToTable(table4,Radical_Projection_Tool.this));

		// Action for REMOVE Button in segmentation step
//		button19.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				int selectedRow = table4.getSelectedRow();
//				if (selectedRow != -1) {
//					DefaultTableModel model = (DefaultTableModel) table4.getModel();
//					model.removeRow(selectedRow);
//				}
//			}
//		});
		button19.addActionListener(new RemoveFilePathFromTable(table4));

		// tooltip to view full file path in segmentation step
		table4.addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int row = table4.rowAtPoint(e.getPoint());
				int col = table4.columnAtPoint(e.getPoint());
				if (row > -1 && col > -1) {
					Object value = table4.getValueAt(row, col);
					table4.setToolTipText(value != null ? value.toString() : null);
				}
			}
		});

		// button add folder for segmentation step
		button18.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int returnVal = chooser.showOpenDialog(Radical_Projection_Tool.this);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File dir = chooser.getSelectedFile();
					File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".tif"));
					if (files != null) {
						DefaultTableModel model = (DefaultTableModel) table4.getModel();
						for (File file : files) {
							model.addRow(new Object[]{file.getAbsolutePath()});
						}
					}
				}
			}
		});
		// button clear all in table for segmentation step
		button20.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table4.getModel();
				model.setRowCount(0);
			}
		});
		// Slider update the percentage when the value change
		slider1.addChangeListener(new ChangeListener(){
			@Override
			public void stateChanged(ChangeEvent e){
				int currentValue = slider1.getValue();
				label7.setText("Lignin " + (100-currentValue) + "%");
				label8.setText("Cellulose " + currentValue + "%");
			}
		});

		// button projection and smoothing in segmentation step
		button22.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultTableModel model = (DefaultTableModel) table4.getModel();
				int rowCount = model.getRowCount();
				if(rowCount > 0){
					label13.setForeground(Color.RED);
					label13.setText("Creating hybrid stack and smoothing...");
					SwingWorker<DataDuringSegmentationProcess, Void> hybridStackWorker = new SwingWorker<DataDuringSegmentationProcess, Void>() {
						@Override
						protected DataDuringSegmentationProcess doInBackground() throws Exception {
							// get all file paths from table
//						DefaultTableModel model = (DefaultTableModel) table4.getModel();
//						int rowCount = model.getRowCount();
							int sliderValue = slider1.getValue();
							int windowSizeinMicroMeter = (int)spinner1.getValue();
							int windowSize = Math.round(windowSizeinMicroMeter/0.2f);
							double sigmaValueFilter = (double) spinner2.getValue();
							int diameter = (int) spinner4.getValue();
							ArrayList<Path> filePaths = new ArrayList<>();
							for (int i = 0; i < rowCount; i++) {
								filePaths.add(Paths.get(model.getValueAt(i, 0).toString()));
							}
							CreateHybridStack chs = new CreateHybridStack(context,filePaths.get(0),
									sliderValue,
									windowSize,
									sigmaValueFilter,
									diameter);
							return chs.process();
						}
						@Override
						protected void done() {
							try {
								dataAfterSmoothed = get();
								IJ.showStatus("Processing finished");
								label13.setForeground(Color.GREEN);
								label13.setText("Processing finished");
								button4.setEnabled(true);
							} catch (InterruptedException | ExecutionException ex) {
								throw new RuntimeException(ex);
							}
						}
					};
					hybridStackWorker.execute();
				} else{
					label13.setForeground(Color.BLACK);
					label13.setText("Please add files to process");
				}
			}
		});

		// button select Centroid to view the image and let user
		button4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RandomAccessibleInterval<FloatType>	smoothedStack = dataAfterSmoothed.getSmoothStack();
				RandomAccessibleInterval<FloatType> just1Slide = Views.hyperSlice(smoothedStack,2,0);
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
				// Calculate window size for 200% zoom
				int imgWidth = impInByte.getWidth() * (int) magnificationLevel;
				int imgHeight = impInByte.getHeight() * (int) magnificationLevel;
				// position the window at  bottom left
				int xlocation = 10;
				int ylocation = screenHeight-imgHeight-((int)screenHeight*2/100); // screenHeight*5/100 to create a little bit space
				ImageWindow window = impInByte.getWindow();
				window.setLocationAndSize( xlocation,ylocation ,imgWidth,imgHeight);

				canvas.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						// Add point to the PointRoi
						int x = canvas.offScreenX(e.getX());
						int y = canvas.offScreenY(e.getY());
						Point pointLatest = new Point(x,y);
						coordinates.add(pointLatest);
						IJ.log(coordinates.toString());
						button3.setEnabled(true);
					}
				});
				}
		});

		// button Watershed to segment the image
		button3.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingWorker<Void, Void> segmentationWorker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						Reconstruction recon = new Reconstruction(dataAfterSmoothed,coordinates);
						overlaySegmentation = recon.process1Slide();
						return null;
					}
					@Override
					protected void done(){
						coordinatesBatch.clear();
						coordinatesBatch.addAll(coordinates);
						coordinates.clear();
						button3.setEnabled(false);
						impInByte.setOverlay(overlaySegmentation);
						impInByte.updateAndDraw();
					}
				};
				segmentationWorker.execute();
			}
		});

		// button processing wholeStack
		button21.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				label13.setForeground(Color.RED);
				label13.setText("Processing whole stack...");
				SwingWorker<Void, Void> batchSegmentationWorker = new SwingWorker<Void, Void>() {
					@Override
					protected Void doInBackground() throws Exception {
						Reconstruction recon = new Reconstruction(dataAfterSmoothed,coordinatesBatch);
						recon.processWholeStack();
						return null;
					}
					@Override
					protected void done(){
						label13.setForeground(Color.GREEN);
						label13.setText("Processing whole stack finished");
					}
				};
				batchSegmentationWorker.execute();
			}
		});
	}

	private void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents  @formatter:off
		// Generated using JFormDesigner Educational license - Anh Minh Do
		tabbedPane2 = new JTabbedPane();
		panel3 = new JPanel();
		button5 = new JButton();
		textField1 = new JTextField();
		checkBox3 = new JCheckBox();
		label9 = new JLabel();
		spinner5 = new JSpinner();
		label10 = new JLabel();
		label11 = new JLabel();
		spinner6 = new JSpinner();
		label12 = new JLabel();
		checkBox4 = new JCheckBox();
		comboBox1 = new JComboBox<>();
		checkBox5 = new JCheckBox();
		button1 = new JButton();
		panel4 = new JPanel();
		tabbedPane3 = new JTabbedPane();
		panel9 = new JPanel();
		button2 = new JButton();
		button15 = new JButton();
		button6 = new JButton();
		button16 = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel10 = new JPanel();
		label17 = new JLabel();
		spinner9 = new JSpinner();
		label18 = new JLabel();
		label19 = new JLabel();
		spinner10 = new JSpinner();
		label20 = new JLabel();
		button7 = new JButton();
		button23 = new JButton();
		label22 = new JLabel();
		label14 = new JLabel();
		panel5 = new JPanel();
		tabbedPane4 = new JTabbedPane();
		panel11 = new JPanel();
		button17 = new JButton();
		button18 = new JButton();
		button19 = new JButton();
		button20 = new JButton();
		scrollPane4 = new JScrollPane();
		table4 = new JTable();
		panel12 = new JPanel();
		label1 = new JLabel();
		spinner1 = new JSpinner();
		label2 = new JLabel();
		spinner2 = new JSpinner();
		label3 = new JLabel();
		spinner3 = new JSpinner();
		label4 = new JLabel();
		spinner4 = new JSpinner();
		label5 = new JLabel();
		label7 = new JLabel();
		slider1 = new JSlider();
		label8 = new JLabel();
		label6 = new JLabel();
		label13 = new JLabel();
		button22 = new JButton();
		button4 = new JButton();
		button3 = new JButton();
		button21 = new JButton();
		panel6 = new JPanel();
		button8 = new JButton();
		button9 = new JButton();
		button10 = new JButton();
		button14 = new JButton();
		scrollPane2 = new JScrollPane();
		table2 = new JTable();
		panel7 = new JPanel();
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

		//======== tabbedPane2 ========
		{
			tabbedPane2.setTabPlacement(SwingConstants.LEFT);
			tabbedPane2.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
			tabbedPane2.setPreferredSize(null);

			//======== panel3 ========
			{
				panel3.setLayout(new MigLayout(
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

				//---- button5 ----
				button5.setText("Browse");
				panel3.add(button5, "cell 0 1");

				//---- textField1 ----
				textField1.setText("file/Path");
				panel3.add(textField1, "cell 2 1 10 1");

				//---- checkBox3 ----
				checkBox3.setText("background subtraction: ");
				panel3.add(checkBox3, "cell 0 2");

				//---- label9 ----
				label9.setText("Rolling");
				panel3.add(label9, "cell 2 2");

				//---- spinner5 ----
				spinner5.setModel(new SpinnerNumberModel(10, 1, null, 1));
				spinner5.setEnabled(false);
				panel3.add(spinner5, "cell 3 2");

				//---- label10 ----
				label10.setText("Enhance Constrast: ");
				panel3.add(label10, "cell 0 3");

				//---- label11 ----
				label11.setText("Saturated");
				panel3.add(label11, "cell 2 3");

				//---- spinner6 ----
				spinner6.setModel(new SpinnerNumberModel(35, 0, 100, 1));
				panel3.add(spinner6, "cell 3 3");

				//---- label12 ----
				label12.setText("%");
				panel3.add(label12, "cell 4 3");

				//---- checkBox4 ----
				checkBox4.setText("Rotate:");
				panel3.add(checkBox4, "cell 0 4");

				//---- comboBox1 ----
				comboBox1.setModel(new DefaultComboBoxModel<>(new String[] {
					"90 Degrees Left",
					"90 Degrees Right"
				}));
				comboBox1.setEnabled(false);
				panel3.add(comboBox1, "cell 2 4 2 1");

				//---- checkBox5 ----
				checkBox5.setText("Fix LSM880 stripe artifacts");
				checkBox5.setToolTipText("corrects for horizontal stripe patterns in case the bi-directional settings of the LSM880 are offset");
				panel3.add(checkBox5, "cell 0 5");

				//---- button1 ----
				button1.setText("OK");
				panel3.add(button1, "cell 0 6 2 1");
			}
			tabbedPane2.addTab("0. CZI to TIFF", panel3);

			//======== panel4 ========
			{
				panel4.setLayout(new MigLayout(
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
					"[]"));

				//======== tabbedPane3 ========
				{

					//======== panel9 ========
					{
						panel9.setLayout(new MigLayout(
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
							"[]"));

						//---- button2 ----
						button2.setText("ADD");
						panel9.add(button2, "cell 0 0 2 1");

						//---- button15 ----
						button15.setText("ADD FOLDER");
						panel9.add(button15, "cell 2 0");

						//---- button6 ----
						button6.setText("REMOVE");
						panel9.add(button6, "cell 3 0 2 1");

						//---- button16 ----
						button16.setText("CLEAR");
						panel9.add(button16, "cell 5 0");

						//======== scrollPane1 ========
						{

							//---- table1 ----
							table1.setModel(new DefaultTableModel(
								new Object[][] {
									{null},
								},
								new String[] {
									"File Paths"
								}
							) {
								Class<?>[] columnTypes = new Class<?>[] {
									String.class
								};
								@Override
								public Class<?> getColumnClass(int columnIndex) {
									return columnTypes[columnIndex];
								}
							});
							scrollPane1.setViewportView(table1);
						}
						panel9.add(scrollPane1, "cell 0 1 6 1");
					}
					tabbedPane3.addTab("images list", panel9);

					//======== panel10 ========
					{
						panel10.setLayout(new MigLayout(
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

						//---- label17 ----
						label17.setText("target_xy pixel size");
						panel10.add(label17, "cell 0 0");

						//---- spinner9 ----
						spinner9.setModel(new SpinnerNumberModel(200, 0, null, 1));
						panel10.add(spinner9, "cell 1 0");

						//---- label18 ----
						label18.setText("nm");
						panel10.add(label18, "cell 2 0");

						//---- label19 ----
						label19.setText("target_z pixel size");
						panel10.add(label19, "cell 0 1");

						//---- spinner10 ----
						spinner10.setModel(new SpinnerNumberModel(200, 0, null, 1));
						panel10.add(spinner10, "cell 1 1");

						//---- label20 ----
						label20.setText("nm");
						panel10.add(label20, "cell 2 1");

						//---- button7 ----
						button7.setText("PROCESS");
						panel10.add(button7, "cell 0 2");

						//---- button23 ----
						button23.setText("Move processed files to next stage");
						panel10.add(button23, "cell 1 2");

						//---- label22 ----
						label22.setText("Status:");
						panel10.add(label22, "cell 0 3");

						//---- label14 ----
						label14.setText("Waiting...");
						panel10.add(label14, "cell 1 3");
					}
					tabbedPane3.addTab("parameters", panel10);
				}
				panel4.add(tabbedPane3, "cell 0 0 12 6");
			}
			tabbedPane2.addTab("1. Create side-view Projections", panel4);

			//======== panel5 ========
			{
				panel5.setLayout(new MigLayout(
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

				//======== tabbedPane4 ========
				{

					//======== panel11 ========
					{
						panel11.setLayout(new MigLayout(
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
							"[]"));

						//---- button17 ----
						button17.setText("ADD");
						panel11.add(button17, "cell 0 0");

						//---- button18 ----
						button18.setText("ADD FOLDER");
						panel11.add(button18, "cell 1 0");

						//---- button19 ----
						button19.setText("REMOVE");
						panel11.add(button19, "cell 2 0");

						//---- button20 ----
						button20.setText("CLEAR");
						panel11.add(button20, "cell 3 0");

						//======== scrollPane4 ========
						{

							//---- table4 ----
							table4.setModel(new DefaultTableModel(
								new Object[][] {
									{null},
								},
								new String[] {
									"FILE PATHS"
								}
							));
							scrollPane4.setViewportView(table4);
						}
						panel11.add(scrollPane4, "cell 0 1 4 1");
					}
					tabbedPane4.addTab("Images List", panel11);

					//======== panel12 ========
					{
						panel12.setLayout(new MigLayout(
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
							"[]" +
							"[]" +
							"[]" +
							"[]"));

						//---- label1 ----
						label1.setText("Length of Long-axis Analysis Window (\u03bcm)");
						panel12.add(label1, "cell 0 0");

						//---- spinner1 ----
						spinner1.setModel(new SpinnerNumberModel(1, 0, null, 1));
						panel12.add(spinner1, "cell 1 0");

						//---- label2 ----
						label2.setText("Pre-watershed Smoothing");
						panel12.add(label2, "cell 0 1");

						//---- spinner2 ----
						spinner2.setModel(new SpinnerNumberModel(2.0, 0.0, 5.0, 0.1));
						panel12.add(spinner2, "cell 1 1");
						panel12.add(label3, "cell 0 2");
						panel12.add(spinner3, "cell 1 2");

						//---- label4 ----
						label4.setText("Inner Vessel Diameter [\u03bcm]");
						panel12.add(label4, "cell 0 3");

						//---- spinner4 ----
						spinner4.setModel(new SpinnerNumberModel(1, 0, 30, 1));
						panel12.add(spinner4, "cell 1 3");

						//---- label5 ----
						label5.setText("Hybrid-weighting of lignin-to-cellulose[%]");
						panel12.add(label5, "cell 0 4");

						//---- label7 ----
						label7.setText("Lignin 100%");
						label7.setHorizontalAlignment(SwingConstants.RIGHT);
						panel12.add(label7, "cell 1 4");

						//---- slider1 ----
						slider1.setValue(0);
						slider1.setPaintTicks(true);
						slider1.setMajorTickSpacing(25);
						panel12.add(slider1, "cell 2 4");

						//---- label8 ----
						label8.setText("Cellulose 0%");
						label8.setHorizontalAlignment(SwingConstants.LEFT);
						panel12.add(label8, "cell 3 4");

						//---- label6 ----
						label6.setText("Status: ");
						label6.setHorizontalAlignment(SwingConstants.RIGHT);
						panel12.add(label6, "cell 0 5");

						//---- label13 ----
						label13.setText("Waiting...");
						panel12.add(label13, "cell 1 5 3 1");

						//---- button22 ----
						button22.setText("Projection and smoothing");
						panel12.add(button22, "cell 0 6");

						//---- button4 ----
						button4.setText("Select Centroid");
						button4.setEnabled(false);
						panel12.add(button4, "cell 1 6");

						//---- button3 ----
						button3.setText("Watershed");
						button3.setEnabled(false);
						panel12.add(button3, "cell 2 6");

						//---- button21 ----
						button21.setText("Process Whole Stack");
						panel12.add(button21, "cell 3 6");
					}
					tabbedPane4.addTab("Parameters", panel12);
				}
				panel5.add(tabbedPane4, "cell 0 0 12 7");
			}
			tabbedPane2.addTab("2. Initialization of Vessel Extraction", panel5);

			//======== panel6 ========
			{
				panel6.setLayout(new MigLayout(
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

				//---- button8 ----
				button8.setText("ADD");
				panel6.add(button8, "cell 0 0");

				//---- button9 ----
				button9.setText("REMOVE");
				panel6.add(button9, "cell 2 0");

				//---- button10 ----
				button10.setText("OK");
				panel6.add(button10, "cell 3 0");

				//---- button14 ----
				button14.setText("Plot 2 radial projection");
				panel6.add(button14, "cell 0 1");

				//======== scrollPane2 ========
				{
					scrollPane2.setViewportView(table2);
				}
				panel6.add(scrollPane2, "cell 0 5 4 1");
			}
			tabbedPane2.addTab("3. Execute vessel segmentation", panel6);

			//======== panel7 ========
			{
				panel7.setLayout(new MigLayout(
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
				panel7.add(tabbedPane1, "cell 0 1");
			}
			tabbedPane2.addTab("4. Analysis", panel7);
		}
		contentPane.add(tabbedPane2, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(getOwner());
		// JFormDesigner - End of component initialization  //GEN-END:initComponents  @formatter:on
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables  @formatter:off
	// Generated using JFormDesigner Educational license - Anh Minh Do
	private JTabbedPane tabbedPane2;
	private JPanel panel3;
	private JButton button5;
	private JTextField textField1;
	private JCheckBox checkBox3;
	private JLabel label9;
	private JSpinner spinner5;
	private JLabel label10;
	private JLabel label11;
	private JSpinner spinner6;
	private JLabel label12;
	private JCheckBox checkBox4;
	private JComboBox<String> comboBox1;
	private JCheckBox checkBox5;
	private JButton button1;
	private JPanel panel4;
	private JTabbedPane tabbedPane3;
	private JPanel panel9;
	private JButton button2;
	private JButton button15;
	private JButton button6;
	private JButton button16;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel10;
	private JLabel label17;
	private JSpinner spinner9;
	private JLabel label18;
	private JLabel label19;
	private JSpinner spinner10;
	private JLabel label20;
	private JButton button7;
	private JButton button23;
	private JLabel label22;
	private JLabel label14;
	private JPanel panel5;
	private JTabbedPane tabbedPane4;
	private JPanel panel11;
	private JButton button17;
	private JButton button18;
	private JButton button19;
	private JButton button20;
	private JScrollPane scrollPane4;
	private JTable table4;
	private JPanel panel12;
	private JLabel label1;
	private JSpinner spinner1;
	private JLabel label2;
	private JSpinner spinner2;
	private JLabel label3;
	private JSpinner spinner3;
	private JLabel label4;
	private JSpinner spinner4;
	private JLabel label5;
	private JLabel label7;
	private JSlider slider1;
	private JLabel label8;
	private JLabel label6;
	private JLabel label13;
	private JButton button22;
	private JButton button4;
	private JButton button3;
	private JButton button21;
	private JPanel panel6;
	private JButton button8;
	private JButton button9;
	private JButton button10;
	private JButton button14;
	private JScrollPane scrollPane2;
	private JTable table2;
	private JPanel panel7;
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
