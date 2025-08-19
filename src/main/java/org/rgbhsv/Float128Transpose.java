package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

public class Float128Transpose {
    private static final VectorSpecies<Float> SPECIES_128 = FloatVector.SPECIES_128; // A species for 4 floats

    public static final VectorShuffle<Float> interleave32_lft = VectorShuffle.fromValues(SPECIES_128, 0, 4, 1, 5);
    public static final VectorShuffle<Float> interleave32_rgt = VectorShuffle.fromValues(SPECIES_128, 2, 6, 3, 7);

    public static final VectorShuffle<Float> interleave64_lft = VectorShuffle.fromValues(SPECIES_128, 0, 1, 8, 9);
    public static final VectorShuffle<Float> interleave64_rgt = VectorShuffle.fromValues(SPECIES_128, 2, 3, 10, 11);

    record Result(FloatVector col1, FloatVector col2, FloatVector col3, FloatVector col4) {
    }

    // todo: write transpose function
    // on arm, we use 128 bit wide float vectors
    // that's 4 floats. so 4x4 matrix
    public Result transpose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        // Step 1: Interleave elements of row1 and row2
        ////FloatVector col4 = FloatVector.recombine(SPECIES_128, temp2, temp4, 1, 1);
        var a0 = row1.rearrange(interleave32_lft, row2);
        var a1 = row1.rearrange(interleave32_rgt, row2);
        var a2 = row3.rearrange(interleave32_lft, row4);
        var a3 = row3.rearrange(interleave32_rgt, row4);

        var X = a0.rearrange(interleave64_lft, a2);
        var Y = a0.rearrange(interleave64_rgt, a2);
        var Z = a1.rearrange(interleave64_lft, a3);
        var W = a1.rearrange(interleave64_rgt, a3);

        // Now col1, col2, col
        return new Result(
                X, Y, Z, W
        );
    }
}
