package com.degreework.fractalimagecompression.model;

import lombok.AllArgsConstructor;
import lombok.Data;
@Data
@AllArgsConstructor
public class TransformedBlock {
    private byte[] data;
    int width;
    int height;
    int channels;

    private int k;
    private int l;
    private int flip;
    private int angle;
}
