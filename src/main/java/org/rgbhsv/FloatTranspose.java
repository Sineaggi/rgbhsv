package org.rgbhsv;

import jdk.incubator.vector.FloatVector;

public interface FloatTranspose {
    record Result(FloatVector col1, FloatVector col2, FloatVector col3, FloatVector col4) {
    }

    Result transpose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4);

    Result itranspose(FloatVector row1, FloatVector row2, FloatVector row3, FloatVector row4);
}
