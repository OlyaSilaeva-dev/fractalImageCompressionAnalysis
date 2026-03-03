package com.degreework.fractalimagecompression.visualization;

import javax.swing.*;
import java.awt.image.BufferedImage;

public class ImageViewer {

    private ImageViewer() {}

    public static void show(BufferedImage image, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        JLabel label = new JLabel(new ImageIcon(image));
        frame.add(label);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
