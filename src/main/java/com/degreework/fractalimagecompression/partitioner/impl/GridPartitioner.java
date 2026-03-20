package com.degreework.fractalimagecompression.partitioner.impl;

import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class GridPartitioner implements ImagePartitioner {
    @Override
    public List<Rect> partition(Mat img, int blockSize, int step) {
        List<Rect> rects = new ArrayList<>();
        int rows = img.rows();
        int cols = img.cols();

        for (int y = 0; y <= rows - blockSize; y += step) {
            for (int x = 0; x <= cols - blockSize; x += step) {
                rects.add(new Rect(x, y, blockSize, blockSize));
            }
        }
        return rects;
    }
}
