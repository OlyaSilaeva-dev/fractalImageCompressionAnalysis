package com.degreework.fractalimagecompression.othercodecs;

import com.degreework.fractalimagecompression.io.ImageLoader;
import org.opencv.core.Mat;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JpegCodec {
    static Logger logger = Logger.getLogger(JpegCodec.class.getName());

    public static Mat save(BufferedImage img, int qualityPercent, String path) throws Exception {
        ImageWriter writer = ImageIO.getImageWritersByFormatName("jpeg").next();
        ImageWriteParam param = writer.getDefaultWriteParam();

        if (param.canWriteCompressed()) {
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            float quality = Math.min(1.0f, Math.max(0.0f, qualityPercent / 100.0f));
            param.setCompressionQuality(quality);
            logger.log(Level.INFO, "JPEG quality set to: {0}%", qualityPercent);
        }

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(new File(path))) {
            writer.setOutput(ios);
            writer.write(null, new IIOImage(img, null, null), param);
            writer.dispose();
        }

        return ImageLoader.bufferedImageToMat(ImageLoader.loadImage(path));
    }
}