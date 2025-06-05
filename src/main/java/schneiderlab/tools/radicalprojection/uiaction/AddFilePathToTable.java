package schneiderlab.tools.radicalprojection.uiaction;

import schneiderlab.tools.radicalprojection.userinterfacecomponents.Radical_Projection_Tool;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AddFilePathToTable implements ActionListener {
    private final JTable table;
    private final Component parent;

    public AddFilePathToTable (JTable table, Component parent) {
        this.table = table;
        this.parent = parent;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            String path = chooser.getSelectedFile().getAbsolutePath();
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.addRow(new Object[]{path});
        }

    }


}
