package RadicalProjectionMain;

import ij.IJ;
import org.scijava.command.Command;
import org.scijava.plugin.Plugin;
import javax.swing.*;
import java.awt.*;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import UIDesign.Radical_Projection_Tool;

@Plugin(type = Command.class, menuPath = "Plugins > Radical Projection")
public class Main implements Command {
    @Override
    public void run() {
        launchUI();
    }

    public static void launchUI(){
        SwingUtilities.invokeLater(() -> {
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.contains("mac")) {
                    com.formdev.flatlaf.themes.FlatMacLightLaf.setup();
                } else {
                    com.formdev.flatlaf.FlatLightLaf.setup();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            JFrame frame = new JFrame("Radical_Projection_UI");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            Radical_Projection_Tool form = new Radical_Projection_Tool();
            frame.setContentPane(form.getContentPane());
            frame.pack();
            frame.setVisible(true);
        });
    }
}
