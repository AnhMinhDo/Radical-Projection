package schneiderlab.tools.radialprojection.controllers.uiaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MoveCurrentFileToRadialProjectionStep implements ActionListener {
    private final JTable table;
    private final JTextField textField;
    private final JTabbedPane tabbedMainPane;
    private final JPanel radialProjectionPanel;

    public MoveCurrentFileToRadialProjectionStep(JTable table,
                                                 JTextField textField,
                                                 JTabbedPane tabbedMainPane,
                                                 JPanel radialProjectionPanel) {
        this.table = table;
        this.textField = textField;
        this.tabbedMainPane = tabbedMainPane;
        this.radialProjectionPanel = radialProjectionPanel;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        if (model.getRowCount() > 0) {
            String currentFilePathString = (String) model.getValueAt(0,0);
            textField.setText(currentFilePathString);
            model.removeRow(0);
        }
        tabbedMainPane.setSelectedComponent(radialProjectionPanel);
    }
}
