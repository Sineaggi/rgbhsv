package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

/**
 * Modified version of <a href="https://stackoverflow.com/questions/34122605/how-to-optimize-simd-transpose-function-8x4-4x8">...</a>
 */
public class Float256Transpose implements FloatTranspose {
    private static final VectorSpecies<Float> SPECIES_256 = FloatVector.SPECIES_256; // A species for 8 floats

    public static final VectorShuffle<Float> shuffle_256_1 = VectorShuffle.fromValues(SPECIES_256, 0, 4, 8 , 12, 1, 5, 9 , 13);
    public static final VectorShuffle<Float> shuffle_256_2 = VectorShuffle.fromValues(SPECIES_256, 2, 6, 10, 14, 3, 7, 11, 15);

    public static final VectorShuffle<Float> shuffle_256_3 = VectorShuffle.fromValues(SPECIES_256, 0, 1, 2, 3, 8, 9, 10, 11);
    public static final VectorShuffle<Float> shuffle_256_4 = VectorShuffle.fromValues(SPECIES_256, 4, 5, 6, 7, 12, 13, 14, 15);

    @Override
    public FloatTranspose.Result transpose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        var a00 = row1.rearrange(shuffle_256_1, row2);
        var a10 = row3.rearrange(shuffle_256_1, row4);
        var a01 = row1.rearrange(shuffle_256_2, row2);
        var a11 = row3.rearrange(shuffle_256_2, row4);

        var X = a00.rearrange(shuffle_256_3, a10);
        var Y = a00.rearrange(shuffle_256_4, a10);
        var Z = a01.rearrange(shuffle_256_3, a11);
        var W = a01.rearrange(shuffle_256_4, a11);

        return new FloatTranspose.Result(X, Y, Z, W);
    }
}
