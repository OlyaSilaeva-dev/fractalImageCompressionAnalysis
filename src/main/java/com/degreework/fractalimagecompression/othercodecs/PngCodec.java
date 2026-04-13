package com.degreework.fractalimagecompression.othercodecs;

import com.degreework.fractalimagecompression.io.ImageLoader;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PngCodec {
    private static final Logger logger = Logger.getLogger(PngCodec.class.getName());

    /**
     * Сохраняет изображение в формате PNG.
     * Заметьте: в PNG качество всегда 100%, так как это сжатие без потерь.
     */
    public static Mat save(BufferedImage img, String path) throws Exception {
        File outputFile = new File(path);

        // ImageIO для PNG по умолчанию использует максимальное сжатие без потерь
        boolean result = ImageIO.write(img, "png", outputFile);

        if (!result) {
            throw new Exception("Не удалось найти подходящий ImageWriter для формата PNG");
        }

        logger.log(Level.INFO, "PNG сохранен успешно: {0}", path);

        // Загружаем обратно в Mat для дальнейших расчетов (PSNR, SSIM)
        return ImageLoader.bufferedImageToMat(ImageLoader.loadImage(path));
    }

    /**
     * Рассчитывает коэффициент сжатия.
     * Для PNG он обычно будет в районе 1.5 - 2.5, так как данные не выбрасываются.
     */
    public static double calculateCompressionRatio(Mat original, String path) {
        int channels = original.channels();
        // Размер несжатых данных в памяти
        long uncompressedSize = (long) original.rows() * original.cols() * channels;

        File compressedFile = new File(path);
        long compressedSize = compressedFile.length();

        if (compressedSize == 0) {
            return 0;
        }

        return (double) uncompressedSize / compressedSize;
    }
}