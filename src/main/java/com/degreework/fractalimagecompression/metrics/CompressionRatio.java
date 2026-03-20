package com.degreework.fractalimagecompression.metrics;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import org.opencv.core.Mat;

import java.util.List;

public class CompressionRatio {

    private static final int BYTES_PER_TRANSFORMATION = 48;

    public double compute(Mat original,
                          List<BlockTransformation> transformationList) {
        double originalSize = original.total() * original.elemSize();
        double compressedSize = transformationList.size() * BYTES_PER_TRANSFORMATION;
        return originalSize / compressedSize;
    }
}
