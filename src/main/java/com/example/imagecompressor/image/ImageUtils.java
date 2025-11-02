//Лабораторная работа 1, вариант 13
//Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
//Файл преобразования входного изображения в тип данных принимаемый линейной рециркуляционной сетью
//Использованные источники:
//Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
package com.example.imagecompressor.image;

import com.example.imagecompressor.network.Matrix;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.List;

public class ImageUtils {
    private static final double[] VALUE_RANGE = {-1.0, 1.0};

    public record Padding(int width, int height) {}
    public record ImageBlocks(Matrix blocks, int originalWidth, int originalHeight, int paddedWidth, int paddedHeight) {}

    public static ImageBlocks splitIntoBlocks(BufferedImage image, int blockWidth, int blockHeight) {
        // 1. Конвертация в 3-компонентный RGB, если необходимо
        if (image.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            BufferedImage convertedImg = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
            convertedImg.getGraphics().drawImage(image, 0, 0, null);
            image = convertedImg;
        }

        // 2. Расчет паддинга
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        Padding padding = computePadding(originalWidth, originalHeight, blockWidth, blockHeight);
        int paddedWidth = originalWidth + padding.width();
        int paddedHeight = originalHeight + padding.height();

        // 3. Создание нового изображения с паддингом
        BufferedImage paddedImage = new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_3BYTE_BGR);
        paddedImage.getGraphics().drawImage(image, 0, 0, null);
        // Здесь используется простейшее копирование края, для простоты. Отражение (reflect) сложнее.

        // 4. Разделение на блоки и нормализация
        List<double[]> blocksList = new ArrayList<>();
        byte[] pixels = ((DataBufferByte) paddedImage.getRaster().getDataBuffer()).getData();
        int channels = 3; // B, G, R

        for (int y = 0; y < paddedHeight; y += blockHeight) {
            for (int x = 0; x < paddedWidth; x += blockWidth) {
                double[] block = new double[blockWidth * blockHeight * channels];
                int blockIndex = 0;
                for (int by = 0; by < blockHeight; by++) {
                    for (int bx = 0; bx < blockWidth; bx++) {
                        int currentX = x + bx;
                        int currentY = y + by;
                        int pixelIndex = (currentY * paddedWidth + currentX) * channels;
                        block[blockIndex++] = normalize(Byte.toUnsignedInt(pixels[pixelIndex + 2]));
                        block[blockIndex++] = normalize(Byte.toUnsignedInt(pixels[pixelIndex + 1]));
                        block[blockIndex++] = normalize(Byte.toUnsignedInt(pixels[pixelIndex]));
                    }
                }
                blocksList.add(block);
            }
        }

        double[][] blocksArray = blocksList.toArray(new double[0][]);
        return new ImageBlocks(new Matrix(blocksArray), originalWidth, originalHeight, paddedWidth, paddedHeight);
    }

    public static BufferedImage reconstructFromBlocks(Matrix blocks, int originalWidth, int originalHeight, int blockWidth, int blockHeight, int paddedWidth, int paddedHeight) {
        BufferedImage resultImage = new BufferedImage(paddedWidth, paddedHeight, BufferedImage.TYPE_3BYTE_BGR);
        byte[] pixels = ((DataBufferByte) resultImage.getRaster().getDataBuffer()).getData();
        int channels = 3;
        int blocksPerRow = paddedWidth / blockWidth;

        for (int i = 0; i < blocks.rows; i++) {
            int row = i / blocksPerRow;
            int col = i % blocksPerRow;
            int startY = row * blockHeight;
            int startX = col * blockWidth;

            double[] blockData = blocks.data[i];
            int blockIndex = 0;

            for (int by = 0; by < blockHeight; by++) {
                for (int bx = 0; bx < blockWidth; bx++) {
                    int currentX = startX + bx;
                    int currentY = startY + by;
                    int pixelIndex = (currentY * paddedWidth + currentX) * channels;

                    pixels[pixelIndex + 2] = (byte) denormalize(blockData[blockIndex++]); // R
                    pixels[pixelIndex + 1] = (byte) denormalize(blockData[blockIndex++]); // G
                    pixels[pixelIndex] = (byte) denormalize(blockData[blockIndex++]);     // B
                }
            }
        }

        return resultImage.getSubimage(0, 0, originalWidth, originalHeight);
    }

    private static Padding computePadding(int width, int height, int blockW, int blockH) {
        int padW = (blockW - (width % blockW)) % blockW;
        int padH = (blockH - (height % blockH)) % blockH;
        return new Padding(padW, padH);
    }

    private static double normalize(int pixelValue) {
        double min = VALUE_RANGE[0];
        double max = VALUE_RANGE[1];
        return (pixelValue / 255.0) * (max - min) + min;
    }

    private static int denormalize(double normalizedValue) {
        double min = VALUE_RANGE[0];
        double max = VALUE_RANGE[1];
        double value = (normalizedValue - min) / (max - min) * 255.0;
        return Math.max(0, Math.min(255, (int) Math.round(value)));
    }
}