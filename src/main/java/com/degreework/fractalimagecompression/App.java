package com.degreework.fractalimagecompression;

import com.degreework.fractalimagecompression.decoder.FractalDecoder;
import com.degreework.fractalimagecompression.encoder.FractalEncoder;
import com.degreework.fractalimagecompression.io.FractalSerializer;
import com.degreework.fractalimagecompression.metrics.CompressionRatio;
import com.degreework.fractalimagecompression.metrics.PSNR;
import com.degreework.fractalimagecompression.metrics.SSIM;
import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.othercodecs.JpegCodec;
import com.degreework.fractalimagecompression.othercodecs.PngCodec;
import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import com.degreework.fractalimagecompression.partitioner.impl.QuadTreePartitioner;
import com.degreework.fractalimagecompression.visualization.ImageViewer1;
import com.degreework.fractalimagecompression.io.ImageLoader;
import com.degreework.fractalimagecompression.visualization.ImageViewer2;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.degreework.fractalimagecompression.io.ImageLoader.bufferedImageToMat;
import static com.degreework.fractalimagecompression.io.ImageLoader.matToBufferedImage;

public class App {
    static {
        System.setProperty("OPENCV_LOG_LEVEL", "ERROR");
        System.setProperty("OPENCV_OPENCL_RUNTIME", "");
        nu.pattern.OpenCV.loadShared();
    }

    static Logger logger = Logger.getLogger(App.class.getName());

    private static final int DEST_SIZE = 8;
    private static final int SOURCE_SIZE = DEST_SIZE * 2;
    private static final int STEP = DEST_SIZE;
    private static final PSNR psnr = new PSNR();
    private static final SSIM ssim = new SSIM();
    private static final int QUALITY_PERSENT = 10;
    private static final String path = "src/main/resources/";

