//Лабораторная работа 1, вариант 13
//Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
//Файл начала программы создающий линейную рециркуляционную сеть
//Использованные источники:
//Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
package com.example.imagecompressor;

import com.example.imagecompressor.image.ImageUtils;
import com.example.imagecompressor.network.Matrix;
import com.example.imagecompressor.network.NeuralNetwork;

import javax.imageio.ImageIO;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class ImageCompressor {

    public static void main(String[] args) {
        CliArguments cliArgs = CliArguments.parse(args);

        try {
            // 1. Загрузка и обработка изображения
            System.out.println("Reading input image: " + cliArgs.inputFile.getPath());
            BufferedImage originalImage = ImageIO.read(cliArgs.inputFile);
            ImageUtils.ImageBlocks imageBlocksData = ImageUtils.splitIntoBlocks(
                    originalImage, cliArgs.blockWidth, cliArgs.blockHeight
            );
            Matrix blocks = imageBlocksData.blocks();

            // 2. Расчет параметров сети и коэффициента сжатия
            int inputSize = blocks.cols; // Размер одного блока (например, 8*8*3 = 192)
            int hiddenSize = (cliArgs.hiddenLayerSize != null) ? cliArgs.hiddenLayerSize : inputSize / 2;
            double compressCoef = getCompressCoef(blocks, inputSize, hiddenSize);
            // --- КОНЕЦ ИСПРАВЛЕНИЯ ---

            System.out.printf("Compress coefficient: %.4f%n", compressCoef);
            if (compressCoef <= 1) {
                System.out.println("The compress coefficient isn't greater than 1. Stopping.");
                System.out.println("Try reducing the hidden layer size (-sl) or using a larger image.");
                return;
            }

            // 3. Создание и обучение сети
            NeuralNetwork network = new NeuralNetwork(
                    inputSize,
                    hiddenSize,
                    cliArgs.learningRate,
                    cliArgs.maxError,
                    cliArgs.maxEpochs
            );
            System.out.println("Starting network training...");
            network.train(blocks);

            // 4. Восстановление изображения
            System.out.println("Reconstructing image...");
            Matrix reconstructedBlocks = Matrix.multiply(
                    Matrix.multiply(blocks, network.weights1),
                    network.weights2
            );

            BufferedImage reconstructedImage = ImageUtils.reconstructFromBlocks(
                    reconstructedBlocks,
                    imageBlocksData.originalWidth(),
                    imageBlocksData.originalHeight(),
                    cliArgs.blockWidth,
                    cliArgs.blockHeight,
                    imageBlocksData.paddedWidth(),
                    imageBlocksData.paddedHeight()
            );

            // 5. Сохранение и отображение результата
            cliArgs.outputFile.getParentFile().mkdirs();
            ImageIO.write(reconstructedImage, "bmp", cliArgs.outputFile);
            System.out.println("Output image saved to: " + cliArgs.outputFile.getPath());

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(cliArgs.outputFile);
            }

        } catch (IOException e) {
            System.err.println("Error processing image file: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static double getCompressCoef(Matrix blocks, int inputSize, int hiddenSize) {
        int numBlocks = blocks.rows;
        double originalDataSize = (double) numBlocks * inputSize * 8;
        double compressedDataSize = (double) numBlocks * hiddenSize * 64;
        double decoderModelSize = (double) hiddenSize * inputSize * 64;
        double imageSize = 2 * 32;
        return originalDataSize / (imageSize + compressedDataSize + decoderModelSize);
    }
}