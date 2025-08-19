package org.rgbhsv;

import jdk.incubator.vector.*;

import static java.lang.Math.*;
import static jdk.incubator.vector.VectorOperators.*;
import static org.rgbhsv.Float128Transpose.*;

public class RgbHsv {

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

    public static void ahsv_from_argb_c(
            float[] dst_a, float[] dst_h, float[] dst_s, float[] dst_v,
            float[] src_a, float[] src_r, float[] src_g, float[] src_b,
            int length) {
        for (int i = length, offset = 0; i > 0; i--, offset += 1) {
            float a = src_a[offset];
            float r = src_r[offset];
            float g = src_g[offset];
            float b = src_b[offset];

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

            dst_a[offset] = a;
            dst_h[offset] = h;
            dst_s[offset] = s;
            dst_v[offset] = v;
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

    static void argb_from_ahsv_c(float[] dst_a, float[] dst_r, float[] dst_g, float[] dst_b,
                                 float[] src_a, float[] src_h, float[] src_s, float[] src_v,
                                 int length) {
        for (int i = length, offset = 0; i > 0; i--, offset += 1) {
            float a = src_a[offset];
            float h = src_h[offset];
            float s = src_s[offset];
            float v = src_v[offset];

            // The HUE should be at range [0, 1], convert 1.0 to 0.0 if needed.
            if (h >= 1.0f)
                h -= 1.0f;

            dst_a[offset] = a;

            h *= 6.0f;
            int index = (int)h;

            float f = h - (float)index;
            float p = v * (1.0f - s);
            float q = v * (1.0f - s * f);
            float t = v * (1.0f - s * (1.0f - f));

            switch (index) {
                case 0 -> {
                    dst_r[offset] = v;
                    dst_g[offset] = t;
                    dst_b[offset] = p;
                }
                case 1 -> {
                    dst_r[offset] = q;
                    dst_g[offset] = v;
                    dst_b[offset] = p;
                }
                case 2 -> {
                    dst_r[offset] = p;
                    dst_g[offset] = v;
                    dst_b[offset] = t;
                }
                case 3 -> {
                    dst_r[offset] = p;
                    dst_g[offset] = q;
                    dst_b[offset] = v;
                }
                case 4 -> {
                    dst_r[offset] = t;
                    dst_g[offset] = p;
                    dst_b[offset] = v;
                }
                case 5 -> {
                    dst_r[offset] = v;
                    dst_g[offset] = p;
                    dst_b[offset] = q;
                }
            }
        }
    }

    static final VectorSpecies<Float> SPECIES = FloatVector.SPECIES_PREFERRED;

    static void argb_from_ahsv_sse2(float[] dst, float[] src, int length, VectorSpecies<Float> species) {
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);
        var p4 = FloatVectorSupport.fromSingle(species, 4.0f);

        final var speciesLength = species.length();
        var f1 = new int[speciesLength];
        var f2 = new int[speciesLength];
        var f3 = new int[speciesLength];
        var f4 = new int[speciesLength];

        for (int i = 0; i < speciesLength; i++) {
            f1[i] = i * 4 /* + 0 */;
            f2[i] = i * 4 + 1;
            f3[i] = i * 4 + 2;
            f4[i] = i * 4 + 3;
        }

        //x/M = (x*(2^n/M))>>n
        //var M = 12;
        //var n = 4; //todo
        //var magicNumber = Math.pow(2, n) / M;//todo

        int i = length;
        int offset = 0;
        while ((i -= speciesLength) >= 0) {

            var xA = FloatVector.fromArray(species, src, offset, f1, 0);
            var xH = FloatVector.fromArray(species, src, offset, f2, 0);
            var xS = FloatVector.fromArray(species, src, offset, f3, 0);
            var xV = FloatVector.fromArray(species, src, offset, f4, 0);

            // the general case
            // a % b = a - floor(a / b) * b
            //xH = xH.broadcast(.723f);
            // k = (n + H / 30) mod 12
            var ho60 = xH.mul(360).div(60);
            // todo: how to lanewise mod
            // todo: #2 maybe since the values are 'reduced' this can work?
            // todo: tests pass, but we still need to validate this is actually faster/correct
            FloatVector kR = ho60.add(5);//.mod(6);
            FloatVector kG = ho60.add(3);//.mod(6);
            FloatVector kB = ho60.add(1);//.mod(6);

            var zR = kR.compare(GE, 6);
            var zG = kG.compare(GE, 6);
            var zB = kB.compare(GE, 6);

            kR = kR.sub(6, zR);
            kG = kG.sub(6, zG);
            kB = kB.sub(6, zB);

            var xR = xV.sub(xV.mul(xS).mul(p0.max(kR.min(p4.sub(kR)).min(p1))));
            var xG = xV.sub(xV.mul(xS).mul(p0.max(kG.min(p4.sub(kG)).min(p1))));
            var xB = xV.sub(xV.mul(xS).mul(p0.max(kB.min(p4.sub(kB)).min(p1))));

            xA.intoArray(dst, offset, f1, 0);
            xR.intoArray(dst, offset, f2, 0);
            xG.intoArray(dst, offset, f3, 0);
            xB.intoArray(dst, offset, f4, 0);

            offset += 4 * speciesLength;
        }
    }

    static void argb_from_ahsv_sse2(float[] dst_a, float[] dst_r, float[] dst_g, float[] dst_b,
                                    float[] src_a, float[] src_h, float[] src_s, float[] src_v
            , int length, VectorSpecies<Float> species) {
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);
        var p4 = FloatVectorSupport.fromSingle(species, 4.0f);

        final var speciesLength = species.length();

        int i = length;
        int offset = 0;
        while ((i -= speciesLength) >= 0) {

            var xA = FloatVector.fromArray(species, src_a, offset);
            var xH = FloatVector.fromArray(species, src_h, offset);
            var xS = FloatVector.fromArray(species, src_s, offset);
            var xV = FloatVector.fromArray(species, src_v, offset);

            // the general case
            // a % b = a - floor(a / b) * b
            //xH = xH.broadcast(.723f);
            // k = (n + H / 30) mod 12
            var ho60 = xH.mul(360).div(60);
            // todo: how to lanewise mod
            // todo: #2 maybe since the values are 'reduced' this can work?
            // todo: tests pass, but we still need to validate this is actually faster/correct
            FloatVector kR = ho60.add(5);//.mod(6);
            FloatVector kG = ho60.add(3);//.mod(6);
            FloatVector kB = ho60.add(1);//.mod(6);

            var zR = kR.compare(GE, 6);
            var zG = kG.compare(GE, 6);
            var zB = kB.compare(GE, 6);

            kR = kR.sub(6, zR);
            kG = kG.sub(6, zG);
            kB = kB.sub(6, zB);

            var xR = xV.sub(xV.mul(xS).mul(p0.max(kR.min(p4.sub(kR)).min(p1))));
            var xG = xV.sub(xV.mul(xS).mul(p0.max(kG.min(p4.sub(kG)).min(p1))));
            var xB = xV.sub(xV.mul(xS).mul(p0.max(kB.min(p4.sub(kB)).min(p1))));

            xA.intoArray(dst_a, offset);
            xR.intoArray(dst_r, offset);
            xG.intoArray(dst_g, offset);
            xB.intoArray(dst_b, offset);

            offset += speciesLength;
        }
    }

