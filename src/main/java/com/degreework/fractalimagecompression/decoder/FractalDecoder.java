package com.degreework.fractalimagecompression.decoder;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import com.degreework.fractalimagecompression.partitioner.ImagePartitioner;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public class FractalDecoder {

    private final ImagePartitioner partitioner;

    public FractalDecoder(ImagePartitioner partitioner) {
        this.partitioner = partitioner;
    }

    /**
     * @param transformations Сжатый образ изображения. Он содержит список правил
     *                        для каждого рангового блока изображения.
     * @param sourceSize      Размер доменного (исходного) блока
     * @param destSize        Размер рангового (целевого) блока.
     * @param step            Шаг поиска доменных блоков.
     * @param iterations      Количество итераций процесса восстановления.
     * @return Итоговое восстановленное изображение
     */
    public Mat decompress(List<BlockTransformation> transformations,
                          int sourceSize,
                          int destSize,
                          int step,
                          int iterations,
                          int width,
                          int height) {

        Mat currentImg = new Mat(height, width, CvType.CV_32FC1);
        Core.randu(currentImg, 0, 256);

        for (int iter = 0; iter < iterations; iter++) {
            Mat nextImg = Mat.zeros(height, width, CvType.CV_32FC1);
            Mat weight = Mat.zeros(height, width, CvType.CV_32FC1); // для усреднения

            for (BlockTransformation bt : transformations) {
                Rect rangeRect = bt.getRect();
                Transformation t = bt.getTransformation();

                int domainX = t.getK() * step;
                int domainY = t.getL() * step;

                // Проверка границ домена
                if (domainX < 0 || domainY < 0 ||
                        domainX + sourceSize > width ||
                        domainY + sourceSize > height) {
                    continue;
                }

                Rect domainRect = new Rect(domainX, domainY, sourceSize, sourceSize);
                Mat source = currentImg.submat(domainRect);

                // Уменьшаем до размера рангового блока
                Mat reduced = new Mat();
                Imgproc.resize(source, reduced, new Size(rangeRect.width, rangeRect.height), 0, 0, Imgproc.INTER_AREA);

                // Применяем трансформацию
                Mat transformed = applyTransformation(reduced, t.getFlip(), t.getAngle());

                // Применяем контраст и яркость
                Core.multiply(transformed, new Scalar(t.getContrast()), transformed);
                Core.add(transformed, new Scalar(t.getBrightness()), transformed);

                // Накопление в nextImg
                Mat targetRegion = nextImg.submat(rangeRect);
                Core.add(targetRegion, transformed, targetRegion);

                // Увеличиваем вес для этой области
                Mat weightRegion = weight.submat(rangeRect);
                Core.add(weightRegion, Mat.ones(rangeRect.size(), CvType.CV_32FC1), weightRegion);

                // Освобождение
                source.release();
                reduced.release();
                transformed.release();
                targetRegion.release();
                weightRegion.release();
            }

            // Усреднение
            Core.divide(nextImg, weight, nextImg);

            // Нормализация в 0-255
            Mat normalized = new Mat();
            Core.normalize(nextImg, normalized, 0, 255, Core.NORM_MINMAX, CvType.CV_8UC1);

            currentImg.release();
            currentImg = nextImg.clone(); // сохраняем как float для следующей итерации
            nextImg.release();
            weight.release();
        }

        Mat result = new Mat();
        currentImg.convertTo(result, CvType.CV_8UC1);
        return result;
    }
}
