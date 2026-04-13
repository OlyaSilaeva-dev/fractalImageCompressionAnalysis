package com.degreework.fractalimagecompression.metrics;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import org.opencv.core.Mat;

import java.io.File;
import java.util.List;

public class CompressionRatio {

   public static double compute(Mat original, String fractalFilePath) {
        double originalSize = original.total();
        double compressedSize = new File(fractalFilePath).length();

       if (compressedSize == 0) {
           return 0;
       }

        return originalSize / compressedSize;
    }
}