    /**
     * This is translated from code available here https://github.com/wqren/rgbhsv/blob/master/src/rgbhsv.cpp
     * The code is similar to the paper describing the sse implementation in
     * https://www.daaam.info/Downloads/Pdfs/proceedings/proceedings_2011/780.pdf
     * It is assumed that all bitwise and and or functions can be replaced with the operation that has the same result.
     *
     * The original paper describes this algorithm as numerically stable.
     * @param dst
     * @param src
     * @param length
     * @param species
     */
    public static void ahsv_from_argb_sse2(float[] dst, float[] src, int length, VectorSpecies<Float> species) {

        // todo: note this could probably work out if we add support for masks.
        //  now we've seen some code like this before, perhaps the code could be modified at build time to generate
        //  code that contains masks?
        if (length < species.length()) {
            // todo: need to handle smaller input
            throw new RuntimeException("Not Implemented");
        } else if (length % species.length() != 0) {
            // todo: need to handle leftover input
            throw new RuntimeException("Not Implemented");
        }

        var m4o6 = FloatVectorSupport.fromSingle(species, -4.0f / 6.0f);
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var epsilon = FloatVectorSupport.fromSingle(species, 1e-8f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);

        //var index = FloatVectorSupport.indexVector(species);

        final var speciesLength = species.length();
        var f1 = new int[speciesLength];
        var f2 = new int[speciesLength];
        var f3 = new int[speciesLength];
        var f4 = new int[speciesLength];

        for (int i = 0; i < speciesLength; i++) {
            f1[i] = i * 4 /* + 0 */;
            f2[i] = i * 4 + 1;
            f3[i] = i * 4 + 2;
            f4[i] = i * 4 + 3;
        }

        FloatVector xG, xB, xA, xR;
        FloatVector xH, xS, xV, xC;
        VectorMask<Float> xX, xY, xZ;

        int i = length;
        int offset = 0;
        while ((i -= speciesLength) >= 0) {
            // todo: this only works when the amount of pixels is 4 aligned.
            //  needs code to handle leftover pixels. maybe use mask? check jep for details

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
            // todo: in the future it'd be nice to replace this with a generic (lane width independent) transpose.
            if (speciesLength == 2) {
                var row1 = FloatVector.fromArray(species, src, offset);
                var row2 = FloatVector.fromArray(species, src, offset + speciesLength);
                var row3 = FloatVector.fromArray(species, src, offset + 2 * speciesLength);
                var row4 = FloatVector.fromArray(species, src, offset + 3 * speciesLength);

                xA = row1.rearrange(Float64Transpose.t1, row3);
                xR = row1.rearrange(Float64Transpose.t2, row3);
                xG = row2.rearrange(Float64Transpose.t1, row4);
                xB = row2.rearrange(Float64Transpose.t2, row4);
            } else if (speciesLength == 4) {
                var row1 = FloatVector.fromArray(species, src, offset);
                var row2 = FloatVector.fromArray(species, src, offset + speciesLength);
                var row3 = FloatVector.fromArray(species, src, offset + 2 * speciesLength);
                var row4 = FloatVector.fromArray(species, src, offset + 3 * speciesLength);

                var a0 = row1.rearrange(interleave32_lft, row2);
                var a1 = row1.rearrange(interleave32_rgt, row2);
                var a2 = row3.rearrange(interleave32_lft, row4);
                var a3 = row3.rearrange(interleave32_rgt, row4);

                xA = a0.rearrange(interleave64_lft, a2);
                xR = a0.rearrange(interleave64_rgt, a2);
                xG = a1.rearrange(interleave64_lft, a3);
                xB = a1.rearrange(interleave64_rgt, a3);
            } else if (speciesLength == 8) {
                var row1 = FloatVector.fromArray(species, src, offset);
                var row2 = FloatVector.fromArray(species, src, offset + speciesLength);
                var row3 = FloatVector.fromArray(species, src, offset + 2 * speciesLength);
                var row4 = FloatVector.fromArray(species, src, offset + 3 * speciesLength);

                var a00 = row1.rearrange(Float256Transpose.shuffle_256_1, row2);
                var a10 = row3.rearrange(Float256Transpose.shuffle_256_1, row4);
                var a01 = row1.rearrange(Float256Transpose.shuffle_256_2, row2);
                var a11 = row3.rearrange(Float256Transpose.shuffle_256_2, row4);

                xA = a00.rearrange(Float256Transpose.shuffle_256_3, a10);
                xR = a00.rearrange(Float256Transpose.shuffle_256_4, a10);
                xG = a01.rearrange(Float256Transpose.shuffle_256_3, a11);
                xB = a01.rearrange(Float256Transpose.shuffle_256_4, a11);
            } else {
                // todo
                xA = FloatVector.fromArray(species, src, offset, f1, 0);
                xR = FloatVector.fromArray(species, src, offset, f2, 0);
                xG = FloatVector.fromArray(species, src, offset, f3, 0);
                xB = FloatVector.fromArray(species, src, offset, f4, 0);
            }

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

            xR = p0.blend(xR, xX);    // xR <- [X!=0 ? R : 0]
            xB = p0.blend(xB, xZ);    // xB <- [Z!=0 ? B : 0]
            xG = p0.blend(xG, xY);    // xG <- [Y!=0 ? G : 0]

            xZ = xZ.not();
            xY = xY.not();

            xG = xG.lanewise(NEG, xZ);      // xG <- [Y!=0 ? (Z==0 ? G : -G) : 0]
            xR = xR.lanewise(NEG, xY);      // xR <- [X!=0 ? (Y==0 ? R : -R) : 0]

            // G is now accumulator.
            xG = xG.add(xR);                 // xG <- [Rx + Gx]
            xB = xB.lanewise(NEG, xY);       // xB <- [Z!=0 ? (Y==0 ? B : -B) : 0]

            //var m6 =
            xC = xC.mul(-6.f);               // xC <- [C*6     ]
            xG = xG.sub(xB);                 // xG <- [Rx+Gx+Bx]


            xH = p0.blend(m4o6, xX);         // xH <- [V==R ?0 :-4/6]
            xG = xG.div(xC);                 // xG <- [(Rx+Gx+Bx)/6C]

            // Correct the achromatic case - H/S may be infinite (or near) due to division by zero.
            xH = xH.lanewise(NEG, xZ);           // xH <- [V==R ? 0 : V==G ? -4/6 : 4/6]
            var xCm = epsilon.compare(LE, xC);
            xH = xH.add(1.f);                   // xH <- [V==R ? 1 : V==G ?  2/6 :10/6]

            xG = xG.add(xH);

            // Normalize H to a fraction. If it's greater than or equal to 1 then 1 is subtracted
            // to get the Hue at [0..1) domain.
            var xHm = p1.compare(LE, xG);

            xH = p0.blend(p1, xHm);
            xS = p0.blend(xS, xCm);
            xG = p0.blend(xG, xCm);
            xG = xG.sub(xH);

            if (species.length() == 2) {
                // todo
                xA.intoArray(dst, offset, f1, 0);
                xG.intoArray(dst, offset, f2, 0);
                xS.intoArray(dst, offset, f3, 0);
                xV.intoArray(dst, offset, f4, 0);
            } else if (species.length() == 4) {
                var a0 = xA.rearrange(interleave32_lft, xG);
                var a1 = xA.rearrange(interleave32_rgt, xG);
                var a2 = xS.rearrange(interleave32_lft, xV);
                var a3 = xS.rearrange(interleave32_rgt, xV);

                var row1 = a0.rearrange(interleave64_lft, a2);
                var row2 = a0.rearrange(interleave64_rgt, a2);
                var row3 = a1.rearrange(interleave64_lft, a3);
                var row4 = a1.rearrange(interleave64_rgt, a3);

                row1.intoArray(dst, offset);
                row2.intoArray(dst, offset + speciesLength);
                row3.intoArray(dst, offset + 2 * speciesLength);
                row4.intoArray(dst, offset + 3 * speciesLength);
            } else if (species.length() == 8) {
                // todo
                xA.intoArray(dst, offset, f1, 0);
                xG.intoArray(dst, offset, f2, 0);
                xS.intoArray(dst, offset, f3, 0);
                xV.intoArray(dst, offset, f4, 0);
            } else {
                xA.intoArray(dst, offset, f1, 0);
                xG.intoArray(dst, offset, f2, 0);
                xS.intoArray(dst, offset, f3, 0);
                xV.intoArray(dst, offset, f4, 0);
            }

            offset += 4 * speciesLength;
        }
    }

