package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.rgbhsv.RgbHsv.*;

@Fork(value = 1, jvmArgsAppend = {"-server", "-disablesystemassertions"})
public class JmhMain {
    public static void main(String[] args) throws IOException {
        Main.main(args);
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

    static int size;
    static float[] argb;
    static float[] ahsv;

    static float[] argb_a;
    static float[] argb_r;
    static float[] argb_g;
    static float[] argb_b;

    static float[] ahsv_a;
    static float[] ahsv_h;
    static float[] ahsv_s;
    static float[] ahsv_v;

    static {
        try (var is = Objects.requireNonNull(JmhMain.class.getResourceAsStream("/example.png"), "failed to find example file")) {
            var image = ImageIO.read(is);

            int w = image.getWidth();
            int h = image.getHeight();

            int[] dataBuffInt = image.getRGB(0, 0, w, h, null, 0, w);
            //System.out.println(dataBuffInt);
            size = dataBuffInt.length;
            argb = new float[size * 4];
            ahsv = new float[size * 4];

            argb_a = new float[size];
            argb_r = new float[size];
            argb_g = new float[size];
            argb_b = new float[size];
            ahsv_a = new float[size];
            ahsv_h = new float[size];
            ahsv_s = new float[size];
            ahsv_v = new float[size];

            for (int i = 0; i < size; i++) {
                var c = new Color(dataBuffInt[i]);
                argb[0 + 4 * i] = c.getAlpha() / 255f;
                argb[1 + 4 * i] = c.getRed() / 255f;
                argb[2 + 4 * i] = c.getGreen() / 255f;
                argb[3 + 4 * i] = c.getBlue() / 255f;

                argb_a[i] = c.getAlpha() / 255f;
                argb_r[i] = c.getRed() / 255f;
                argb_g[i] = c.getGreen() / 255f;
                argb_b[i] = c.getBlue() / 255f;
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void ahsv_from_argb_vector(Blackhole blackhole) {
        ahsv_from_argb_sse2(ahsv, argb, size, SPECIES);
        blackhole.consume(ahsv);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void ahsv_from_argb_scalar(Blackhole blackhole) {
        ahsv_from_argb_c(ahsv, argb, size);
        blackhole.consume(ahsv);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void ahsv_from_argb_scalar2(Blackhole blackhole) {
        ahsv_from_argb_c(ahsv_a, ahsv_h, ahsv_s, ahsv_v,
                argb_a,argb_r, argb_g, argb_b, size);
        blackhole.consume(ahsv_a);
        blackhole.consume(ahsv_h);
        blackhole.consume(ahsv_s);
        blackhole.consume(ahsv_v);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void ahsv_from_argb_vector2(Blackhole blackhole) {
        ahsv_from_argb_sse2(ahsv_a, ahsv_h, ahsv_s, ahsv_v,
                argb_a,argb_r, argb_g, argb_b, size, SPECIES);
        blackhole.consume(ahsv_a);
        blackhole.consume(ahsv_h);
        blackhole.consume(ahsv_s);
        blackhole.consume(ahsv_v);
    }

    // ahsv

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void argb_from_ahsv_vector(Blackhole blackhole) {
        argb_from_ahsv_sse2(argb, ahsv, size, SPECIES);
        blackhole.consume(argb);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void argb_from_ahsv_scalar(Blackhole blackhole) {
        argb_from_ahsv_c(argb, ahsv, size);
        blackhole.consume(argb);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void argb_from_ahsv_scalar2(Blackhole blackhole) {
        argb_from_ahsv_c(argb_a,argb_r, argb_g, argb_b,
                ahsv_a, ahsv_h, ahsv_s, ahsv_v,
                size);
        blackhole.consume(ahsv_a);
        blackhole.consume(ahsv_h);
        blackhole.consume(ahsv_s);
        blackhole.consume(ahsv_v);
    }

    @Benchmark
    @Warmup(iterations = 1, time = 1, timeUnit = TimeUnit.SECONDS)
    public void argb_from_ahsv_vector2(Blackhole blackhole) {
        argb_from_ahsv_sse2(argb_a,argb_r, argb_g, argb_b,
                ahsv_a, ahsv_h, ahsv_s, ahsv_v, size, SPECIES);
        blackhole.consume(ahsv_a);
        blackhole.consume(ahsv_h);
        blackhole.consume(ahsv_s);
        blackhole.consume(ahsv_v);
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
