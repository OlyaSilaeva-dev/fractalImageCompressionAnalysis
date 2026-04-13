package com.degreework.fractalimagecompression.visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class ImageViewer2 {
    private static JFrame frame;
    private static ComparisonPanel comparisonPanel;

    private static final int WIDTH = 1400;
    private static final int HEIGHT = 800;
    private static final double ZOOM_STEP = 0.25;
    private static final double MAX_ZOOM = 8.0;
    private static final double MIN_ZOOM = 0.5;

    private ImageViewer2() {}

    public static void showComparison(BufferedImage fractalImage, BufferedImage jpegImage,
                                      String fractalTitle, String jpegTitle) {

        if (frame == null) {
            frame = new JFrame("Сравнение сжатия: Фрактальное vs JPEG");
            frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            comparisonPanel = new ComparisonPanel(fractalImage, jpegImage, fractalTitle, jpegTitle);

            JScrollPane scrollPane = new JScrollPane(comparisonPanel);
            scrollPane.getVerticalScrollBar().setUnitIncrement(20);
            scrollPane.getHorizontalScrollBar().setUnitIncrement(20);

            frame.add(scrollPane);
            frame.setSize(WIDTH, HEIGHT);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        } else {
            comparisonPanel.updateImages(fractalImage, jpegImage, fractalTitle, jpegTitle);
        }
    }

    private static class ComparisonPanel extends JPanel {
        private BufferedImage fractalImage;
        private BufferedImage jpegImage;
        private String fractalTitle;
        private String jpegTitle;

        private double zoom = 1.0;
        private Point dragStart = null;
        private Point viewPosition = new Point(0, 0);

        public ComparisonPanel(BufferedImage fractalImage, BufferedImage jpegImage,
                               String fractalTitle, String jpegTitle) {
            this.fractalImage = fractalImage;
            this.jpegImage = jpegImage;
            this.fractalTitle = fractalTitle;
            this.jpegTitle = jpegTitle;

            setBackground(Color.DARK_GRAY);
            setPreferredSize(getInitialSize());

            setupMouseListeners();
            setupMouseWheelListener();
        }

        private Dimension getInitialSize() {
            int width = (Math.max(fractalImage.getWidth(), jpegImage.getWidth()) + 100) * 2;
            int height = Math.max(fractalImage.getHeight(), jpegImage.getHeight()) + 150;
            return new Dimension(width, height);
        }

        private void setupMouseListeners() {
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    dragStart = e.getPoint();
                    setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    dragStart = null;
                    setCursor(Cursor.getDefaultCursor());
                }
            });

            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (dragStart != null && zoom > 1.0) {
                        Point current = e.getPoint();
                        int dx = current.x - dragStart.x;
                        int dy = current.y - dragStart.y;

                        viewPosition.x = Math.max(0, Math.min(viewPosition.x - dx,
                                getZoomedWidth() - getVisibleWidth()));
                        viewPosition.y = Math.max(0, Math.min(viewPosition.y - dy,
                                getZoomedHeight() - getVisibleHeight()));

                        dragStart = current;
                        repaint();
                    }
                }
            });
        }

        private void setupMouseWheelListener() {
            addMouseWheelListener(e -> {
                double oldZoom = zoom;
                if (e.getWheelRotation() < 0) {
                    zoom = Math.min(MAX_ZOOM, zoom + ZOOM_STEP);
                } else {
                    zoom = Math.max(MIN_ZOOM, zoom - ZOOM_STEP);
                }

                if (zoom != oldZoom) {
                    adjustViewportAfterZoom(e.getPoint(), oldZoom);
                    repaint();
                }
            });
        }

        private void adjustViewportAfterZoom(Point mousePos, double oldZoom) {
            double ratio = zoom / oldZoom;
            viewPosition.x = (int) (mousePos.x + (viewPosition.x - mousePos.x) * ratio);
            viewPosition.y = (int) (mousePos.y + (viewPosition.y - mousePos.y) * ratio);

            viewPosition.x = Math.max(0, Math.min(viewPosition.x, getZoomedWidth() - getVisibleWidth()));
            viewPosition.y = Math.max(0, Math.min(viewPosition.y, getZoomedHeight() - getVisibleHeight()));
        }

        private int getVisibleWidth() {
            return getParent() instanceof JViewport ?
                    ((JViewport) getParent()).getWidth() : getWidth();
        }

        private int getVisibleHeight() {
            return getParent() instanceof JViewport ?
                    ((JViewport) getParent()).getHeight() : getHeight();
        }

        private int getZoomedWidth() {
            return (int) ((Math.max(fractalImage.getWidth(), jpegImage.getWidth()) + 100) * 2 * zoom);
        }

        private int getZoomedHeight() {
            return (int) ((Math.max(fractalImage.getHeight(), jpegImage.getHeight()) + 150) * zoom);
        }

        public void updateImages(BufferedImage fractalImage, BufferedImage jpegImage,
                                 String fractalTitle, String jpegTitle) {
            this.fractalImage = fractalImage;
            this.jpegImage = jpegImage;
            this.fractalTitle = fractalTitle;
            this.jpegTitle = jpegTitle;
            zoom = 1.0;
            viewPosition.setLocation(0, 0);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g.create();

            // Включаем сглаживание для текста
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            int centerX = getWidth() / 2;
            int separatorX = centerX;

            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawLine(separatorX, 0, separatorX, getHeight());

            drawImageWithZoom(g2d, fractalImage, fractalTitle,
                    new Rectangle(10, 10, separatorX - 20, getHeight() - 20));

            drawImageWithZoom(g2d, jpegImage, jpegTitle,
                    new Rectangle(separatorX + 10, 10, getWidth() - separatorX - 20, getHeight() - 20));

            drawZoomInfo(g2d);

            g2d.dispose();
        }

        private void drawImageWithZoom(Graphics2D g2d, BufferedImage image, String title,
                                       Rectangle area) {
            Shape oldClip = g2d.getClip();
            g2d.setClip(area);

            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(area.x, area.y, area.width, area.height);

            int imgWidth = (int) (image.getWidth() * zoom);
            int imgHeight = (int) (image.getHeight() * zoom);

            int x = area.x + (area.width - imgWidth) / 2;
            int y = area.y + (area.height - imgHeight) / 2;

            if (zoom > 1.0) {
                x -= viewPosition.x;
                y -= viewPosition.y;
            }

            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2d.drawImage(image, x, y, imgWidth, imgHeight, null);

            g2d.setColor(Color.YELLOW);
            g2d.setStroke(new BasicStroke(2));
            g2d.drawRect(x, y, imgWidth, imgHeight);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 14));
            String displayTitle = title + (zoom > 1.0 ? String.format(" (Zoom: %.1fx)", zoom) : "");
            int titleX = area.x + (area.width - g2d.getFontMetrics().stringWidth(displayTitle)) / 2;
            g2d.drawString(displayTitle, titleX, area.y + 25);

            g2d.setClip(oldClip);
        }

        private void drawZoomInfo(Graphics2D g2d) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.PLAIN, 12));
            g2d.setBackground(new Color(0, 0, 0, 150));

            String zoomInfo = String.format("Zoom: %.1fx | Прокрутка колесика для зума | Перетаскивание для панорамирования", zoom);
            int infoX = 10;
            int infoY = getHeight() - 10;

            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(zoomInfo);
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(infoX - 5, infoY - fm.getHeight() - 5, textWidth + 10, fm.getHeight() + 10);

            g2d.setColor(Color.WHITE);
            g2d.drawString(zoomInfo, infoX, infoY - 5);
        }
    }
}