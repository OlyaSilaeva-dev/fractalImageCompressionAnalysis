package com.degreework.fractalimagecompression.metrics;

import org.opencv.core.Mat;

public interface Metric {
    double compute(Mat original, Mat reconstructed);
}
