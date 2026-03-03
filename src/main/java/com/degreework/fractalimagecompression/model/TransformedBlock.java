package com.degreework.fractalimagecompression.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.opencv.core.Mat;

@Data
@AllArgsConstructor
public class TransformedBlock {
    private Mat data;
    private int k;
    private int l;
    private int flip;
    private int angle;
}
