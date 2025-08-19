package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

public class Float64Transpose {
    private static final VectorSpecies<Float> SPECIES_64 = FloatVector.SPECIES_64; // A species for 4 floats

    public static final VectorShuffle<Float> t1 = VectorShuffle.fromValues(SPECIES_64, 0, 4);
    public static final VectorShuffle<Float> t2 = VectorShuffle.fromValues(SPECIES_64, 1, 5);

    public static final VectorShuffle<Float> t3 = VectorShuffle.fromValues(SPECIES_64, 2, 6);
    public static final VectorShuffle<Float> t4 = VectorShuffle.fromValues(SPECIES_64, 3, 7);
}
