package schneiderlab.tools.radialprojection.controllers.uiaction;


import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class AddFilePathFromDirToTable implements ActionListener {
    private final JTable table;
    private final Component parent;

    public AddFilePathFromDirToTable(JTable table, Component parent) {
        this.table = table;
        this.parent = parent;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(parent);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File dir = chooser.getSelectedFile();
            File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".tif"));
            if (files != null) {
                DefaultTableModel model = (DefaultTableModel) table.getModel();
                for (File file : files) {
                    model.addRow(new Object[]{file.getAbsolutePath()});
                }
            }
        }
    }
}
