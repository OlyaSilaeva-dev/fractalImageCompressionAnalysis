package com.degreework.fractalimagecompression.encoder;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.model.TransformedBlock;
import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public record FractalEncoder(ImagePartitioner partitioner) {

    private static final double EPSILON = 1e-10;

    /**
     * Для grayscale - возвращает один список трансформаций
     * Для RGB - возвращает список из 3 списков (как в Python)
     */
    public Object compress(Mat img, int sourceSize, int destSize, int step) {
        if (img.channels() == 1) {
            return compressGrayscale(img, sourceSize, destSize, step);
        } else {
            return compressRGB(img, sourceSize, destSize, step);
        }
    }

    /**
     * Сжатие RGB изображения - возвращает List<List<BlockTransformation>>
     * Как в Python: [compress(R), compress(G), compress(B)]
     */
    @SuppressWarnings("unchecked")
    public List<List<BlockTransformation>> compressRGB(Mat img, int sourceSize, int destSize, int step) {
        // Разделяем на каналы (OpenCV дает BGR порядок)
        List<Mat> channels = new ArrayList<>();
        Core.split(img, channels);

        // channels.get(0) = Blue
        // channels.get(1) = Green
        // channels.get(2) = Red

        List<List<BlockTransformation>> result = new ArrayList<>();

        // Сжимаем каждый канал отдельно (как в Python)
        for (int c = 0; c < channels.size(); c++) {
            System.out.println("Compressing channel " + c + " (B=0, G=1, R=2)");
            List<BlockTransformation> channelTransforms = compressGrayscale(channels.get(c), sourceSize, destSize, step);
            result.add(channelTransforms);
        }

        // Освобождаем память
        for (Mat ch : channels) {
            ch.release();
        }

        return result;
    }

    /**
     * Сжатие grayscale изображения
     */
    public List<BlockTransformation> compressGrayscale(Mat img, int sourceSize, int destSize, int step) {
        List<Rect> rangeRects = partitioner.partition(img, destSize, destSize);
        List<TransformedBlock> sourceBlocks = generateSourceBlocks(img, sourceSize, destSize, step);

        return rangeRects.parallelStream()
                .map(rect -> {
                    Mat destBlock = img.submat(rect);
                    Transformation best = findBestMatch(destBlock, sourceBlocks);
                    destBlock.release();
                    return new BlockTransformation(rect, best);
                })
                .toList();
    }

    private Transformation findBestMatch(Mat destBlock, List<TransformedBlock> blocks) {
        double minError = Double.MAX_VALUE;
        Transformation bestMatch = null;

        for (TransformedBlock block : blocks) {
            byte[] data = block.getData();
            double[] contrast = new double[1];
            double[] brightness = new double[1];

            findContrastBrightness(destBlock, data, contrast, brightness);
            double error = calculateError(destBlock, data, contrast[0], brightness[0]);

            if (error < minError) {
                minError = error;
                bestMatch = new Transformation(
                        block.getK(), block.getL(),
                        block.getFlip(), block.getAngle(),
                        contrast, brightness
                );
            }
        }

        return bestMatch != null ? bestMatch : new Transformation(0, 0, 0, 0, new double[]{1}, new double[]{0});
    }

    private void findContrastBrightness(Mat destBlock, byte[] sourceData,
                                        double[] contrastOut, double[] brightnessOut) {
        int n = (int) sourceData.length - 1;

        byte[] destData = new byte[sourceData.length];
        destBlock.get(0, 0, destData);

        double sumS = 0, sumD = 0, sumSS = 0, sumSD = 0;

        for (int i = 0; i < n; i++) {
            double s = sourceData[i] & 0xFF;
            double d = destData[i] & 0xFF;
            sumS += s;
            sumD += d;
            sumSS += s * s;
            sumSD += s * d;
        }

        double denominator = n * sumSS - sumS * sumS;
        if (Math.abs(denominator) > EPSILON) {
            contrastOut[0] = (n * sumSD - sumS * sumD) / denominator;
            brightnessOut[0] = (sumD - contrastOut[0] * sumS) / n;
        } else {
            contrastOut[0] = 0.0;
            brightnessOut[0] = sumD / n;
        }
    }

    private double calculateError(Mat destBlock, byte[] sourceData,
                                  double contrast, double brightness) {
        int n = sourceData.length - 1;
        byte[] destData = new byte[sourceData.length];
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

    private List<TransformedBlock> generateSourceBlocks(Mat img, int domainSize, int rangeSize, int step) {
        List<TransformedBlock> sourceBlocks = new ArrayList<>();

        for (int y = 0; y <= img.rows() - domainSize; y += step) {
            for (int x = 0; x <= img.cols() - domainSize; x += step) {
                Mat domainBlock = img.submat(new Rect(x, y, domainSize, domainSize));
                Mat reducedBlock = new Mat();
                Imgproc.resize(domainBlock, reducedBlock, new Size(rangeSize, rangeSize), 0, 0, Imgproc.INTER_AREA);

                for (int flip : new int[]{-1, 0, 1}) {
                    for (int angle : new int[]{0, 90, 180, 270}) {
                        Mat transformed = applyTransformation(reducedBlock, flip, angle);
                        int n = (int) transformed.total();
                        int ch = transformed.channels();
                        byte[] data = new byte[n * ch];
                        transformed.get(0, 0, data);
                        sourceBlocks.add(new TransformedBlock(data, transformed.width(), transformed.height(), ch, x / step, y / step, flip, angle));
                        transformed.release();
                    }
                }
                reducedBlock.release();
                domainBlock.release();
            }
        }
        return sourceBlocks;
    }
}