package schneiderlab.tools.radialprojection.imageprocessor.core.bandgapmeasurement;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Test
    void testSinglePeak() {
        short[] input = {1, 3, 1};
        byte[] expected = {0, (byte)255, 0};
        assertArrayEquals(expected, Utils.findLocalMaxima(input));
    }

    @Test
    void testFlatPeak() {
        short[] input = {1, 3, 3, 1};
        byte[] expected = {0, (byte)255, 0, 0};
        assertArrayEquals(expected, Utils.findLocalMaxima(input));
    }

    @Test
    void testMultiplePeaks() {
        short[] input = {1, 4, 1, 2, 5, 5, 2};
        byte[] expected = {0, (byte)255, 0, 0, (byte)255, 0, 0};
        assertArrayEquals(expected, Utils.findLocalMaxima(input));
    }

    @Test
    void testNoPeaks() {
        short[] input = {1, 2, 3, 4, 5};
        byte[] expected = {0, 0, 0, 0, 0};
        assertArrayEquals(expected, Utils.findLocalMaxima(input));
    }

    @Test
    void testEdgeCases() {
        short[] input = {5};
        byte[] expected = {0};
        assertArrayEquals(expected, Utils.findLocalMaxima(input));

        short[] input2 = {5, 5, 5};
        byte[] expected2 = {0, 0, 0};
        assertArrayEquals(expected2, Utils.findLocalMaxima(input2));
    }
}