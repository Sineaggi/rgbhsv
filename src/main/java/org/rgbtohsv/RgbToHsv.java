package org.rgbtohsv;

import jdk.incubator.vector.*;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static jdk.incubator.vector.VectorOperators.LE;
import static jdk.incubator.vector.VectorOperators.NEG;

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

    public static void ahsv_from_argb_sse2(float[] dst, float[] src, int length, VectorSpecies<Float> species) {

        var m4o6 = FloatVectorSupport.fromSingle(species, -4.0f / 6.0f);
        var p0 = FloatVectorSupport.fromSingle(species, 0f);
        var epsilon = FloatVectorSupport.fromSingle(species, 1e-8f);
        var p1 = FloatVectorSupport.fromSingle(species, 1.0f);

        var index = FloatVectorSupport.indexVector(species);

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
            xA = FloatVector.fromArray(species, src, offset, f1, 0);
            xR = FloatVector.fromArray(species, src, offset, f2, 0);
            xG = FloatVector.fromArray(species, src, offset, f3, 0);
            xB = FloatVector.fromArray(species, src, offset, f4, 0);

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

            xA.intoArray(dst, offset, f1, 0);
            xG.intoArray(dst, offset, f2, 0);
            xS.intoArray(dst, offset, f3, 0);
            xV.intoArray(dst, offset, f4, 0);

            offset += 4 * speciesLength;
        }
    }
}