    // todo: this will be a composed funciton.
    // the goal is, if this is as fast as the other options, to use this instead of
    // transposed/swizzled data.
    // all of that will be hoisted out to it's own setup/teardown.
    /*
    public static void ahsv_from_argb_sse2(
            IntFunction<FloatVector> f1, IntFunction<FloatVector> f2, IntFunction<FloatVector> f3, IntFunction<FloatVector> f4,
            //dst_a, float[] dst_h, float[] dst_s, float[] dst_v,
            //float[] src_a, float[] src_r, float[] src_g, float[] src_b,
            int length, VectorSpecies<Float> species) {

        // todo: note this could probably work out if we add support for masks.
        //  now we've seen some code like this before, perhaps the code could be modified at build time to generate
        //  code that contains masks?
        if (length < species.length()) {
            // todo: need to handle smaller input
            throw new RuntimeException("Not Implemented");
        } else if (length % species.length() != 0) {
            // todo: need to handle leftover input
            throw new RuntimeException("Not Implemented");
        }

        var m4o6 = FloatVectorSupport.fromSingle(species, -4.0f / 6.0f);
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var epsilon = FloatVectorSupport.fromSingle(species, 1e-8f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);

        final var speciesLength = species.length();

        FloatVector xG, xB, xA, xR;
        FloatVector xH, xS, xV, xC;
        VectorMask<Float> xX, xY, xZ;

        int i = length;
        int offset = 0;
        while ((i -= speciesLength) >= 0) {
            // todo: this only works when the amount of pixels is 4 aligned.
            //  needs code to handle leftover pixels. maybe use mask? check jep for details

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
            // todo: in the future it'd be nice to replace this with a generic (lane width independent) transpose.
            xA = FloatVector.fromArray(species, src_a, offset);
            xR = FloatVector.fromArray(species, src_r, offset);
            xG = FloatVector.fromArray(species, src_g, offset);
            xB = FloatVector.fromArray(species, src_b, offset);

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

            xR = p0.blend(xR, xX);    // xR <- [X!=0 ? R : 0]
            xB = p0.blend(xB, xZ);    // xB <- [Z!=0 ? B : 0]
            xG = p0.blend(xG, xY);    // xG <- [Y!=0 ? G : 0]

            xZ = xZ.not();
            xY = xY.not();

            xG = xG.lanewise(NEG, xZ);      // xG <- [Y!=0 ? (Z==0 ? G : -G) : 0]
            xR = xR.lanewise(NEG, xY);      // xR <- [X!=0 ? (Y==0 ? R : -R) : 0]

            // G is now accumulator.
            xG = xG.add(xR);                 // xG <- [Rx + Gx]
            xB = xB.lanewise(NEG, xY);       // xB <- [Z!=0 ? (Y==0 ? B : -B) : 0]

            //var m6 =
            xC = xC.mul(-6.f);               // xC <- [C*6     ]
            xG = xG.sub(xB);                 // xG <- [Rx+Gx+Bx]


            xH = p0.blend(m4o6, xX);         // xH <- [V==R ?0 :-4/6]
            xG = xG.div(xC);                 // xG <- [(Rx+Gx+Bx)/6C]

            // Correct the achromatic case - H/S may be infinite (or near) due to division by zero.
            xH = xH.lanewise(NEG, xZ);           // xH <- [V==R ? 0 : V==G ? -4/6 : 4/6]
            var xCm = epsilon.compare(LE, xC);
            xH = xH.add(1.f);                   // xH <- [V==R ? 1 : V==G ?  2/6 :10/6]

            xG = xG.add(xH);

            // Normalize H to a fraction. If it's greater than or equal to 1 then 1 is subtracted
            // to get the Hue at [0..1) domain.
            var xHm = p1.compare(LE, xG);

            xH = p0.blend(p1, xHm);
            xS = p0.blend(xS, xCm);
            xG = p0.blend(xG, xCm);
            xG = xG.sub(xH);

            xA.intoArray(dst_a, offset);
            xG.intoArray(dst_h, offset);
            xS.intoArray(dst_s, offset);
            xV.intoArray(dst_v, offset);

            offset += speciesLength;
        }
    }

     */

