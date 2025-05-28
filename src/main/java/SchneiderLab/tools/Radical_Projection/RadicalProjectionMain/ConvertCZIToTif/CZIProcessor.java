package SchneiderLab.tools.Radical_Projection.RadicalProjectionMain.ConvertCZIToTif;

import ij.IJ;
import ij.ImagePlus;
import ij.io.Opener;
import ij.measure.Calibration;
import ij.plugin.ChannelSplitter;
import ij.plugin.Duplicator;
import ij.plugin.RGBStackMerge;

import java.io.File;

public class CZIProcessor {

    public static void processCZItoTIFF(String folderPath,
                                        boolean backgroundSubtraction,
                                        int rolling,
                                        int saturated,
                                        boolean isRotate,
                                        String rotateDirection,
                                        boolean fixArtifact) {
        System.out.println("Selected Folder: " + folderPath);
        System.out.println("Background Subtraction: " + backgroundSubtraction);

        if (folderPath == null || folderPath.isEmpty()) {
            System.err.println("Error: Folder path is empty.");
            return;
        }

        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Error: Invalid folder path.");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".czi"));
        if (files == null || files.length == 0) {
            System.err.println("No CZI files found in folder.");
            return;
        }
        IJ.log(String.valueOf(backgroundSubtraction));
        for (File file : files) {
            if (backgroundSubtraction) {
                executeWithBGSub(file,rolling,saturated,isRotate,rotateDirection,fixArtifact);
            } else {
                executeNoBGSub(file,saturated,isRotate,rotateDirection,fixArtifact);
            }
        }
    }

