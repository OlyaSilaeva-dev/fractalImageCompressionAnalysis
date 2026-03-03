package com.degreework.fractalimagecompression.utils;

import org.opencv.core.Core;
import org.opencv.core.Mat;

public class Utils {

    private Utils() {}

    public static Mat applyTransformation(Mat src, int direction, int angle) {
        Mat dst = new Mat();

        if (direction == -1) {
            Core.flip(src, dst, 0);
        } else {
            dst = src.clone();
        }

        if (angle == 90) {
            Core.rotate(dst, dst, Core.ROTATE_90_CLOCKWISE);
        } else if (angle == 180) {
            Core.rotate(dst, dst, Core.ROTATE_180);
        } else if (angle == 270) {
            Core.rotate(dst, dst, Core.ROTATE_90_COUNTERCLOCKWISE);
        }

        return dst;
    }
}
