package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorShuffle;
import jdk.incubator.vector.VectorSpecies;

/**
 * Implementation based on <a href="https://fgiesen.wordpress.com/2013/07/09/simd-transposes-1/">SIMD transposes 1</a>
 */
public class Float128Transpose implements FloatTranspose {
    private static final VectorSpecies<Float> SPECIES_128 = FloatVector.SPECIES_128; // A species for 4 floats

    public static final VectorShuffle<Float> interleave32_lft = VectorShuffle.fromValues(SPECIES_128, 0, 4, 1, 5);
    public static final VectorShuffle<Float> interleave32_rgt = VectorShuffle.fromValues(SPECIES_128, 2, 6, 3, 7);

    public static final VectorShuffle<Float> interleave64_lft = VectorShuffle.fromValues(SPECIES_128, 0, 1, 8, 9);
    public static final VectorShuffle<Float> interleave64_rgt = VectorShuffle.fromValues(SPECIES_128, 2, 3, 10, 11);

    private Float128Transpose(int impl) {
        this.impl = impl;
    }

    public static Float128Transpose create() {
        return new Float128Transpose(0);
    }

    public static Float128Transpose create(int impl) {
        return new Float128Transpose(impl);
    }

    private final int impl;

    @Override
    public FloatTranspose.Result transpose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        if (impl == 0) {
            return interleave1(row1, row2, row3, row4);
        } else {
            return interleave2(row1, row2, row3, row4);
        }
    }

    private FloatTranspose.Result interleave1(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4) {
        var a0 = row1.rearrange(interleave32_lft, row2);
        var a1 = row1.rearrange(interleave32_rgt, row2);
        var a2 = row3.rearrange(interleave32_lft, row4);
        var a3 = row3.rearrange(interleave32_rgt, row4);

        var X = a0.rearrange(interleave64_lft, a2);
        var Y = a0.rearrange(interleave64_rgt, a2);
        var Z = a1.rearrange(interleave64_lft, a3);
        var W = a1.rearrange(interleave64_rgt, a3);

        return new FloatTranspose.Result(X, Y, Z, W);
    }

    private FloatTranspose.Result interleave2(FloatVector p0, FloatVector p1, FloatVector p2, FloatVector p3) {
        var b0 = p0.rearrange(interleave32_lft, p2);
        var b1 = p1.rearrange(interleave32_lft, p3);
        var b2 = p0.rearrange(interleave32_rgt, p2);
        var b3 = p1.rearrange(interleave32_rgt, p3);

        var X = b0.rearrange(interleave32_lft, b1);
        var Y = b0.rearrange(interleave32_rgt, b1);
        var Z = b2.rearrange(interleave32_lft, b3);
        var W = b2.rearrange(interleave32_rgt, b3);

        return new FloatTranspose.Result(X, Y, Z, W);
    }
}
