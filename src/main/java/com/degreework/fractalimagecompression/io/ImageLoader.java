package com.degreework.fractalimagecompression.io;

import com.degreework.fractalimagecompression.App;
import com.degreework.fractalimagecompression.exceptions.ImageReadingException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class ImageLoader {

    static Logger logger = Logger.getLogger(App.class.getName());

    public static BufferedImage loadImage(String imagePath) throws Exception {
        BufferedImage image;
        try {
            image = ImageIO.read(new File(imagePath));
        } catch (IOException e) {
            logger.warning("Не удалось загрузить изображение!");
            throw new ImageReadingException();
        }

        return image;
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        int width = bi.getWidth();
        int height = bi.getHeight();

        int numComponents = bi.getColorModel().getNumComponents();

        if (numComponents == 1) {
            Mat mat = new Mat(height, width, CvType.CV_8UC1);

            BufferedImage grayImg = bi;
            if (bi.getType() != BufferedImage.TYPE_BYTE_GRAY) {
                grayImg = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
                grayImg.getGraphics().drawImage(bi, 0, 0, null);
            }

            byte[] data = ((DataBufferByte) grayImg.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        } else {
            Mat mat = new Mat(height, width, CvType.CV_8UC3);

            BufferedImage bgrImg = bi;
            if (bi.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                bgrImg = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                bgrImg.getGraphics().drawImage(bi, 0, 0, null);
            }

            byte[] data = ((DataBufferByte) bgrImg.getRaster().getDataBuffer()).getData();
            mat.put(0, 0, data);
            return mat;
        }
    }
    public static BufferedImage matToBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) type = BufferedImage.TYPE_3BYTE_BGR;
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer);
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}
