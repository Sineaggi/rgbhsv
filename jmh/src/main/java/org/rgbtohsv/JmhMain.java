package org.rgbtohsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.IOException;
import java.util.Random;

import static org.rgbtohsv.RgbToHsv.ahsv_from_argb_c;
import static org.rgbtohsv.RgbToHsv.ahsv_from_argb_sse2;

public class JmhMain {
    public static void main(String[] args) throws IOException {
        org.openjdk.jmh.Main.main(args);
    }

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

    static int size = 16384;
    static float[] argb = new float[4 * size];
    static float[] ahsv = new float[4 * size];
    //static float[] ahsv2 = new float[4 * size];

    static {
        argb_fill(argb, size);
    }

    @Benchmark
    public float[] vector() {
        //var size = 16384;
        //float[] argb = new float[4 * size];
        //float[] ahsv = new float[4 * size];
        //float[] ahsv2 = new float[4 * size];
//
        //argb_fill(argb, size);
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
        //ahsv_from_argb_c(ahsv2, argb, size);
        //float f1 = ahsv[20];
        //float f2 = ahsv2[20];
        //assertArrayEquals(ahsv, ahsv2, 1e-6f);
        return ahsv;
    }

    //static int size = 16384;
    static float[] a = new float[4 * size];
    static float[] b = new float[4 * size];
    static float[] c = new float[4 * size];

    static {
        var r = new Random();
        for (int i = 0; i < a.length; i++) {
            a[i] = r.nextFloat();
            b[i] = r.nextFloat();
        }
    }

    @Benchmark
    public float[] single() {
        ahsv_from_argb_c(ahsv, argb, size);
        return ahsv;
    }

    //@Benchmark
    public float[] scalarComputation() {
        scalarComputation(a, b, c);
        return c;
    }

    void scalarComputation(float[] a, float[] b, float[] c) {
        for (int i = 0; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }
    }

    //@Benchmark
    public float[] vectorComputation() {
        vectorComputation(a, b, c);
        return c;
    }

    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    void vectorComputation(float[] a, float[] b, float[] c) {
        int i = 0;
        int upperBound = SPECIES.loopBound(a.length);
        for (; i < upperBound; i += SPECIES.length()) {
            // FloatVector va, vb, vc;
            var va = FloatVector.fromArray(SPECIES, a, i);
            var vb = FloatVector.fromArray(SPECIES, b, i);
            var vc = va.mul(va)
                    .add(vb.mul(vb))
                    .neg();
            vc.intoArray(c, i);
        }
        for (; i < a.length; i++) {
            c[i] = (a[i] * a[i] + b[i] * b[i]) * -1.0f;
        }
    }
}
