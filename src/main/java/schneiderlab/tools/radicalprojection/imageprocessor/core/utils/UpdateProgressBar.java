package schneiderlab.tools.radicalprojection.imageprocessor.core.utils;

import ij.IJ;

public class UpdateProgressBar {

    public void run(String arg) {
        int totalWork = 1000; // Total number of iterations or work units
        long startTime = System.currentTimeMillis();

        for (int i = 0; i < totalWork; i++) {
            // Do your processing here

            // Update progress and estimate time remaining
            if (i % 10 == 0) { // Update every 10 iterations to reduce overhead
                long currentTime = System.currentTimeMillis();
                double elapsedSeconds = (currentTime - startTime) / 1000.0;
                double progress = (i + 1) / (double) totalWork;
                double estimatedTotalSeconds = elapsedSeconds / progress;
                double remainingSeconds = estimatedTotalSeconds - elapsedSeconds;

                IJ.showProgress(i, totalWork);
                IJ.showStatus(String.format(
                        "Progress: %.1f%%, Remaining: %.1f seconds",
                        progress * 100, remainingSeconds));
            }
        }

        IJ.showProgress(1.0);
        IJ.showStatus("Processing complete!");
    }
}
