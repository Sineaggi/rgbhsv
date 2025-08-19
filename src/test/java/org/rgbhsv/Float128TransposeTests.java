package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Float128TransposeTests {
    @Test
    public void test() {
        var p0 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{0, 1, 2, 3}, 0);
        var p1 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{4, 5, 6, 7}, 0);
        var p2 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{8, 9, 10, 11}, 0);
        var p3 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{12, 13, 14, 15}, 0);

        var out = Float128Transpose.create().transpose(p0, p1, p2, p3);

        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{0, 4, 8, 12}, 0), out.col1());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{1, 5, 9, 13}, 0), out.col2());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{2, 6, 10, 14}, 0), out.col3());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{3, 7, 11, 15}, 0), out.col4());
    }

    @Test
    public void test2() {
        var p0 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{0, 1, 2, 3}, 0);
        var p1 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{4, 5, 6, 7}, 0);
        var p2 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{8, 9, 10, 11}, 0);
        var p3 = FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{12, 13, 14, 15}, 0);

        var out = Float128Transpose.create(-1).transpose(p0, p1, p2, p3);

        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{0, 4, 8, 12}, 0), out.col1());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{1, 5, 9, 13}, 0), out.col2());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{2, 6, 10, 14}, 0), out.col3());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_128, new float[]{3, 7, 11, 15}, 0), out.col4());
    }
}
