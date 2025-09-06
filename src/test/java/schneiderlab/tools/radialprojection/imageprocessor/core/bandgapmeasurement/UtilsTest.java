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

    @Test
    void testEmptyArray() {
        assertEquals(0.0,
                Utils.calculateAverageBandWidthInNm(new byte[]{}, 5));
    }

    @Test
    void testNoBands() {
        assertEquals(0.0,
                Utils.calculateAverageBandWidthInNm(new byte[]{0, 1, 2}, 10));
    }

    @Test
    void testSingleBand() {
        // 3 pixels wide, scale 10 → 30
        assertEquals(30.0,
                Utils.calculateAverageBandWidthInNm(
                        new byte[]{(byte)255, (byte)255, (byte)255}, 10));
    }

    @Test
    void testTwoEqualBands() {
        // Two bands, each 2 pixels wide, sum=4, avg=2 * scale 5 = 10
        assertEquals(10.0,
                Utils.calculateAverageBandWidthInNm(
                        new byte[]{(byte)255, (byte)255, 0, (byte)255, (byte)255}, 5));
    }

    @Test
    void testTwoUnequalBands() {
        // Bands: 2 pixels and 3 pixels → sum=5, avg=2.5 * scale 4 = 10
        assertEquals(10.0,
                Utils.calculateAverageBandWidthInNm(
                        new byte[]{(byte)255, (byte)255, 0, (byte)255, (byte)255, (byte)255}, 4));
    }

    @Test
    void testBandAtEnd() {
        // One band of width 2 at end → avg=2 * 7 = 14
        assertEquals(14.0,
                Utils.calculateAverageBandWidthInNm(
                        new byte[]{0, 0, (byte)255, (byte)255}, 7));
    }

    @Test
    void testTypicalBands() {
        // Bands: 2 pixels and 3 pixels → sum=5, avg=2.5 * scale 4 = 10
        assertEquals(10.0,
                Utils.calculateAverageBandWidthInNm(
                        new byte[]{0, (byte)255, (byte)255, 0, (byte)255, (byte)255, (byte)255,0}, 4));
    }

}