package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Float64TransposeTests {
    @Test
    public void test() {
        var p0 = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{0, 1}, 0);
        var p1 = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{2, 3}, 0);
        var p2 = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{4, 5}, 0);
        var p3 = FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{6, 7}, 0);

        var out = new Float64Transpose().transpose(p0, p1, p2, p3);

        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{0, 4}, 0), out.col1());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{1, 5}, 0), out.col2());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{2, 6}, 0), out.col3());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_64, new float[]{3, 7}, 0), out.col4());
    }
}
