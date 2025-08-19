package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

public class Float64Transpose implements FloatTranspose {
    private static final VectorSpecies<Float> SPECIES_64 = FloatVector.SPECIES_64; // A species for 4 floats

    public static final VectorShuffle<Float> t1 = VectorShuffle.fromValues(SPECIES_64, 0, 4);
    public static final VectorShuffle<Float> t2 = VectorShuffle.fromValues(SPECIES_64, 1, 5);

    public static final VectorShuffle<Float> it1 = VectorShuffle.fromValues(SPECIES_64, 0, 2);
    public static final VectorShuffle<Float> it2 = VectorShuffle.fromValues(SPECIES_64, 1, 3);

    @Override
    public Result transpose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        var col1 = row1.rearrange(Float64Transpose.t1, row3);
        var col2 = row1.rearrange(Float64Transpose.t2, row3);
        var col3 = row2.rearrange(Float64Transpose.t1, row4);
        var col4 = row2.rearrange(Float64Transpose.t2, row4);

        return new Result(col1, col2, col3, col4);
    }

    @Override
    public Result itranspose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        var col1 = row1.rearrange(Float64Transpose.it1, row2);
        var col2 = row3.rearrange(Float64Transpose.it1, row4);
        var col3 = row1.rearrange(Float64Transpose.it2, row2);
        var col4 = row3.rearrange(Float64Transpose.it2, row4);

        return new Result(col1, col2, col3, col4);
    }
}
