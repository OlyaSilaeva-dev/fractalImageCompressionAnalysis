package com.degreework.fractalimagecompression.exceptions;

public class ImageReadingException extends Exception {
    public ImageReadingException(String message) { super(message); }

    public ImageReadingException() { super("Ошибка при чтении изображения"); }
}
