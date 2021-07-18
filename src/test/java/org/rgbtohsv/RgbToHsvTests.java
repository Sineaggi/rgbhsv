package org.rgbtohsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rgbtohsv.RgbToHsv.*;
import static java.lang.Math.*;

public class RgbToHsvTests {
    static void argb_fill(float[] argb, int length) {
        int i = length, offset = 0;

        for (;;)  {
            float a = 1.0f;
            float r;
            float g;
            float b;

            for (r = 0.0f; r <= 1.0f; r += 0.02f) {
                for (g = 0.0f; g <= 1.0f; g += 0.02f) {
                    for (b = 0.0f; b <= 1.0f; b += 0.02f) {
                        argb[offset /* + 0 */] = a;
                        argb[offset + 1] = r;
                        argb[offset + 2] = g;
                        argb[offset + 3] = b;

                        offset += 4;
                        if (--i == 0) return;
                    }
                }
            }
        }
    }

    static void validate(float[] argb, float[] ahsv, int length) {
        int i;

        int offset = 0;
        for (i = 0; i < length; i++) {
            var local_argb = new float[4];
            var local_ahsv = new float[4];

            System.arraycopy(argb, offset, local_argb, 0, 4);
            ahsv_from_argb_sse2(local_ahsv, local_argb, 1, SPECIES);
            argb_from_ahsv_sse2(local_argb, local_ahsv, 1);

            // float should_ahsv[4];
            // ahsv_from_argb_c(should_ahsv, argb, 1);

            if (!(fuzzyEq(ahsv[0], local_ahsv[0]) && fuzzyEq(ahsv[1], local_ahsv[1]) &&
                    fuzzyEq(ahsv[2], local_ahsv[2]) && fuzzyEq(ahsv[3], local_ahsv[3]))) {
                System.out.printf("ERROR: HSV(%+0.4f %+0.4f %+0.4f) ... HSV(%+0.4f %+0.4f %+0.4f)\n",
                        ahsv[1], ahsv[2], ahsv[3],
                        local_ahsv[1], local_ahsv[2], local_ahsv[3]);
            }

            if (!(fuzzyEq(argb[0], local_argb[0]) && fuzzyEq(argb[1], local_argb[1]) &&
                    fuzzyEq(argb[2], local_argb[2]) && fuzzyEq(argb[3], local_argb[3]))) {
                System.out.printf("ERROR: RGB(%+0.4f %+0.4f %+0.4f) ... HSV(%+0.4f %+0.4f %+0.4f)\n" +
                        "       RGB(%+0.4f %+0.4f %+0.4f) ... ...(%+0.4f %+0.4f %+0.4f)\n\n",
                        argb[1], argb[2], argb[3],
                        local_ahsv[1], local_ahsv[2], local_ahsv[3],
                        local_argb[1], local_argb[2], local_argb[3],
                        ahsv[1], ahsv[2], ahsv[3]);
                //fflush(stdout);

                //memcpy(local_argb, argb, sizeof(float)* 4);
                System.arraycopy(argb, offset, local_argb, 0, 4);
                ahsv_from_argb_sse2(ahsv, argb, 1, SPECIES);
                argb_from_ahsv_sse2(argb, ahsv, 1);
            }

            offset += 4;
        }
    }

    static boolean fuzzyEq(float a, float b) {
        return (abs(a - b) < 1e-6f);
    }

    @Test
    @Disabled("not yet implemented")
    public void test() {
        int size = 100000;

        float[] argb = new float[size * 4 * 16];
        float[] ahsv = new float[size * 4 * 16];

        argb_fill(argb, size);
        ahsv_from_argb_c(ahsv, argb, size);
        validate(argb, ahsv, size);
    }

    @Test
    public void test1() {
        var size = 16;
        float[] argb = new float[4 * 16];
        float[] ahsv = new float[4 * 16];

        argb_fill(argb, size);

        var new_rbg = new float[argb.length];
        ahsv_from_argb_c(ahsv, argb, size);
        argb_from_ahsv_c(new_rbg, ahsv, size);
        assertArrayEquals(new_rbg, argb, 0.000001f);
    }

    @Test
    @Disabled("not yet implemented")
    public void test2() {
        var size = 16;
        float[] argb = new float[4 * 16];
        float[] ahsv = new float[4 * 16];

        argb_fill(argb, size);

        var new_rbg = new float[argb.length];
        argb_from_ahsv_sse2(ahsv, argb, size);
        argb_from_ahsv_c(new_rbg, ahsv, size);
        assertArrayEquals(new_rbg, argb, 0.000001f);
    }

