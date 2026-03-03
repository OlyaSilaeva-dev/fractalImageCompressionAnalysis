package com.degreework.fractalimagecompression;

import com.degreework.fractalimagecompression.decoder.FractalDecoder;
import com.degreework.fractalimagecompression.encoder.FractalEncoder;
import com.degreework.fractalimagecompression.visualization.ImageViewer;
import com.degreework.fractalimagecompression.io.ImageLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
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

      FractalEncoder encoder = new FractalEncoder();
      logger.info("Начало кодирования...");

      var transformations = encoder.compress(matImg, 8, 4, 8);
      logger.info("Кодирование завершено.");

      FractalDecoder decoder = new FractalDecoder();
      int nbIter = 5;

      logger.info("Начало восстановления...");
      for (int i = 0; i < nbIter; i++) {
        Mat recovered = decoder.decompress(transformations, 8, 4, 8, i);
        ImageViewer.show(matToBufferedImage(recovered), "Итерация №" + i);
      }

    } catch (Exception e) {
      logger.severe("Ошибка: " + e.getMessage());
    }
  }
}