package com.degreework.fractalimagecompression.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.opencv.core.Rect;

@Getter
@AllArgsConstructor
public class BlockTransformation {
    private Rect rect;
    private Transformation transformation;
}
