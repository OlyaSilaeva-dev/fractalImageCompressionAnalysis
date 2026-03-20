package com.degreework.fractalimagecompression.encoder;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.model.TransformedBlock;
import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public class FractalEncoder {
    private final ImagePartitioner partitioner;

    private static final double EPSILON = 1e-10;

    public FractalEncoder(ImagePartitioner partitioner) {
        this.partitioner = partitioner;
    }

    /**
     * Реализация классического алгоритма Барнсли-Джаквина для фрактального сжатия изображения
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
    public List<BlockTransformation> compress(Mat img, int sourceSize, int destSize, int step) {
        List<Rect> rangeRects = partitioner.partition(img, destSize, destSize);

        List<BlockTransformation> transformations =
                Collections.synchronizedList(new ArrayList<>());

        List<TransformedBlock> sourceBlocks =
                generateSourceBlocks(img, sourceSize, destSize, step);

        rangeRects.parallelStream().forEach(rect -> {

            Mat destBlock = img.submat(rect);

            Transformation best = findBestMatch(destBlock, sourceBlocks);

            transformations.add(new BlockTransformation(rect, best));

            destBlock.release();

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

        byte[] sourceData = new byte[n];
        byte[] destData = new byte[n];

        sourceBlock.get(0, 0, sourceData);
        destBlock.get(0, 0, destData);

        double sumS = 0;
        double sumD = 0;
        double sumSS = 0;
        double sumSD = 0;

        for (int i = 0; i < n; i++) {

            double s = sourceData[i] & 0xFF;
            double d = destData[i] & 0xFF;

            sumS += s;
            sumD += d;
            sumSS += s * s;
            sumSD += s * d;
        }

        double denominator = (n * sumSS - sumS * sumS);

        double contrast = 0.0;
        double brightness = sumD / n;

        if (Math.abs(denominator) > EPSILON) {
            contrast = (n * sumSD - sumS * sumD) / denominator;
            brightness = (sumD - contrast * sumS) / n;
        }

        if (contrast > 1.0) contrast = 1.0;
        if (contrast < -1.0) contrast = -1.0;

        return new double[]{contrast, brightness};
    }

    private double calculateError(Mat destBlock, Mat sourceBlock, double contrast, double brightness) {
        int n = (int) destBlock.total();

        byte[] sourceData = new byte[n];
        byte[] destData = new byte[n];

        sourceBlock.get(0, 0, sourceData);
        destBlock.get(0, 0, destData);

        double sum = 0;

        for (int i = 0; i < n; i++) {

            double s = sourceData[i] & 0xFF;
            double d = destData[i] & 0xFF;

            double diff = d - (contrast * s + brightness);

            sum += diff * diff;
        }

        return sum;
    }

    /**
     * Генерация всех возможных доменных блоков с их трансформациями
     * @param img Исходное изображение
     * @param domainSize Размер доменного блока (должен быть в 2 раза больше рангового)
     * @param rangeSize Размер рангового блока
     * @param step Шаг сканирования доменных блоков
     * @return Список трансформированных доменных блоков
     */
    private List<TransformedBlock> generateSourceBlocks(Mat img, int domainSize, int rangeSize, int step) {
        List<TransformedBlock> sourceBlocks = new ArrayList<>();

        for (int y = 0; y <= img.rows() - domainSize; y += step) {
            for (int x = 0; x <= img.cols() - domainSize; x += step) {
                Mat domainBlock = img.submat(new Rect(x, y, domainSize, domainSize));

                Mat reducedBlock = new Mat();
                Imgproc.resize(domainBlock, reducedBlock, new Size(rangeSize, rangeSize), 0, 0, Imgproc.INTER_AREA);

                int[] flipValues = {-1, 0, 1};
                int[] angleValues = {0, 90, 180, 270};

                for (int flip : flipValues) {
                    for (int angle : angleValues) {
                        Mat transformed = applyTransformation(reducedBlock, flip, angle);

                        sourceBlocks.add(new TransformedBlock(
                                transformed,
                                x / step,
                                y / step,
                                flip,
                                angle
                        ));
                    }
                }

                reducedBlock.release();
                domainBlock.release();
            }
        }

        return sourceBlocks;
    }

}