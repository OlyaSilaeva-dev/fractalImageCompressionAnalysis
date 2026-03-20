package com.degreework.fractalimagecompression.partitioner;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.util.List;

public interface ImagePartitioner {
    /**
     * Разделяет изображение на список прямоугольных областей (Rect).
     * @param img Матрица изображения для анализа размеров.
     * @param blockSize Размер стороны квадрата (ширина и высота блока).
     * @param step      Расстояние между левыми верхними углами соседних блоков.
     * Если step < blockSize, блоки будут перекрываться.
     * Если step == blockSize, блоки будут прилегать друг к другу.
     * @return Список объектов Rect, определяющих координаты и размеры каждого блока.
     */
    List<Rect> partition(Mat img, int blockSize, int step);

    /**
     * Базовый метод нарезки "стык в стык" (без перекрытия).
     * Шаг по умолчанию принимается равным размеру блока.
     */
    default List<Rect> partition(Mat img, int blockSize) {
        return partition(img, blockSize, blockSize);
    }
}
