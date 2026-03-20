package com.degreework.fractalimagecompression.visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer {

    private static JFrame frame;
    private static JPanel panel;

    private ImageViewer() {}

    public static void show(BufferedImage image, String title) {
        if (frame == null) {
            frame = new JFrame("Images");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            panel = new JPanel();
            // На старте создаём сетку с 0 строк и 5 колонок
            panel.setLayout(new GridLayout(0, 3, 10, 10));

            JScrollPane scrollPane = new JScrollPane(panel);
            frame.add(scrollPane);

            frame.setSize(900, 700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout());

        JLabel label = new JLabel(new ImageIcon(image));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);

        imagePanel.add(label, BorderLayout.CENTER);
        imagePanel.add(titleLabel, BorderLayout.SOUTH);

        panel.add(imagePanel);

        panel.revalidate();
        panel.repaint();
    }
}