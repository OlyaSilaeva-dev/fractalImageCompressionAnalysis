package com.degreework.fractalimagecompression.encoder;

import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.model.TransformedBlock;
import org.ejml.simple.SimpleMatrix;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public class FractalEncoder {

    /**
     * @param img        Исходное изображение
     * @param sourceSize Размер доменного блока. Обычно в 2 раза больше,
     *                   чем destSize (например, 16x16 пикселей).
     * @param destSize   Размер рангового блока.
     *                   Это размер фрагментов, на которые разбивается
     *                   итоговое сжатое изображение (например, 8x8 пикселей).
     * @param step       Шаг, с которым мы сканируем изображение для поиска доменных блоков.
     *                   Чем меньше шаг, тем больше вариантов блоков найдет алгоритм и тем выше
     *                   будет качество, но сжатие займет гораздо больше времени.
     * @return «Инструкция по сборке» изображения. Она содержит список правил
     * для каждого рангового блока изображения.
     */
    public List<List<Transformation>> compress(Mat img, int sourceSize, int destSize, int step) {
        int iCount = img.rows() / destSize;
        int jCount = img.cols() / destSize;

        List<List<Transformation>> transformations = Collections.synchronizedList(
                new ArrayList<>(Collections.nCopies(iCount, null))
        );

        List<TransformedBlock> sourceBlocks = generateSourceBlocks(img, sourceSize, destSize, step);

        IntStream.range(0, iCount).parallel().forEach(i -> {
            List<Transformation> row = new ArrayList<>();

            for (int j = 0; j < jCount; j++) {
                Rect rect = new Rect(j * destSize, i * destSize, destSize, destSize);
                Mat destBlock = img.submat(rect);

                row.add(findBestMatch(destBlock, sourceBlocks));
                destBlock.release();
            }

            transformations.set(i, row);
            System.out.println("Processing row: " + i + ", " + jCount);

        });

        return transformations;
    }

    private Transformation findBestMatch(Mat destBlock, List<TransformedBlock> blocks) {
        double minError = Double.MAX_VALUE;
        Transformation bestMatch = null;

        for (TransformedBlock block : blocks) {
            double[] cb = findContrastBrightness(destBlock, block.getData());
            double contrast = cb[0];
            double brightness = cb[1];

            double error = calculateError(destBlock, block.getData(), contrast, brightness);
            if (error < minError) {
                minError = error;
                bestMatch = new Transformation(block.getK(), block.getL(), block.getFlip(),
                        block.getAngle(), contrast, brightness);
            }
        }

        return bestMatch;
    }

    private double[] findContrastBrightness(Mat destBlock, Mat sourceBlock) {
        int n = (int) sourceBlock.total();
        double sumS = 0;
        double sumD = 0;
        double sumSS = 0;
        double sumSD = 0;

        for (int r = 0; r < sourceBlock.rows(); r++) {
            for (int c = 0; c < sourceBlock.cols(); c++) {
                double s = sourceBlock.get(r, c)[0];
                double d = destBlock.get(r, c)[0];

                sumS += s;
                sumD += d;
                sumSS += s * s;
                sumSD += s * d;
            }
        }

        double denominator = (n * sumSS - sumS * sumS);
        double s = 0.0;
        double o = sumD / n;

        if (Math.abs(denominator) > 1e-10) {
            s = (n * sumSD - sumS * sumD) / denominator;
            o = (sumD - s * sumS) / n;
        }

        if (s > 1.0) s = 1.0;
        if (s < -1.0) s = -1.0;

        return new double[]{s, o};
    }

    private double calculateError(Mat destBlock, Mat sourceBlock, double contrast, double brightness) {
        double sum = 0;

        for (int i = 0; i < destBlock.rows(); i++) {
            for (int j = 0; j < destBlock.cols(); j++) {
                double diff = destBlock.get(i, j)[0] - (contrast * sourceBlock.get(i, j)[0] + brightness);
                sum += diff * diff;
            }
        }

        return sum;
    }

    private List<TransformedBlock> generateSourceBlocks(Mat img, int sSize, int dSize, int step) {
        List<TransformedBlock> sourceBlocks = new ArrayList<>();

        for (int k = 0; k <= (img.rows() - sSize) / step; k++) {
            for (int l = 0; l <= (img.cols() - sSize) / step; l++) {
                Rect rect = new Rect(l * step, k * step, sSize, sSize);
                Mat source = img.submat(rect);

                Mat reduced = new Mat();
                Imgproc.resize(source, reduced, new Size(dSize, dSize));

                int[] directions = {1, -1};
                int[] angles = {0, 90, 180, 270};
                for (int direction : directions) {
                    for (int angle : angles) {
                        Mat transformed = applyTransformation(reduced, direction, angle);
                        sourceBlocks.add(new TransformedBlock(transformed, k, l, direction, angle));
                    }
                }

                source.release();
            }
        }

        return sourceBlocks;
    }
}