package schneiderlab.tools.radialprojection.controllers.uiaction.czitotif;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class BrowseButtonCZIToTif implements ActionListener {
    private JTextField textFieldFolderPath;
    private JFrame parentFrame;

    public BrowseButtonCZIToTif(JTextField textFieldFolderPath, JFrame parentFrame) {
        this.textFieldFolderPath = textFieldFolderPath;
        this.parentFrame = parentFrame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnVal = chooser.showOpenDialog(parentFrame);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            textFieldFolderPath.setText(chooser.getSelectedFile().getAbsolutePath());
    }
}

}
