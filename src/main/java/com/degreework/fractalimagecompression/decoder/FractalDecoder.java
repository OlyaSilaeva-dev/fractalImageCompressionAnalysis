package com.degreework.fractalimagecompression.decoder;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public class FractalDecoder {

    /**
     * Декомпрессия grayscale
     */
    public Mat decompressGrayscale(List<BlockTransformation> transformations,
                                   int sourceSize,
                                   int step,
                                   int iterations,
                                   int width,
                                   int height) {

        Mat currentImg = new Mat(height, width, CvType.CV_64FC1);
        Core.randu(currentImg, 0, 256);

        for (int iter = 0; iter < iterations; iter++) {

            Mat nextImg = Mat.zeros(height, width, CvType.CV_64FC1);
            Mat weight = Mat.zeros(height, width, CvType.CV_64FC1);

            for (BlockTransformation bt : transformations) {

                Rect rangeRect = bt.getRect();
                Transformation t = bt.getTransformation();

                int domainX = t.getK() * step;
                int domainY = t.getL() * step;

                if (domainX < 0 || domainY < 0 ||
                        domainX + sourceSize > width ||
                        domainY + sourceSize > height) {
                    continue;
                }

                Mat source = currentImg.submat(new Rect(domainX, domainY, sourceSize, sourceSize)).clone();

                Mat reduced = new Mat();
                Imgproc.resize(source, reduced, new Size(rangeRect.width, rangeRect.height), 0, 0, Imgproc.INTER_AREA);
                source.release();

                Mat transformed = applyTransformation(reduced, t.getFlip(), t.getAngle());
                reduced.release();

                double contrast = t.getContrast()[0];
                double brightness = t.getBrightness()[0];
                Core.multiply(transformed, Scalar.all(contrast), transformed);
                Core.add(transformed, Scalar.all(brightness), transformed);

                Core.min(transformed, Scalar.all(255.0), transformed);
                Core.max(transformed, Scalar.all(0.0), transformed);

                Mat targetRegion = nextImg.submat(rangeRect);
                Core.add(targetRegion, transformed, targetRegion);

                Mat weightRegion = weight.submat(rangeRect);
                Core.add(weightRegion, Mat.ones(rangeRect.size(), CvType.CV_64FC1), weightRegion);

                targetRegion.release();
                weightRegion.release();
                transformed.release();
            }

            Core.add(weight, Scalar.all(1e-8), weight);
            Core.divide(nextImg, weight, nextImg);

            Core.min(nextImg, Scalar.all(255.0), nextImg);
            Core.max(nextImg, Scalar.all(0.0), nextImg);

            currentImg.release();
            currentImg = nextImg.clone();

            nextImg.release();
            weight.release();
        }

        Mat result = new Mat();
        currentImg.convertTo(result, CvType.CV_8UC1);
        currentImg.release();

        return result;
    }

    /**
     * Декомпрессия RGB - как в Python: принимает 3 списка трансформаций
     */
    public Mat decompressRGB(List<List<BlockTransformation>> transformations,
                             int sourceSize,
                             int step,
                             int iterations,
                             int width,
                             int height) {

        // Декомпрессируем каждый канал отдельно
        Mat channelB = decompressGrayscale(transformations.get(0), sourceSize, step, iterations, width, height);
        Mat channelG = decompressGrayscale(transformations.get(1), sourceSize, step, iterations, width, height);
        Mat channelR = decompressGrayscale(transformations.get(2), sourceSize, step, iterations, width, height);

        // Объединяем в BGR порядок (как в OpenCV)
        List<Mat> channels = new ArrayList<>();
        channels.add(channelB);
        channels.add(channelG);
        channels.add(channelR);

        Mat result = new Mat();
        Core.merge(channels, result);

        channelB.release();
        channelG.release();
        channelR.release();

        return result;
    }
}