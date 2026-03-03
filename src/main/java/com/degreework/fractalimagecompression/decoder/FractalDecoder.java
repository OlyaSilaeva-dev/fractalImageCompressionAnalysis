package com.degreework.fractalimagecompression.decoder;

import com.degreework.fractalimagecompression.model.Transformation;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import static com.degreework.fractalimagecompression.utils.Utils.applyTransformation;

public class FractalDecoder {

    /**
     * @param transformations Сжатый образ изображения. Он содержит список правил
     *                        для каждого рангового блока изображения.
     * @param sourceSize      Размер доменного (исходного) блока
     * @param destSize        Размер рангового (целевого) блока.
     * @param step            Шаг поиска доменных блоков.
     * @param iterations      Количество итераций процесса восстановления.
     * @return Итоговое восстановленное изображение
     */
    public Mat decompress(List<List<Transformation>> transformations, int sourceSize, int destSize, int step, int iterations) {
        int height = transformations.size() * destSize;
        int width = transformations.get(0).size() * destSize;

        Mat currentImg = new Mat(height, width, CvType.CV_8UC1);
        Core.randu(currentImg, 0, 256);

        for (int iter = 0; iter < iterations; iter++) {
            Mat nextImg = Mat.zeros(height, width, CvType.CV_8UC1);
            for (int i = 0; i < transformations.size(); i++) {
                for (int j = 0; j < transformations.get(i).size(); j++) {
                    Transformation t = transformations.get(i).get(j);

                    Mat source = new Mat(currentImg, new Rect(t.getL() * step, t.getK() * step, sourceSize, sourceSize));
                    Mat reduced = new Mat();
                    Imgproc.resize(source, reduced, new Size(destSize, destSize));
                    Mat transformed = applyTransformation(reduced, t.getFlip(), t.getAngle());
                    transformed.convertTo(transformed, -1, t.getContrast(), t.getBrightness());
                    transformed.copyTo(nextImg.submat(new Rect(j * destSize, i * destSize, destSize, destSize)));
                }
            }
            currentImg = nextImg;
        }
        return currentImg;
    }

}
