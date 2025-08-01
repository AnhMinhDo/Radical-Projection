package schneiderlab.tools.radialprojection.models.czitotifmodel;

import ij.IJ;
import ij.Prefs;
import schneiderlab.tools.radialprojection.imageprocessor.core.convertczitotif.RotateDirection;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class CziToTifModel {
    private String dirPath;
    private boolean isBgSub;
    private int rollingValue;
    private int saturationValue;
    private boolean isRotate;
    private RotateDirection rotateDirection;
    private String statusString;

    public CziToTifModel() {
    }

    public void initValues(String propertiesFile){
        // load initial values for cziToTifModel from properties file
        Properties props = new Properties();
        try (InputStream input = getClass().getResourceAsStream("/properties_files/initValues.properties")){
            props.load(input);
            String dirpath= Prefs.get("RadialProjection.CziToTifModel.dirPath",
                    props.getProperty("CziToTifModel.dirPath"));
            this.setDirPath(dirpath);
            boolean isBgSub= Prefs.get("RadialProjection.CziToTifModel.isBgSub",
                    Boolean.parseBoolean(props.getProperty("CziToTifModel.isBgSub")));
            this.setIsbgSub(isBgSub);
            int rollingValue= (int)Prefs.get("RadialProjection.CziToTifModel.rollingValue",
                    Integer.parseInt(props.getProperty("CziToTifModel.rollingValue")));
            this.setRollingValue(rollingValue);
            int saturationValue= (int)Prefs.get("RadialProjection.CziToTifModel.saturationValue",
                    Integer.parseInt(props.getProperty("CziToTifModel.saturationValue")));
            this.setSaturationValue(saturationValue);
            boolean isRotate= Prefs.get("RadialProjection.CziToTifModel.isRotate",
                    Boolean.parseBoolean(props.getProperty("CziToTifModel.isRotate")));
            this.setRotate(isRotate);
            String rotateDirection = Prefs.get("RadialProjection.CziToTifModel.rotateDirection",
                    props.getProperty("CziToTifModel.rotateDirection"));
            this.setRotateDirection(RotateDirection.fromLabel(rotateDirection));
        } catch (IOException e) {
            System.err.println("Fail to load .properties file");
        }
    }

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }

    public boolean isBgSub() {
        return isBgSub;
    }

    public void setIsbgSub(boolean isbgSub) {
        this.isBgSub = isbgSub;
    }

    public int getRollingValue() {
        return rollingValue;
    }

    public void setRollingValue(int rollingValue) {
        this.rollingValue = rollingValue;
    }

    public int getSaturationValue() {
        return saturationValue;
    }

    public void setSaturationValue(int saturationValue) {
        this.saturationValue = saturationValue;
    }

    public boolean isRotate() {
        return isRotate;
    }

    public void setRotate(boolean rotate) {
        isRotate = rotate;
    }

    public RotateDirection getRotateDirection() {
        return rotateDirection;
    }

    public void setRotateDirection(RotateDirection rotateDirection) {
        this.rotateDirection = rotateDirection;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }
}
