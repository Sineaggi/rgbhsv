package org.rgbtohsv;

import jdk.incubator.vector.*;

import java.util.Arrays;

import static java.lang.Math.*;
import static jdk.incubator.vector.VectorOperators.*;

public class RgbToHsv {

    public static void ahsv_from_argb_c(float[] dst, float []src, int length) {
        for (int i = length, offset = 0; i > 0; i--, offset += 4) {
            float a = src[offset /* + 0 */];
            float r = src[offset + 1];
            float g = src[offset + 2];
            float b = src[offset + 3];

            float m = min(min(r, g), b);
            float v = max(max(r, g), b);
            float c = v - m;

            float h = 0.0f;
            float s = 0.0f;
            float x;

            if (c != 0.0f) {
                s = c / v;

                if (v == r) {
                    h = g - b;
                    x = 1.0f;
                }
                else if (v == g) {
                    h =  b - r;
                    x = 1.0f / 3.0f;
                }
                else {
                    h = r - g;
                    x = 2.0f / 3.0f;
                }

                h /= 6.0f * c;
                h += x;

                if (h >= 1.0f)
                    h -= 1.0f;
            }

            dst[offset /* + 0 */] = a;
            dst[offset + 1] = h;
            dst[offset + 2] = s;
            dst[offset + 3] = v;
        }
    }

    static void argb_from_ahsv_c(float[] dst, float[] src, int length) {
        for (int i = length, offset = 0; i > 0; i--, offset += 4) {
            float a = src[offset /* + 0 */];
            float h = src[offset + 1];
            float s = src[offset + 2];
            float v = src[offset + 3];

            // The HUE should be at range [0, 1], convert 1.0 to 0.0 if needed.
            if (h >= 1.0f)
                h -= 1.0f;

            dst[offset /* + 0 */] = a;

            h *= 6.0f;
            int index = (int)h;

            float f = h - (float)index;
            float p = v * (1.0f - s);
            float q = v * (1.0f - s * f);
            float t = v * (1.0f - s * (1.0f - f));

            switch (index) {
                case 0 -> {
                    dst[offset + 1] = v;
                    dst[offset + 2] = t;
                    dst[offset + 3] = p;
                }
                case 1 -> {
                    dst[offset + 1] = q;
                    dst[offset + 2] = v;
                    dst[offset + 3] = p;
                }
                case 2 -> {
                    dst[offset + 1] = p;
                    dst[offset + 2] = v;
                    dst[offset + 3] = t;
                }
                case 3 -> {
                    dst[offset + 1] = p;
                    dst[offset + 2] = q;
                    dst[offset + 3] = v;
                }
                case 4 -> {
                    dst[offset + 1] = t;
                    dst[offset + 2] = p;
                    dst[offset + 3] = v;
                }
                case 5 -> {
                    dst[offset + 1] = v;
                    dst[offset + 2] = p;
                    dst[offset + 3] = q;
                }
            }
        }
    }

    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_128;

    static void argb_from_ahsv_sse2(float[] dst, float[] src, int length) {
        throw new RuntimeException("Not yet implemented");
    }

    private static FloatVector fromSingle(VectorSpecies<Float> species, float single) {
        float[] a = new float[species.length()];
        Arrays.fill(a, single);
        return FloatVector.fromArray(SPECIES, a, 0);
    }