    public static void ahsv_from_argb_sse2(
            float[] dst_a, float[] dst_h, float[] dst_s, float[] dst_v,
            float[] src_a, float[] src_r, float[] src_g, float[] src_b,
            int length, VectorSpecies<Float> species) {

        // todo: note this could probably work out if we add support for masks.
        //  now we've seen some code like this before, perhaps the code could be modified at build time to generate
        //  code that contains masks?
        if (length < species.length()) {
            // todo: need to handle smaller input
            throw new RuntimeException("Not Implemented");
        } else if (length % species.length() != 0) {
            // todo: need to handle leftover input
            throw new RuntimeException("Not Implemented");
        }

        var m4o6 = FloatVectorSupport.fromSingle(species, -4.0f / 6.0f);
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var epsilon = FloatVectorSupport.fromSingle(species, 1e-8f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);

        final var speciesLength = species.length();

        FloatVector xG, xB, xA, xR;
        FloatVector xH, xS, xV, xC;
        VectorMask<Float> xX, xY, xZ;

        int i = length;
        int offset = 0;
        while ((i -= speciesLength) >= 0) {
            // todo: this only works when the amount of pixels is 4 aligned.
            //  needs code to handle leftover pixels. maybe use mask? check jep for details

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
            // todo: in the future it'd be nice to replace this with a generic (lane width independent) transpose.
            xA = FloatVector.fromArray(species, src_a, offset);
            xR = FloatVector.fromArray(species, src_r, offset);
            xG = FloatVector.fromArray(species, src_g, offset);
            xB = FloatVector.fromArray(species, src_b, offset);

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

            xR = p0.blend(xR, xX);    // xR <- [X!=0 ? R : 0]
            xB = p0.blend(xB, xZ);    // xB <- [Z!=0 ? B : 0]
            xG = p0.blend(xG, xY);    // xG <- [Y!=0 ? G : 0]

            xZ = xZ.not();
            xY = xY.not();

            xG = xG.lanewise(NEG, xZ);      // xG <- [Y!=0 ? (Z==0 ? G : -G) : 0]
            xR = xR.lanewise(NEG, xY);      // xR <- [X!=0 ? (Y==0 ? R : -R) : 0]

            // G is now accumulator.
            xG = xG.add(xR);                 // xG <- [Rx + Gx]
            xB = xB.lanewise(NEG, xY);       // xB <- [Z!=0 ? (Y==0 ? B : -B) : 0]

            //var m6 =
            xC = xC.mul(-6.f);               // xC <- [C*6     ]
            xG = xG.sub(xB);                 // xG <- [Rx+Gx+Bx]


            xH = p0.blend(m4o6, xX);         // xH <- [V==R ?0 :-4/6]
            xG = xG.div(xC);                 // xG <- [(Rx+Gx+Bx)/6C]

            // Correct the achromatic case - H/S may be infinite (or near) due to division by zero.
            xH = xH.lanewise(NEG, xZ);           // xH <- [V==R ? 0 : V==G ? -4/6 : 4/6]
            var xCm = epsilon.compare(LE, xC);
            xH = xH.add(1.f);                   // xH <- [V==R ? 1 : V==G ?  2/6 :10/6]

            xG = xG.add(xH);

            // Normalize H to a fraction. If it's greater than or equal to 1 then 1 is subtracted
            // to get the Hue at [0..1) domain.
            var xHm = p1.compare(LE, xG);

            xH = p0.blend(p1, xHm);
            xS = p0.blend(xS, xCm);
            xG = p0.blend(xG, xCm);
            xG = xG.sub(xH);

            xA.intoArray(dst_a, offset);
            xG.intoArray(dst_h, offset);
            xS.intoArray(dst_s, offset);
            xV.intoArray(dst_v, offset);

            offset += speciesLength;
        }
    }
}
