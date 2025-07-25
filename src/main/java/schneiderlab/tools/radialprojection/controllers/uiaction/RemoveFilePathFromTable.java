package schneiderlab.tools.radialprojection.controllers.uiaction;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RemoveFilePathFromTable implements ActionListener {
    private final JTable table;

    public RemoveFilePathFromTable(JTable table) {
        this.table = table;
    }

    @Override
    public void actionPerformed(ActionEvent e){
        int selectedRow = table.getSelectedRow();
        if (selectedRow != -1) {
            DefaultTableModel model = (DefaultTableModel) table.getModel();
            model.removeRow(selectedRow);
        }
    }
}