    public static void ahsv_from_argb_sse2(float[] dst, float[] src, int length, VectorSpecies<Float> species) {

        //System.out.println(SPECIES.length());

        final var speciesLength = species.length();

        var m4o6 = FloatVector.fromArray(SPECIES, new float[]{-4.0f / 6.0f, -4.0f / 6.0f, -4.0f / 6.0f, -4.0f / 6.0f}, 0);
        var p0 = FloatVector.fromArray(SPECIES, new float[]{0f, 0f, 0f, 0f}, 0);
        var epsilon = FloatVector.fromArray(SPECIES, new float[]{1e-8f, 1e-8f, 1e-8f, 1e-8f}, 0);
        var index = FloatVector.fromArray(SPECIES, new float[]{0f, 1f, 2f, 3f}, 0);
        var p1 = FloatVector.fromArray(SPECIES, new float[]{1.0f, 1.0f, 1.0f, 1.0f}, 0);

        var f1 = new int[speciesLength];
        var f2 = new int[speciesLength];
        var f3 = new int[speciesLength];
        var f4 = new int[speciesLength];

        for (int i = 0; i < speciesLength; i++) {
            f1[i] = 0 + i * 4;
            f2[i] = 1 + i * 4;
            f3[i] = 2 + i * 4;
            f4[i] = 3 + i * 4;
        }
        //var f1 = new int[] {0, 4, 8, 12};
        //var f2 = new int[] {1, 5, 9, 13};
        //var f3 = new int[] {2, 6, 10, 14};
        //var f4 = new int[] {3, 7, 11, 15};

        //FloatVector xA, xR, xG, xB;
        FloatVector xG, xB, xA, xR;
        FloatVector xH, xS, xV, xC;
        VectorMask<Float> xX, xY, xZ;

        int i = length;
        //for (; i < SPECIES.loopBound(length); i += SPECIES.length()) {
        int offset = 0;
        while ((i -= speciesLength) >= 0) {
            // todo: write some masking shit? also this only works when the amount of pixels is 4 aligned.
            //var va = FloatVector.fromArray(SPECIES, src, i);

            // Transpose.
            //
            // What we get: xA == [A3 A2 A1 A0] - Alpha channel.
            //              xR == [R3 R2 R1 R0] - Red   channel.
            //              xG == [G3 G2 G1 G0] - Green channel.
            //              xB == [B3 B2 B1 B0] - Blue  channel.
            //
            // What we use: xC - Temporary.
            //              xS - Temporary.
            //              xV - Temporary.
            xA = FloatVector.fromArray(SPECIES, src, offset, f1, 0);
            xR = FloatVector.fromArray(SPECIES, src, offset, f2, 0);
            xG = FloatVector.fromArray(SPECIES, src, offset, f3, 0);
            xB = FloatVector.fromArray(SPECIES, src, offset, f4, 0);
            //var vb = FloatVector.fromArray(SPECIES, src, i);

            // Calculate Value, Chroma, and Saturation.
            //
            // What we get: xC == [C3 C2 C1 C0 ] - Chroma.
            //              xV == [V3 V2 V1 V0 ] - Value == Max(R, G, B).
            //              xS == [S3 S2 S1 S0 ] - Saturation, possibly incorrect due to division
            //                                     by zero, corrected at the end of the algorithm.
            //
            // What we use: xR
            //              xG
            //              xB
            xS = xG.max(xB);                // xS <- [max(G, B)]
            xC = xG.min(xB);                // xC <- [min(G, B)]

            xS = xS.max(xR);                // xS <- [max(G, B, R)]
            xC = xC.min(xR);                // xC <- [min(G, B, R)]

            xV = xS;                          // xV <- [V    ]
            xS = xS.sub(xC);                 // xS <- [V - m]
            xS = xS.div(xV);                // xS <- [S    ]

            xC = xC.sub(xV);                // xC <- [V + m]

            // Calculate Hue.
            //
            // What we get: xG - Hue
            //              xC - Chroma * 6.
            //
            // What we use: xR - Destroyed during calculation.
            //              xG - Destroyed during calculation.
            //              xB - Destroyed during calculation.
            //              xC - Chroma.
            //              xX - Mask.
            //              xY - Mask.
            //              xZ - Mask.

            xZ = xV.compare(VectorOperators.EQ, xG);     // xZ <- [V==G]
            xX = xV.compare(VectorOperators.NE, xR);     // xX <- [V!=R]

            xY = xX.and(xZ);                               // xY <- [V!=R && V==G]
            xZ = xX.andNot(xZ);                           // xZ <- [V!=R && V!=G]

            xY = xY.not();  // xY <- [V==R || V!=G]
            xZ = xZ.not();  // xZ <- [V==R || V==G]

            //xR.
            //xR.
            xR = xR.mul(0, xX.not());  // xR <- [X!=0 ? R : 0]
            xB = xB.mul(0, xZ.not());  // xB <- [Z!=0 ? B : 0]
            xG = xG.mul(0, xY.not());  // xG <- [Y!=0 ? G : 0]

            //float[] sn = new float[] {-0.f, -0.f, -0.f, -0.f};
            //var ff = FloatVector.fromArray(SPECIES, sn, 0);
            //var xZv = ff.selectFrom(ff, xZ);
            //var xYv = ff.selectFrom(ff, xY);
            //xZ.not();
            //var vfm = VectorMask.fromValues(SPECIES, false, false, false, false);
            xZ = xZ.not();
            xY = xY.not();

            //float[] sn2 = new float[] {0, 0, 0, 0};
            //var ff2 = FloatVector.fromArray(SPECIES, sn2, 0);

            xG = xG.lanewise(NEG, xZ);//.selectFrom(ff2, xZ);
            xR = xR.lanewise(NEG, xY);//.selectFrom(ff2, xY);
            //xR = xR.lanewise(XOR, xYv);
            //System.out.println(xZv);

            // G is now accumulator.
            xG = xG.add(xR);
            xB = xB.lanewise(NEG, xY);

            //var m6 =
            xC = xC.mul(-6.f);
            xG = xG.sub(xB);


            xH = p0.selectFrom(m4o6, xX); //todo: selectFrom maybe not right. mult by 0 could also be good.
            xG = xG.div(xC);
            //System.out.println(xH);

            // Correct the achromatic case - H/S may be infinite (or near) due to division by zero.
            xH = xH.lanewise(NEG, xZ);
            var xCm = epsilon.compare(LE, xC);
            //System.out.println(xCm);
            xH = xH.add(1.f);

            xG = xG.add(xH);

            // Normalize H to a fraction. If it's greater than or equal to 1 then 1 is subtracted
            // to get the Hue at [0..1) domain.
            xH = index.selectFrom(p1, p1.compare(LE, xG));

            xS = index.selectFrom(xS, xCm);
            xG = index.selectFrom(xG, xCm);
            xG = xG.sub(xH);

            xA.intoArray(dst, offset, f1, 0);
            xG.intoArray(dst, offset, f2, 0);
            xS.intoArray(dst, offset, f3, 0);
            xV.intoArray(dst, offset, f4, 0);


            offset += 4 * speciesLength;
        }
    }
}
