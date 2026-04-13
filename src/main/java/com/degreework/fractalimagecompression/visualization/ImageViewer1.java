package com.degreework.fractalimagecompression.visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class ImageViewer1 {

    private static JFrame frame;
    private static JPanel panel;
    private static JScrollPane scrollPane;

    private static final int WIDTH = 900;
    private static final int HEIGHT = 700;

    private ImageViewer1() {}

    public static void show(BufferedImage image, String title) {
        if (frame == null) {
            frame = new JFrame("Images");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

            panel = new JPanel(new GridLayout(0, 3, 2, 2));
            panel.setPreferredSize(new Dimension(WIDTH, 0));

            scrollPane = new JScrollPane(panel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            frame.add(scrollPane);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        }

        JPanel imagePanel = getJPanel(image, title);
        panel.add(imagePanel);

        updatePanelHeight(image.getHeight());

        panel.revalidate();
        panel.repaint();
    }


    private static void updatePanelHeight(int height) {
        int componentCount = panel.getComponentCount();
        int rows = (int) Math.ceil((double) componentCount / 3);

        int cellHeight = height + 70;

        int totalHeight = rows * cellHeight;
        panel.setPreferredSize(new Dimension(WIDTH, totalHeight));

        if (scrollPane != null) {
            scrollPane.revalidate();
        }
    }

    private static JPanel getJPanel(BufferedImage image, String title) {
        JPanel imagePanel = new JPanel();
        imagePanel.setLayout(new BorderLayout(0, 0));

        String htmlTitle = "<html><div style='text-align: center; padding: 5px; width: 250px;'>"
                + title.replace("\n", "<br>")
                + "</div></html>";

        JLabel titleLabel = new JLabel(htmlTitle);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel label = new JLabel(new ImageIcon(image));

        imagePanel.add(label, BorderLayout.CENTER);
        imagePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        imagePanel.add(titleLabel, BorderLayout.SOUTH);
        return imagePanel;
    }
}
