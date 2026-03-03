package com.degreework.fractalimagecompression.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Transformation {
    private int k;
    private int l;          // Координаты исходного (source) блока
    private int flip;          // Направление (1 или -1)
    private int angle;         // Угол (0, 90, 180, 270)
    private double contrast;   // Контраст (s)
    private double brightness; // Яркость (o)
}