    private static void executeWithBGSub(File file,
                                         int rolling,
                                         int saturated,
                                         boolean isRotate,
                                         String rotateDirection,
                                         boolean fixArtifact) {
        System.out.println("Processing with BGSub: " + file.getName());
        IJ.log("Processing " + file.getName() + " with background subtraction");

        ImagePlus imp = new Opener().openImage(file.getAbsolutePath());
        if (imp == null) return;
        // get info meta data
        Calibration cal = imp.getCalibration();
        double pixelWidth = cal.pixelWidth; // in microns
        double pixelWidthNm = pixelWidth * 1000;
        IJ.log("Pixel size: " + pixelWidthNm + " nm");
        String customMetadata = "Pixel size: " + pixelWidthNm + " nm";
        //this part of the code corrects for horizontal stripe patterns in case the bi-directional settings of the LSM880 are offset
        if(fixArtifact){
            int width = imp.getWidth();
            int height = imp.getHeight();
            int slices = imp.getNSlices(); // total Z-slices

            // Only apply if bi-directional artifacts are confirmed
            if (width > height) {
                // Resize to half height (smooth), then resize back
                IJ.run(imp, "Size...", "width=" + width + " height=" + (int)(height * 0.5) + " depth=" + slices + " average interpolation=Bicubic");
                IJ.run(imp, "Size...", "width=" + width + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
            } else {
                // Resize to half width (smooth), then resize back
                IJ.run(imp, "Size...", "width=" + (int)(width * 0.5) + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
                IJ.run(imp, "Size...", "width=" + width + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
            }
        }

        ImagePlus[] channels = ChannelSplitter.split(imp);
        if (channels.length >= 2) {
            // Duplicate channels
            ImagePlus calco = new Duplicator().run(channels[0]);
            ImagePlus fuchsin = new Duplicator().run(channels[1]);

            // Apply color LUTs
            IJ.run(calco, "Magenta", "");
            IJ.run(fuchsin, "Green", "");

            // Enhance contrast
            String saturated_parameter = "saturated=" + (saturated/100.0);
            IJ.run(calco, "Enhance Contrast", saturated_parameter);
            IJ.run(fuchsin, "Enhance Contrast", saturated_parameter);
            // substrast background
            String rolling_parameter = "rolling="+rolling+" stack";
            IJ.run(calco, "Subtract Background...", rolling_parameter);
            IJ.run(fuchsin, "Subtract Background...", rolling_parameter);
            //rotate the stack
            if(isRotate){
                String rotateCommand = "Rotate " + rotateDirection;
                IJ.run(calco, rotateCommand, "");
                IJ.run(fuchsin, rotateCommand, "");
            }
            // Merge channels (R = calco, G = fuchsin)
            ImagePlus merged = RGBStackMerge.mergeChannels(new ImagePlus[]{calco, fuchsin, null}, false);
            merged.setProperty("Info", customMetadata);
            merged.setCalibration(cal);
            // Save and clean up
            String savePath = file.getParent() + File.separator + stripExtension(file.getName()) + "_proc_withBGsub.tif";
            IJ.saveAsTiff(merged, savePath);
            merged.close();
            calco.close();
            fuchsin.close();
        } else {
            imp.setProperty("Info", customMetadata);
            IJ.saveAsTiff(imp, file.getParent() + File.separator + stripExtension(file.getName()) + "_processed_withBGsub.tif");
        }
        imp.close();
    }

    private static void executeNoBGSub(File file,
                                       int saturated,
                                       boolean isRotate,
                                       String rotateDirection,
                                       boolean fixArtifact) {
        System.out.println("Processing without BGSub: " + file.getName());
        IJ.log("Processing " + file.getName() + " without background subtraction");

        ImagePlus imp = new Opener().openImage(file.getAbsolutePath());
        if (imp == null) return;
        // get info meta data
        Calibration cal = imp.getCalibration();
        double pixelWidth = cal.pixelWidth; // in microns
        double pixelHeight = cal.pixelHeight; // in microns
        double pixelDepth = cal.pixelDepth;
        double pixelWidthNm = pixelWidth * 1000;
        IJ.log("Pixel size: " + pixelWidthNm + " nm");
        String customMetadata = "Pixel size: " + pixelWidthNm + " nm";
        //this part of the code corrects for horizontal stripe patterns in case the bi-directional settings of the LSM880 are offset
        if(fixArtifact){
            int width = imp.getWidth();
            int height = imp.getHeight();
            int slices = imp.getNSlices(); // total Z-slices

            // Only apply if bi-directional artifacts are confirmed
            if (width > height) {
                // Resize to half height (smooth), then resize back
                IJ.run(imp, "Size...", "width=" + width + " height=" + (int)(height * 0.5) + " depth=" + slices + " average interpolation=Bicubic");
                IJ.run(imp, "Size...", "width=" + width + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
            } else {
                // Resize to half width (smooth), then resize back
                IJ.run(imp, "Size...", "width=" + (int)(width * 0.5) + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
                IJ.run(imp, "Size...", "width=" + width + " height=" + height + " depth=" + slices + " average interpolation=Bicubic");
            }
        }
        ImagePlus[] channels = ChannelSplitter.split(imp);
        if (channels.length >= 2) {
            // Duplicate channels
            ImagePlus calco = new Duplicator().run(channels[0]);
            ImagePlus fuchsin = new Duplicator().run(channels[1]);

            // Apply color LUTs
            IJ.run(calco, "Magenta", "");
            IJ.run(fuchsin, "Green", "");

            // Enhance contrast
            String saturated_parameter = "saturated=" + (saturated/100.0);
            IJ.run(calco, "Enhance Contrast", saturated_parameter);
            IJ.run(fuchsin, "Enhance Contrast", saturated_parameter);
            //rotate the stack
            if(isRotate){
                String rotateCommand = "Rotate " + rotateDirection;
                IJ.run(calco, rotateCommand, "");
                IJ.run(fuchsin, rotateCommand, "");
            }
            // Merge channels (R = calco, G = fuchsin)
            ImagePlus merged = RGBStackMerge.mergeChannels(new ImagePlus[]{calco, fuchsin, null}, false);

            // Save and clean up
            String savePath = file.getParent() + File.separator + stripExtension(file.getName()) + "_proc_noBGsub.tif";
            merged.setProperty("Info", customMetadata);
            merged.setCalibration(cal);
            IJ.saveAsTiff(merged, savePath);
            merged.close();
            calco.close();
            fuchsin.close();
        } else {
            imp.setProperty("Info", customMetadata);
            IJ.saveAsTiff(imp, file.getParent() + File.separator + stripExtension(file.getName()) + "_processed_noBGsub.tif");
        }
        imp.close();
    }

    private static String stripExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? filename : filename.substring(0, lastDot);
    }
}