    @Test
    @DisplayName("test different pixels")
    public void test3() {
        var size = 4;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        //argb_fill(argb, size);
        argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        argb[1] = 0.1f; argb[5] = 0.3f; argb[9] = 0.5f; argb[13] = .7f;
        argb[2] = 0.2f; argb[6] = 0.4f; argb[10] = 0.6f; argb[14] = .8f;
        argb[3] = 0.3f; argb[7] = .5f; argb[11] = .7f; argb[15] = .9f;

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    @DisplayName("test monochrome")
    public void test33() {
        var size = 4;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        //argb_fill(argb, size);
        argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        argb[1] = 0.1f; argb[5] = 0.3f; argb[9] = 0.5f; argb[13] = 0.7f;
        argb[2] = 0.1f; argb[6] = 0.3f; argb[10] = 0.5f; argb[14] = 0.7f;
        argb[3] = 0.1f; argb[7] = 0.3f; argb[11] = 0.5f; argb[15] = 0.7f;

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    @DisplayName("test white")
    public void test333() {
        var size = 4;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        //argb_fill(argb, size);
        argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        argb[1] = 1f; argb[5] = 1f; argb[9] = 1f; argb[13] = 1f;
        argb[2] = 1f; argb[6] = 1f; argb[10] = 1f; argb[14] = 1f;
        argb[3] = 1f; argb[7] = 1f; argb[11] = 1f; argb[15] = 1f;

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    @DisplayName("test argb fill")
    public void test3334() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);
        //argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        //argb[1] = 1f; argb[5] = 1f; argb[9] = 1f; argb[13] = 1f;
        //argb[2] = 1f; argb[6] = 1f; argb[10] = 1f; argb[14] = 1f;
        //argb[3] = 1f; argb[7] = 1f; argb[11] = 1f; argb[15] = 1f;

        //var size2 = 8;
        float[] argb2 = new float[4 * 4];
        System.arraycopy(argb, 16, argb2, 0, 16);
        float[] argb3 = new float[] {1.0f, 0.0f, 0.0f, 0.08f, 1.0f, 0.0f, 0.0f, 0.099999994f, 1.0f, 0.0f, 0.0f, 0.11999999f, 1.0f, 0.0f, 0.0f, 0.13999999f};

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    @DisplayName("test argb fill but second half")
    public void test33344() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);
        //argb = new float[] {1.0f, 0.0f, 0.0f, 0.08f, 1.0f, 0.0f, 0.0f, 0.099999994f, 1.0f, 0.0f, 0.0f, 0.11999999f, 1.0f, 0.0f, 0.0f, 0.13999999f};
        //argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        //argb[1] = 1f; argb[5] = 1f; argb[9] = 1f; argb[13] = 1f;
        //argb[2] = 1f; argb[6] = 1f; argb[10] = 1f; argb[14] = 1f;
        //argb[3] = 1f; argb[7] = 1f; argb[11] = 1f; argb[15] = 1f;

        //var size2 = 8;
        //float[] argb2 = new float[4 * 4];
        //System.arraycopy(argb, 16, argb2, 0, 16);
        //float[] argb3 = new float[] {1.0f, 0.0f, 0.0f, 0.08f, 1.0f, 0.0f, 0.0f, 0.099999994f, 1.0f, 0.0f, 0.0f, 0.11999999f, 1.0f, 0.0f, 0.0f, 0.13999999f};

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        float f1 = ahsv[20];
        float f2 = ahsv2[20];
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }


    @Test
    @DisplayName("test argb fill but massive")
    public void testmassive() {
        var size = 16384;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);
        //argb = new float[] {1.0f, 0.0f, 0.0f, 0.08f, 1.0f, 0.0f, 0.0f, 0.099999994f, 1.0f, 0.0f, 0.0f, 0.11999999f, 1.0f, 0.0f, 0.0f, 0.13999999f};
        //argb[0] = 1; argb[4] = 1; argb[8] = 1; argb[12] = 1;
        //argb[1] = 1f; argb[5] = 1f; argb[9] = 1f; argb[13] = 1f;
        //argb[2] = 1f; argb[6] = 1f; argb[10] = 1f; argb[14] = 1f;
        //argb[3] = 1f; argb[7] = 1f; argb[11] = 1f; argb[15] = 1f;

        //var size2 = 8;
        //float[] argb2 = new float[4 * 4];
        //System.arraycopy(argb, 16, argb2, 0, 16);
        //float[] argb3 = new float[] {1.0f, 0.0f, 0.0f, 0.08f, 1.0f, 0.0f, 0.0f, 0.099999994f, 1.0f, 0.0f, 0.0f, 0.11999999f, 1.0f, 0.0f, 0.0f, 0.13999999f};

        var species = FloatVector.SPECIES_PREFERRED;
        ahsv_from_argb_sse2(ahsv, argb, size, species);
        ahsv_from_argb_c(ahsv2, argb, size);
        float f1 = ahsv[20];
        float f2 = ahsv2[20];
        assertArrayEquals(ahsv, ahsv2, 1e-6f);
    }

    @Test
    //@Disabled("not yet implemented")
    public void test4() {
        var size = 4;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        var ki = System.nanoTime();
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        System.out.println("time " + (System.nanoTime() - ki));

        ahsv_from_argb_c(ahsv2, argb, size);
        ahsv_from_argb_c(ahsv2, argb, size);
        ahsv_from_argb_c(ahsv2, argb, size);
        ahsv_from_argb_c(ahsv2, argb, size);
        ahsv_from_argb_c(ahsv2, argb, size);
        var ki2 = System.nanoTime();
        ahsv_from_argb_c(ahsv2, argb, size);
        System.out.println("time " + (System.nanoTime() - ki2));

        //ahsv_from_argb_c(ahsv2, ahsv, size);
        assertArrayEquals(ahsv2, argb, 1e-6f);
    }
}
