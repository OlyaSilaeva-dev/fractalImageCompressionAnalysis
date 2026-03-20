package com.degreework.fractalimagecompression.metrics;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

public class SSIM implements Metric {

    private static final double C1 = 6.5025;
    private static final double C2 = 58.5225;

    @Override
    public double compute(Mat original, Mat reconstructed) {

        Mat I1 = new Mat();
        Mat I2 = new Mat();

        original.convertTo(I1, CvType.CV_32F);
        reconstructed.convertTo(I2, CvType.CV_32F);

        Mat mu1 = new Mat();
        Mat mu2 = new Mat();

        Imgproc.GaussianBlur(I1, mu1, new Size(11,11), 1.5);
        Imgproc.GaussianBlur(I2, mu2, new Size(11,11), 1.5);

        Mat mu1Sq = new Mat();
        Mat mu2Sq = new Mat();
        Mat mu1mu2 = new Mat();

        Core.multiply(mu1, mu1, mu1Sq);
        Core.multiply(mu2, mu2, mu2Sq);
        Core.multiply(mu1, mu2, mu1mu2);

        Mat sigma1Sq = new Mat();
        Mat sigma2Sq = new Mat();
        Mat sigma12 = new Mat();

        Mat tmp1 = new Mat();
        Mat tmp2 = new Mat();

        Core.multiply(I1, I1, tmp1);
        Core.multiply(I2, I2, tmp2);

        Imgproc.GaussianBlur(tmp1, sigma1Sq, new Size(11,11), 1.5);
        Core.subtract(sigma1Sq, mu1Sq, sigma1Sq);

        Imgproc.GaussianBlur(tmp2, sigma2Sq, new Size(11,11), 1.5);
        Core.subtract(sigma2Sq, mu2Sq, sigma2Sq);

        Core.multiply(I1, I2, tmp1);
        Imgproc.GaussianBlur(tmp1, sigma12, new Size(11,11), 1.5);
        Core.subtract(sigma12, mu1mu2, sigma12);

        Mat t1 = new Mat();
        Mat t2 = new Mat();
        Mat t3 = new Mat();

        Core.multiply(mu1mu2, new Scalar(2), t1);
        Core.add(t1, new Scalar(C1), t1);

        Core.multiply(sigma12, new Scalar(2), t2);
        Core.add(t2, new Scalar(C2), t2);

        Core.multiply(t1, t2, t3);

        Core.add(mu1Sq, mu2Sq, t1);
        Core.add(t1, new Scalar(C1), t1);

        Core.add(sigma1Sq, sigma2Sq, t2);
        Core.add(t2, new Scalar(C2), t2);

        Core.multiply(t1, t2, t1);

        Mat ssimMap = new Mat();
        Core.divide(t3, t1, ssimMap);

        Scalar mssim = Core.mean(ssimMap);

        return mssim.val[0];
    }
}
