package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import org.junit.jupiter.api.Test;

public class TransposeTests {
    @Test
    public void test() {
        IO.println(FloatVector.SPECIES_128.length());
        IO.println(FloatVector.SPECIES_PREFERRED.length());
        IO.println(FloatVector.SPECIES_MAX.length());
    }
}
