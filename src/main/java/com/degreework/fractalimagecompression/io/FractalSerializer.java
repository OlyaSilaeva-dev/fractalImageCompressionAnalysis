package com.degreework.fractalimagecompression.io;

import com.degreework.fractalimagecompression.model.BlockTransformation;
import com.degreework.fractalimagecompression.model.Transformation;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;

public class FractalSerializer {
    private static final int MAGIC_NUMBER = 0x46524143;

    public static void saveColorImage(String basePath, Mat img, List<List<BlockTransformation>> channelTransforms) throws IOException {
        String[] suffixes = {"_B", "_G", "_R"};
        for (int c = 0; c < 3; c++) {
            String path = basePath + suffixes[c] + ".fractal";
            FractalSerializer.saveToFile(path, img.width(), img.height(), channelTransforms.get(c));
            System.out.println("Saved channel " + c + " to " + path);
        }
    }

    public List<List<BlockTransformation>> loadColorImage(String basePath) throws IOException {
        String[] suffixes = {"_B", "_G", "_R"};
        List<List<BlockTransformation>> channelTransforms = new ArrayList<>();

        for (int c = 0; c < 3; c++) {
            String path = basePath + suffixes[c] + ".fractal";
            List<BlockTransformation> transforms = FractalSerializer.loadFromFile(path);
            channelTransforms.add(transforms);
            System.out.println("Loaded channel " + c + " from " + path);
        }

        return channelTransforms;
    }

    public static void saveToFile(String path, int imgWidth, int imgHeight, List<BlockTransformation> blocks) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(path)))) {

            dos.writeInt(MAGIC_NUMBER);
            dos.writeInt(imgWidth);
            dos.writeInt(imgHeight);
            dos.writeInt(blocks.size());

            for (BlockTransformation bt : blocks) {
                Rect r = bt.getRect();
                Transformation t = bt.getTransformation();

                dos.writeInt(r.x);
                dos.writeInt(r.y);
                dos.writeInt(r.width);
                dos.writeInt(r.height);

                dos.writeByte(t.getK());
                dos.writeByte(t.getL());
                dos.writeByte(t.getFlip());
                dos.writeByte(t.getAngle());

                double[] contrast = t.getContrast();
                double[] brightness = t.getBrightness();
                int ch = contrast.length;
                dos.writeByte(ch);

                for (int i = 0; i < ch; i++) {
                    long cInt = Math.round(contrast[i] * 1e8);
                    long bInt = Math.round(brightness[i] * 1e6);

                    cInt = Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, cInt));
                    bInt = Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, bInt));

                    dos.writeInt((int) cInt);
                    dos.writeInt((int) bInt);

                }
            }
        }
    }

    public static List<BlockTransformation> loadFromFile(String path) throws IOException {
        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(path));
             DataInputStream dis = new DataInputStream(gzis)) {

            if (dis.readInt() != MAGIC_NUMBER) {
                throw new IOException("Invalid file format!");
            }

            int imgWidth = dis.readInt();
            int imgHeight = dis.readInt();
            int count = dis.readInt();

            List<BlockTransformation> list = new ArrayList<>(count);

            for (int i = 0; i < count; i++) {
                int x = dis.readInt();
                int y = dis.readInt();
                int width = dis.readInt();
                int height = dis.readInt();

                int k = dis.readByte();
                int l = dis.readByte();
                int flip = dis.readByte();
                int angle = dis.readByte();

                int numChannels = dis.readUnsignedByte();

                if (numChannels <= 0 || numChannels > 4) {
                    throw new IOException("Invalid numChannels: " + numChannels + " at block " + i);
                }

                double[] contrast = new double[numChannels];
                double[] brightness = new double[numChannels];

                for (int j = 0; j < numChannels; j++) {
                    int c = dis.readInt();
                    int b = dis.readInt();

                    contrast[j] = c / 1e8;
                    brightness[j] = b / 1e6;
                }

                Rect rect = new Rect(x, y, width, height);
                Transformation trans = new Transformation(k, l, flip, angle, contrast, brightness);
                list.add(new BlockTransformation(rect, trans));
            }
            return list;
        }
    }
}