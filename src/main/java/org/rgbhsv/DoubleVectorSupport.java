package org.rgbhsv;

import jdk.incubator.vector.DoubleVector;
import jdk.incubator.vector.VectorSpecies;

import java.util.Arrays;

public class DoubleVectorSupport {
    public static DoubleVector fromSingle(VectorSpecies<Double> species, double single) {
        double[] a = new double[species.length()];
        Arrays.fill(a, single);
        return DoubleVector.fromArray(species, a, 0);
    }

    public static DoubleVector indexVector(VectorSpecies<Double> species) {
        double[] a = new double[species.length()];
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        return DoubleVector.fromArray(species, a, 0);
    }
}
