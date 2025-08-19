package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Float256TransposeTests {
    @Test
    public void test() {
        var p0 = FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{0, 1, 2, 3, 4, 5, 6, 7}, 0);
        var p1 = FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{8, 9, 10, 11, 12, 13, 14, 15}, 0);
        var p2 = FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{16, 17, 18, 19, 20, 21, 22, 23}, 0);
        var p3 = FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{24, 25, 26, 27, 28, 29, 30, 31}, 0);

        var out = new Float256Transpose().transpose(p0, p1, p2, p3);

        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{0, 4, 8, 12, 16, 20, 24, 28}, 0), out.col1());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{1, 5, 9, 13, 17, 21, 25, 29}, 0), out.col2());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{2, 6, 10, 14, 18, 22, 26, 30}, 0), out.col3());
        assertEquals(FloatVector.fromArray(FloatVector.SPECIES_256, new float[]{3, 7, 11, 15, 19, 23, 27, 31}, 0), out.col4());
    }
}
