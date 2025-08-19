package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.rgbhsv.RgbHsv.*;
import static java.lang.Math.*;

public class RgbHsvTests {
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
            argb_from_ahsv_sse2(local_argb, local_ahsv, 1, SPECIES);

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
                argb_from_ahsv_sse2(argb, ahsv, 1, SPECIES);
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
    @Disabled("oom")
    public void test2() {
        var size = 16 * 32768 * 16;
        float[] argb_orig = new float[4 * size];
        float[] ahsv = new float[4 * size];

        argb_fill(argb_orig, size);

        ahsv_from_argb_c(ahsv, argb_orig, size);
        float[] argb = new float[argb_orig.length];

        var new_rbg = new float[argb.length];
        argb_from_ahsv_sse2(argb, ahsv, size, SPECIES);
        argb_from_ahsv_c(new_rbg, ahsv, size);
        assertArrayEquals(new_rbg, argb, 0.000001f);
    }

    @Test
    public void testDifferentPixels() {
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
    public void testMonochrome() {
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

    //@Test
    //@DisplayName("test 128")
    //public void test33355() {
    //    var size = 8;
    //    float[] argb = new float[4 * size];
    //    float[] ahsv = new float[4 * size];
    //    float[] ahsv2 = new float[4 * size];
//
    //    argb_fill(argb, size);
//
    //    ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
    //    ahsv_from_argb_c(ahsv2, argb, size);
    //    assertArrayEquals(ahsv, ahsv2, 1e-7f);
    //}

    @Test
    @Disabled("not finished")
    public void test_128_shuffle() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);

        //ahsv_from_argb_sse2(ahsv, argb, size);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    public void testWhite() {
        var size = 16;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        Arrays.fill(argb, 1.f);

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        ahsv_from_argb_c(ahsv2, argb, size);
        assertArrayEquals(ahsv, ahsv2, 1e-7f);
    }

