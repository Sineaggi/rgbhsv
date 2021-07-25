package org.rgbhsv;

import jdk.incubator.vector.FloatVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public class FloatVectorSupport {
    public static FloatVector fromSingle(VectorSpecies<Float> species, float single) {
        float[] a = new float[species.length()];
        Arrays.fill(a, single);
        return FloatVector.fromArray(species, a, 0);
    }

    public static FloatVector indexVector(VectorSpecies<Float> species) {
        float[] a = new float[species.length()];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        return FloatVector.fromArray(species, a, 0);
    }
}
