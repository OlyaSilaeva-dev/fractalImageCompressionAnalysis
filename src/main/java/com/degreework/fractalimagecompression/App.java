package com.degreework.fractalimagecompression;

import com.degreework.fractalimagecompression.decoder.FractalDecoder;
import com.degreework.fractalimagecompression.encoder.FractalEncoder;
import com.degreework.fractalimagecompression.metrics.CompressionRatio;
import com.degreework.fractalimagecompression.metrics.Metric;
import com.degreework.fractalimagecompression.metrics.PSNR;
import com.degreework.fractalimagecompression.metrics.SSIM;
import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import com.degreework.fractalimagecompression.partitioner.impl.GridPartitioner;
import com.degreework.fractalimagecompression.partitioner.impl.QuadTreePartitioner;
import com.degreework.fractalimagecompression.visualization.ImageViewer;
import com.degreework.fractalimagecompression.io.ImageLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.util.List;
import java.util.logging.Logger;

import static com.degreework.fractalimagecompression.io.ImageLoader.matToBufferedImage;

public class App {
  static {
    nu.pattern.OpenCV.loadShared();
  }
  static Logger logger = Logger.getLogger(App.class.getName());

  public static void main(String[] args) {
    try {
      String imagePath = "src/main/resources/image2.png";
      BufferedImage bufferedImg = ImageLoader.loadImage(imagePath);

      Mat matImg = ImageLoader.bufferedImageToMat(bufferedImg);
      Imgproc.cvtColor(matImg, matImg, Imgproc.COLOR_BGR2GRAY);
      Imgproc.resize(matImg, matImg, new Size(128, 128));
      ImageViewer.show(matToBufferedImage(matImg), "Оригинал (Grayscale)");

      ImagePartitioner partitioner = new QuadTreePartitioner(4, 40);
//     ImagePartitioner partitioner = new GridPartitioner();

      FractalEncoder encoder = new FractalEncoder(partitioner);
      logger.info("Начало кодирования...");

      var transformations = encoder.compress(matImg, 8, 4, 8);
      logger.info("Кодирование завершено.");

      FractalDecoder decoder = new FractalDecoder(partitioner);
      int nbIter = 15;

      logger.info("Начало восстановления...");
      printCompressionRatio(matImg, transformations);
      for (int i = 0; i < nbIter; i++) {
        Mat recovered = decoder.decompress(
                transformations,
                8,
                4,
                8,
                i,
                matImg.cols(),
                matImg.rows()
        );
        ImageViewer.show(matToBufferedImage(recovered), "Итерация №" + i);
        printMetrics(matImg, recovered);
      }

    } catch (Exception e) {
      logger.severe("Ошибка: " + e.getMessage());
    }
  }

  private static void printMetrics(Mat original, Mat reconstructed) {
    PSNR psnr = new PSNR();
    SSIM ssim = new SSIM();

    double psnrValue = psnr.compute(original, reconstructed);
    double ssimValue = ssim.compute(original, reconstructed);

    System.out.println("PSNR: " + psnrValue);
    System.out.println("SSIM: " + ssimValue);
  }

  private static void printCompressionRatio(Mat original, List<BlockTransformation> transformations) {

    CompressionRatio cr = new CompressionRatio();

    double ratio = cr.compute(
            original,
            transformations
    );

    System.out.println("Compression Ratio: " + ratio);
  }
}