package UIDesign;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.*;
import net.miginfocom.swing.*;

/*
 * Created by JFormDesigner on Fri Apr 04 02:15:10 CEST 2025
 */

/**
 * @author anhminh
 */
public class Radical_Projection_Tool extends JFrame {
	public Radical_Projection_Tool() {
		initComponents();

		// Action for Browse button
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

		// Action for checkbox backgroundSubstraction
		checkBox3.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				spinner5.setEnabled(checkBox3.isSelected());
			}
		});
		// Action for checkbox backgroundSubstraction
		checkBox4.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				comboBox1.setEnabled(checkBox4.isSelected());
			}
		});

		// Action for OK button
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
		button6 = new JButton();
		scrollPane1 = new JScrollPane();
		table1 = new JTable();
		panel10 = new JPanel();
		checkBox6 = new JCheckBox();
		comboBox2 = new JComboBox<>();
		checkBox7 = new JCheckBox();
		spinner7 = new JSpinner();
		label14 = new JLabel();
		label15 = new JLabel();
		spinner8 = new JSpinner();
		label16 = new JLabel();
		label17 = new JLabel();
		spinner9 = new JSpinner();
		label18 = new JLabel();
		label19 = new JLabel();
		spinner10 = new JSpinner();
		label20 = new JLabel();
		button7 = new JButton();
		panel5 = new JPanel();
		label1 = new JLabel();
		spinner1 = new JSpinner();
		label3 = new JLabel();
		spinner2 = new JSpinner();
		label4 = new JLabel();
		spinner3 = new JSpinner();
		label5 = new JLabel();
		spinner4 = new JSpinner();
		label6 = new JLabel();
		label2 = new JLabel();
		slider1 = new JSlider();
		label7 = new JLabel();
		label8 = new JLabel();
		button3 = new JButton();
		button4 = new JButton();
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

						//---- button6 ----
						button6.setText("REMOVE");
						panel9.add(button6, "cell 3 0 2 1");

						//======== scrollPane1 ========
						{
							scrollPane1.setViewportView(table1);
						}
						panel9.add(scrollPane1, "cell 0 1 5 1");
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

						//---- checkBox6 ----
						checkBox6.setText("Rotate:");
						panel10.add(checkBox6, "cell 0 0");

						//---- comboBox2 ----
						comboBox2.setModel(new DefaultComboBoxModel<>(new String[] {
							"90 Degrees Left",
							"90 Degrees Right"
						}));
						comboBox2.setEnabled(false);
						panel10.add(comboBox2, "cell 1 0 2 1");

						//---- checkBox7 ----
						checkBox7.setText("xy pixel size [nm]");
						panel10.add(checkBox7, "cell 0 1");

						//---- spinner7 ----
						spinner7.setEnabled(false);
						panel10.add(spinner7, "cell 1 1");

						//---- label14 ----
						label14.setText("nm");
						panel10.add(label14, "cell 2 1");

						//---- label15 ----
						label15.setText("z pixel size [nm]");
						panel10.add(label15, "cell 0 2");

						//---- spinner8 ----
						spinner8.setModel(new SpinnerNumberModel(300, null, null, 1));
						panel10.add(spinner8, "cell 1 2");

						//---- label16 ----
						label16.setText("nm");
						panel10.add(label16, "cell 2 2");

						//---- label17 ----
						label17.setText("target_xypixelsize");
						panel10.add(label17, "cell 0 3");
						panel10.add(spinner9, "cell 1 3");

						//---- label18 ----
						label18.setText("nm");
						panel10.add(label18, "cell 2 3");

						//---- label19 ----
						label19.setText("target_zpixelsize");
						panel10.add(label19, "cell 0 4");
						panel10.add(spinner10, "cell 1 4");

						//---- label20 ----
						label20.setText("nm");
						panel10.add(label20, "cell 2 4");

						//---- button7 ----
						button7.setText("PROCESS");
						panel10.add(button7, "cell 0 5");
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

				//---- label1 ----
				label1.setText("<html>Length of Long-axis Analysis Window [\u00b5m](Typically set to 5-10 \u00b5m)</html>");
				panel5.add(label1, "cell 0 0");
				panel5.add(spinner1, "cell 1 0 2 1");

				//---- label3 ----
				label3.setText("<html>Pre-watershed Smoothing [x-fold] (Keep between 1-2)</html>");
				panel5.add(label3, "cell 0 1");
				panel5.add(spinner2, "cell 1 1 2 1");

				//---- label4 ----
				label4.setText("<html>Pre-watershed Smoothing [x-fold] (1, 2 or more)</html>");
				panel5.add(label4, "cell 0 2");
				panel5.add(spinner3, "cell 1 2 2 1");

				//---- label5 ----
				label5.setText("<html>Inner Vessel Diameter [\u00b5m] (1 to 1.5 works well)</html>");
				panel5.add(label5, "cell 0 3");
				panel5.add(spinner4, "cell 1 3 2 1");

				//---- label6 ----
				label6.setText("<html>Hybrid-weighting of cellulose-to-lignin [%]</html>");
				panel5.add(label6, "cell 0 4");

				//---- label2 ----
				label2.setText("Cellulose");
				panel5.add(label2, "cell 1 4");
				panel5.add(slider1, "cell 2 4 8 1");

				//---- label7 ----
				label7.setText("lignin");
				panel5.add(label7, "cell 10 4");

				//---- label8 ----
				label8.setText("<html>Name of current processing file</html>");
				panel5.add(label8, "cell 0 5");

				//---- button3 ----
				button3.setText("Continue with parameter set");
				panel5.add(button3, "cell 0 6");

				//---- button4 ----
				button4.setText("Tunning");
				panel5.add(button4, "cell 1 6");
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
	private JButton button6;
	private JScrollPane scrollPane1;
	private JTable table1;
	private JPanel panel10;
	private JCheckBox checkBox6;
	private JComboBox<String> comboBox2;
	private JCheckBox checkBox7;
	private JSpinner spinner7;
	private JLabel label14;
	private JLabel label15;
	private JSpinner spinner8;
	private JLabel label16;
	private JLabel label17;
	private JSpinner spinner9;
	private JLabel label18;
	private JLabel label19;
	private JSpinner spinner10;
	private JLabel label20;
	private JButton button7;
	private JPanel panel5;
	private JLabel label1;
	private JSpinner spinner1;
	private JLabel label3;
	private JSpinner spinner2;
	private JLabel label4;
	private JSpinner spinner3;
	private JLabel label5;
	private JSpinner spinner4;
	private JLabel label6;
	private JLabel label2;
	private JSlider slider1;
	private JLabel label7;
	private JLabel label8;
	private JButton button3;
	private JButton button4;
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
