package schneiderlab.tools.radialprojection;

import org.scijava.Context;
import org.scijava.command.Command;
import org.scijava.plugin.Menu;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import javax.swing.*;

import schneiderlab.tools.radialprojection.controllers.controllers.MainController;
import schneiderlab.tools.radialprojection.models.radialprojection.RadialProjectionModel;
import schneiderlab.tools.radialprojection.views.userinterfacecomponents.Radical_Projection_Tool;

@Plugin(type = Command.class, menuPath = "Plugins > Radial Projection")
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
            RadialProjectionModel radialProjectionModel = new RadialProjectionModel();
            Radical_Projection_Tool form = new Radical_Projection_Tool(context, frame);
            MainController mainController = new MainController(form, radialProjectionModel, context);
            frame.setContentPane(form.getContentPane());
            frame.pack();
            frame.setVisible(true);
    }
}
