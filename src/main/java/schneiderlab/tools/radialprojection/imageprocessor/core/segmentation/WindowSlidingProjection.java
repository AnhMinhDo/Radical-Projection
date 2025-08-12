package schneiderlab.tools.radialprojection.imageprocessor.core.segmentation;

import net.imglib2.Cursor;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.Views;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class WindowSlidingProjection {

    private int currentSliceProcess;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.addPropertyChangeListener(listener);
    }
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.pcs.removePropertyChangeListener(listener);
    }
    public int getCurrentSlice() {
        return this.currentSliceProcess;
    }

    public void setNewCurrentSlice(int newCurrentSlice) {
        int previousSlice = this.currentSliceProcess;
        this.currentSliceProcess = newCurrentSlice;
        this.pcs.firePropertyChange("currentSlice", previousSlice, currentSliceProcess);
    }


    public void averageProjection(
            RandomAccessibleInterval<FloatType> input,
            RandomAccessibleInterval<FloatType> output,
            int windowSize,
            int depth,
            int width,
            int height){
        // perform window sliding Projection, each new slide is the average projection of all the slide in the window
        for (long z = 0; z < depth; z++) {
            setNewCurrentSlice((int)z); // this is for updating the ProgressBar
            // Determine the slice window (handle boundaries)
            long startSlice = Math.max(0, z - windowSize / 2);
            long endSlice = Math.min(depth - 1, z + windowSize / 2);
            int numSlicesInWindow = (int) (endSlice - startSlice + 1);

            // Get the output slice (2D)
            RandomAccessibleInterval<FloatType> outputSlice = Views.hyperSlice(output, 2, z); // d=2 stands for the z dimension(slice)
            // Initialize a sum buffer for the output slice
            float[] sum = new float[(int) (width * height)];
            // Accumulate values from neighboring slices
            for (long zz = startSlice; zz <= endSlice; zz++) {
                RandomAccessibleInterval<FloatType> inputSlice = Views.hyperSlice(input, 2, zz); // d=2 stands for the z dimension(slice)
                Cursor<FloatType> cursor = Views.flatIterable(inputSlice).cursor();

                int pixelIndex = 0;
                while (cursor.hasNext()) {
                    sum[pixelIndex] += cursor.next().get();
                    pixelIndex++;
                }
            }
            // Compute average and write to output
            Cursor<FloatType> outputCursor = Views.flatIterable(outputSlice).cursor();
            for (float s : sum) {
                outputCursor.next().set( s / numSlicesInWindow);
            }
        }
    }
}
