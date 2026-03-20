package com.degreework.fractalimagecompression.partitioner.impl;

import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.ArrayList;
import java.util.List;

public class QuadTreePartitioner implements ImagePartitioner {

    private final int minBlockSize;
    private final double varianceThreshold;

    public QuadTreePartitioner(int minBlockSize, double varianceThreshold) {
        this.minBlockSize = minBlockSize;
        this.varianceThreshold = varianceThreshold;
    }

    @Override
    public List<Rect> partition(Mat img, int blockSize, int step) {
        List<Rect> result = new ArrayList<>();

        split(img, new Rect(0, 0, img.cols(), img.rows()), result);

        return result;
    }

    private void split(Mat img, Rect rect, List<Rect> result) {

        if (rect.width <= minBlockSize || rect.height <= minBlockSize) {
            result.add(rect);
            return;
        }

        Mat sub = img.submat(rect);

        double variance = calculateVariance(sub);

        if (variance < varianceThreshold) {
            result.add(rect);
        } else {

            int halfW = rect.width / 2;
            int halfH = rect.height / 2;

            Rect r1 = new Rect(rect.x, rect.y, halfW, halfH);
            Rect r2 = new Rect(rect.x + halfW, rect.y, halfW, halfH);
            Rect r3 = new Rect(rect.x, rect.y + halfH, halfW, halfH);
            Rect r4 = new Rect(rect.x + halfW, rect.y + halfH, halfW, halfH);

            split(img, r1, result);
            split(img, r2, result);
            split(img, r3, result);
            split(img, r4, result);
        }

        sub.release();
    }

    private double calculateVariance(Mat block) {

        double sum = 0;
        double sumSq = 0;
        int n = (int) block.total();

        for (int i = 0; i < block.rows(); i++) {
            for (int j = 0; j < block.cols(); j++) {
                double val = block.get(i, j)[0];
                sum += val;
                sumSq += val * val;
            }
        }

        double mean = sum / n;

        return sumSq / n - mean * mean;
    }
}