    @Test
    public void testRoundTrip() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] argb2 = new float[4 * size];

        argb_fill(argb, size);

        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        argb_from_ahsv_sse2(argb2, ahsv, size, SPECIES);

        assertArrayEquals(argb, argb2);
    }

    @Test
    public void testRoundTripScalar() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] argb2 = new float[4 * size];

        argb_fill(argb, size);

        ahsv_from_argb_c(ahsv, argb, size);
        argb_from_ahsv_c(argb2, ahsv, size);

        assertArrayEquals(argb, argb2);
    }

    @Test
    @DisplayName("test round trip wide")
    public void testRoundTripWide() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] argb2 = new float[4 * size];

        argb_fill(argb, size);

        float[] src_a = new float[size], src_r = new float[size], src_g = new float[size], src_b = new float[size];
        float[] dst_a = new float[size], dst_h = new float[size], dst_s = new float[size], dst_v = new float[size];
        float[] dst2_a = new float[size], dst2_r = new float[size], dst2_g = new float[size], dst2_b = new float[size];

        split(src_a, src_r, src_g, src_b, argb, size);

        ahsv_from_argb_sse2(dst_a, dst_h, dst_s, dst_v, src_a, src_r, src_g, src_b, size, SPECIES);
        argb_from_ahsv_sse2(dst2_a, dst2_r, dst2_g, dst2_b, dst_a, dst_h, dst_s, dst_v, size, SPECIES);

        recombine(argb2, dst2_a, dst2_r, dst2_g, dst2_b, size);

        assertArrayEquals(argb, argb2);
    }

    @Test
    public void testRoundTripWideScalar() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] argb2 = new float[4 * size];

        argb_fill(argb, size);

        float[] src_a = new float[size], src_r = new float[size], src_g = new float[size], src_b = new float[size];
        float[] dst_a = new float[size], dst_h = new float[size], dst_s = new float[size], dst_v = new float[size];
        float[] dst2_a = new float[size], dst2_r = new float[size], dst2_g = new float[size], dst2_b = new float[size];

        split(src_a, src_r, src_g, src_b, argb, size);

        ahsv_from_argb_c(dst_a, dst_h, dst_s, dst_v, src_a, src_r, src_g, src_b, size);
        argb_from_ahsv_c(dst2_a, dst2_r, dst2_g, dst2_b, dst_a, dst_h, dst_s, dst_v, size);

        recombine(argb2, dst2_a, dst2_r, dst2_g, dst2_b, size);

        assertArrayEquals(argb, argb2);
    }

    @Test
    public void testRoundTripWideScalarArgb() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] argb2 = new float[4 * size];

        argb_fill(argb, size);
        ahsv_from_argb_c(ahsv, argb, size);

        float[] src_a = new float[size], src_h = new float[size], src_s = new float[size], src_v = new float[size];
        float[] dst_a = new float[size], dst_r = new float[size], dst_g = new float[size], dst_b = new float[size];
        //float[] dst2_a = new float[size], dst2_r = new float[size], dst2_g = new float[size], dst2_b = new float[size];

        split(src_a, src_h, src_s, src_v, ahsv, size);

        argb_from_ahsv_c(dst_a, dst_r, dst_g, dst_b, src_a, src_h, src_s, src_v, size);
        argb_from_ahsv_c(argb, ahsv, size);

        recombine(argb2, dst_a, dst_r, dst_g, dst_b, size);

        assertArrayEquals(argb, argb2);
    }

    @Test
    public void testWideVsNormalAhsv() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);

        float[] src_a = new float[size], src_r = new float[size], src_g = new float[size], src_b = new float[size];
        float[] dst_a = new float[size], dst_h = new float[size], dst_s = new float[size], dst_v = new float[size];
        float[] dst2_a = new float[size], dst2_r = new float[size], dst2_g = new float[size], dst2_b = new float[size];

        split(src_a, src_r, src_g, src_b, argb, size);

        ahsv_from_argb_c(dst_a, dst_h, dst_s, dst_v, src_a, src_r, src_g, src_b, size);
        ahsv_from_argb_c(ahsv, argb, size);

        recombine(ahsv2, dst_a, dst_h, dst_s, dst_v, size);

        assertArrayEquals(ahsv, ahsv2);
    }

    @Test
    public void testArgbFill() {
        var size = 8;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];

        argb_fill(argb, size);

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
    @DisplayName("test argb fill but massive")
    public void testmassive2() {
        var size = 16384;
        float[] argb = new float[4 * size];
        float[] ahsv = new float[4 * size];
        float[] ahsv2 = new float[4 * size];
        float[] ahsv3 = new float[4 * size];

        argb_fill(argb, size);

        float[] src_a = new float[size], src_r = new float[size], src_g = new float[size], src_b = new float[size];
        float[] dst_a = new float[size], dst_h = new float[size], dst_s = new float[size], dst_v = new float[size];

        split(src_a, src_r, src_g, src_b, argb, size);

        ahsv_from_argb_c(ahsv2, argb, size);

        var species = FloatVector.SPECIES_PREFERRED;
        ahsv_from_argb_sse2(ahsv, argb, size, species);
        assertArrayEquals(ahsv, ahsv2, 1e-6f);

        ahsv_from_argb_sse2(dst_a, dst_h, dst_s, dst_v,
                src_a, src_r, src_g, src_b, size, species);
        recombine(ahsv3, dst_a, dst_h, dst_s, dst_v, size);
        assertArrayEquals(ahsv2, ahsv3, 1e-6f);

        assertArrayEquals(ahsv, ahsv3, 1e-6f);
    }

    @Test
    @DisplayName("test ahsv fill but massive")
    public void testmassive3() {
        var size = 16384;
        float[] ahsv = new float[4 * size];
        float[] argb = new float[4 * size];
        float[] argb2 = new float[4 * size];
        float[] argb3 = new float[4 * size];

        argb_fill(argb, size);
        ahsv_from_argb_c(ahsv, argb, size);

        float[] src_a = new float[size], src_h = new float[size], src_s = new float[size], src_v = new float[size];
        float[] dst_a = new float[size], dst_r = new float[size], dst_g = new float[size], dst_b = new float[size];

        split(src_a, src_h, src_s, src_v, ahsv, size);

        argb_from_ahsv_c(argb2, ahsv, size);

        var species = FloatVector.SPECIES_PREFERRED;
        argb_from_ahsv_sse2(argb, ahsv, size, species);
        assertArrayEquals(argb, argb2, 1e-6f);

        argb_from_ahsv_sse2(dst_a, dst_r, dst_g, dst_b,
                src_a, src_h, src_s, src_v, size, species);
        recombine(argb3, dst_a, dst_r, dst_g, dst_b, size);
        assertArrayEquals(argb2, argb3, 1e-6f);

        assertArrayEquals(argb, argb3, 1e-6f);
    }

    void split(float[] dst1, float[] dst2, float[] dst3, float[] dst4, float[] src, int size) {
        for (int i = 0; i < size; i++) {
            dst1[i] = src[i * 4 + 0];
            dst2[i] = src[i * 4 + 1];
            dst3[i] = src[i * 4 + 2];
            dst4[i] = src[i * 4 + 3];
        }
    }

    void recombine(float[] dst, float[] src1, float[] src2, float[] src3, float[] src4, int size) {
        for (int i = 0; i < size; i++) {
            dst[i * 4 + 0] = src1[i];
            dst[i * 4 + 1] = src2[i];
            dst[i * 4 + 2] = src3[i];
            dst[i * 4 + 3] = src4[i];
        }
    }

    @Test
    @Disabled("unsure")
    public void test4() {
        var size = 1048576;
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
