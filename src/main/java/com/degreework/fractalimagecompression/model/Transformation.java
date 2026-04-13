package com.degreework.fractalimagecompression.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transformation {
    private int k;
    private int l;
    private int flip;
    private int angle;
    private double[] contrast;
    private double[] brightness;
}
