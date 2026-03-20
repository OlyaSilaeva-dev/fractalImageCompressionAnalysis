package com.degreework.fractalimagecompression.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

public class Utils {

    private Utils() {}

    /**
     * Применяет трансформацию к блоку (отражение и поворот)
     */
    public static Mat applyTransformation(Mat block, int flip, int angle) {
        Mat result = block.clone();

        if (flip != 0) {
            Mat flipped = new Mat();
            Core.flip(result, flipped, flip);
            result = flipped;
        }

        if (angle != 0) {
            Mat rotated = new Mat();
            Point center = new Point(result.cols() / 2.0, result.rows() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Imgproc.warpAffine(result, rotated, rotationMatrix, result.size());
            result = rotated;
        }

        return result;
    }
}
