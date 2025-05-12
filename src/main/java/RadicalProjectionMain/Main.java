package RadicalProjectionMain;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import javax.swing.*;

import UIComponents.Radical_Projection_Tool;

@Plugin(type = Command.class, menuPath = "Plugins > Radical Projection")
public class Main implements Command {

    @Parameter
    private Context context; // get context from current Fiji session

    @Override
    public void run() {
        SwingUtilities.invokeLater(()-> launchUI());
    }

    public void launchUI(){
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

            Radical_Projection_Tool form = new Radical_Projection_Tool(context);
            frame.setContentPane(form.getContentPane());
            frame.pack();
            frame.setVisible(true);
    }
}