    public static void main(String[] args) {
        try {
            String imagePath = path +"Lena.png";
            BufferedImage bufferedImg = ImageLoader.loadImage(imagePath);

            Mat matImg = ImageLoader.bufferedImageToMat(bufferedImg);
//            Imgproc.cvtColor(matImg, matImg, Imgproc.COLOR_BGR2GRAY);
            Imgproc.resize(matImg, matImg, new Size(256, 256));
            ImageViewer1.show(matToBufferedImage(matImg), "Оригинал (Grayscale)");

            ImagePartitioner partitioner = new QuadTreePartitioner(DEST_SIZE, 80);
//     ImagePartitioner partitioner = new GridPartitioner();

            FractalEncoder encoder = new FractalEncoder(partitioner);
            logger.info("Начало кодирования...");

            long startEncode = System.currentTimeMillis();
            List<List<BlockTransformation>> transformations =  (List<List<BlockTransformation>>)encoder.compress(matImg, SOURCE_SIZE, DEST_SIZE, STEP);
//            List<BlockTransformation> transformations = encoder.compressGrayscale(matImg, SOURCE_SIZE, DEST_SIZE, STEP);
            long finishEncode = System.currentTimeMillis();

            logger.log(Level.INFO, "Кодирование завершено.");
            logger.log(Level.INFO, "Время кодирования: {0} s", (finishEncode - startEncode) / 1000.0);

//            FractalSerializer.saveToFile(path + "testFractal.frc", matImg.width(),  matImg.height(), transformations);
            for (int c = 0; c < 3; c++) {
                FractalSerializer.saveToFile("channel_" + c + ".fractal",
                        matImg.width(), matImg.height(), transformations.get(c));
            }

            logger.info("Начало восстановления...");
            printCompressionRatio(matImg);

            long startDecode = System.currentTimeMillis();
            int iterations = 12;
            FractalDecoder decoder = new FractalDecoder();
            Mat reconstructed = decoder.decompressRGB(transformations, SOURCE_SIZE, STEP, iterations, matImg.width(), matImg.width());

//            decompressProcess(iterations, matImg, transformations);
            long finishDecode = System.currentTimeMillis();
            logger.log(Level.INFO, "Время декодирования: {0} s", (finishDecode - startDecode) / 1000.0);

            for (int c = 0; c < 3; c++) {
                transformations.add(FractalSerializer.loadFromFile("channel_" + c + ".fractal"));
            }

            ImageViewer1.show(matToBufferedImage(reconstructed), "Итерация №" + 10 + "\n PSNR = " +
                    psnr.compute(matImg, reconstructed) + "\n SSIM = " + ssim.compute(matImg, reconstructed));

            jpegCompression(matToBufferedImage(matImg), matImg);
//            compareCompressions(transformations, matImg, iterations);
            pngCompression(matToBufferedImage(matImg), path + "testPng.png");
        } catch (Exception e) {
            logger.severe("Ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printCompressionRatio(Mat original) {

        double ratio = CompressionRatio.compute(
                original,
                path + "testFractal.frc"
        );

        System.out.println("Compression Ratio (Фрактальное сжатие): " + ratio);
    }

    private static void decompressProcess(int nbIter, Mat matImg, List<BlockTransformation> transformations) {
        FractalDecoder decoder = new FractalDecoder();

        for (int i = 0; i < nbIter; i++) {
            Mat recovered = decoder.decompressGrayscale(
                    transformations,
                    SOURCE_SIZE,
                    STEP,
                    i,
                    matImg.cols(),
                    matImg.rows()
            );

            ImageViewer1.show(matToBufferedImage(recovered), "Итерация №" + i + "\n PSNR = " +
                    psnr.compute(matImg, recovered) + "\n SSIM = " + ssim.compute(matImg, recovered));
        }
    }

    private static void jpegCompression(BufferedImage img, Mat original) {
        try {
            long startEncode = System.currentTimeMillis();
            Mat transformed = JpegCodec.save(img, QUALITY_PERSENT, path + "testJpeg.jpg");
            long finishEncode = System.currentTimeMillis();

            System.out.println("Время кодирования для JPEG " + (finishEncode - startEncode) + " ms");

            System.out.println("Compression Ratio (jpeg) = " + CompressionRatio.compute(original, path + "testJpeg.jpg"));

            System.out.println("PSNR for JPEG = " + psnr.compute(original, transformed)
                    + "\n SSIM for JPEG = " + ssim.compute(original, transformed));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Ошибка при сохранении файла в формате JPEG: {0}", e.getMessage());
        }
    }

    private static void compareCompressions(List<BlockTransformation> transformations,
                                            Mat originalMat, int iterations) throws Exception {
        FractalDecoder decoder = new FractalDecoder();
        var transf = FractalSerializer.loadFromFile(path + "testFractal.frc");

        Mat fractalResult = decoder.decompressGrayscale(
                transf,
                SOURCE_SIZE,
                STEP,
                iterations,
                originalMat.cols(),
                originalMat.rows()
        );

        String imagePath = "src/main/resources/testJpeg.jpg";
        BufferedImage bufferedImg = ImageLoader.loadImage(imagePath);

        Mat jpegMat = ImageLoader.bufferedImageToMat(bufferedImg);

        ImageViewer2.showComparison(
                matToBufferedImage(fractalResult),
                matToBufferedImage(jpegMat),
                "Фрактальное сжатие " + iterations + " итераций",
                "JPEG сжатие (качество" + QUALITY_PERSENT + ", блочные артефакты)"
        );
    }

    private static void pngCompression(BufferedImage img, String path) {
        try {
            long start = System.currentTimeMillis();
            Mat compressed = PngCodec.save(img, path);
            long finish = System.currentTimeMillis();
            System.out.println("Время кодирования для png: " + (finish - start) + "ms");
            System.out.println("PSNR для PNG (эталон): " + psnr.compute(bufferedImageToMat(img), compressed));
            System.out.println("SSIM для PNG (эталон): " + ssim.compute(bufferedImageToMat(img), compressed));
            System.out.println("Compression Ratio для PNG (эталон): " + PngCodec.calculateCompressionRatio(compressed, "src/main/resources/testPng.png"));
        } catch (Exception e) {
            logger.warning("Ошибка при сохранении в формате png" + e.getMessage());
        }
    }



}