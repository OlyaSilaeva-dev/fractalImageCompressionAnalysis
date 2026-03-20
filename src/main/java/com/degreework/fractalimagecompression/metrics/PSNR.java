package com.degreework.fractalimagecompression.metrics;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

public class PSNR implements Metric {
    @Override
    public double compute(Mat original, Mat reconstructed) {

        Mat diff = new Mat();
        Core.absdiff(original, reconstructed, diff);
        
        diff.convertTo(diff, CvType.CV_32F);
        Core.multiply(diff, diff, diff);
        
        Scalar sum = Core.sumElems(diff);
        
        double mse = sum.val[0] / (original.total());
        
        if (mse == 0) {
            return Double.POSITIVE_INFINITY;
        }
        
        double maxPixel = 255.0;
        
        return 10 * Math.log10((maxPixel * maxPixel) / mse);
    }
}
