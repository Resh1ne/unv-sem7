package by.bsuir.lab2.util;

import by.bsuir.lab2.HopfieldNetwork;
import by.bsuir.lab2.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExperimentRunner {
    private static final float ETA = 0.8f;
    private static final float TRAIN_TOL = 1e-6f;
    private static final float RECALL_TOL = 5e-4f;
    private static final int MAX_ITERS = 100_000;

    public static void runAllPlots() throws IOException {
        plotRelaxationVsNoise();
        plotRelaxationVsSize();
        plotTrainingVsCount();
    }

    private static void plotRelaxationVsNoise() throws IOException {
        System.out.println("Построение: Итерации vs Шум...");
        List<float[]> patterns = ImageUtils.loadPatternsFromDir("letters/");
        HopfieldNetwork net = new HopfieldNetwork(patterns.getFirst().length, Math::tanh);
        net.trainProjectiveDelta(patterns, ETA, MAX_ITERS, TRAIN_TOL);

        float[] basePattern = patterns.getFirst(); // Буква 'A'
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        for (int i = 0; i <= 20; i++) {
            double noise = 0.05 + i * 0.02;
            long sumIters = 0;
            int runs = 20;
            for (int r = 0; r < runs; r++) {
                float[] noisy = ImageUtils.addNoise(basePattern, (float) noise, false);
                sumIters += net.recall(noisy, MAX_ITERS, RECALL_TOL).iterations();
            }
            double avg = Math.round((double) sumIters / runs);
            xData.add(noise * 100);
            yData.add(avg);
            System.out.printf(" Шум %.2f -> Средн. итер: %.0f%n", noise, avg);
        }

        GraphUtils.saveTable("table_1.txt", "Уровень шума (%)", "Кол-во итераций", xData, yData);
        GraphUtils.drawGraph("avg_relaxation_vs_noise.png", "Зависимость итераций от шума", "Шум (%)", "Итерации", xData, yData);
    }

    private static void plotRelaxationVsSize() throws IOException {
        System.out.println("\nПостроение: Итерации vs Размер...");
        int[] sizes = {20, 40, 60, 80, 100, 120, 140};
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        BufferedImage original = ImageIO.read(new File("letters/A.png"));

        for (int size : sizes) {
            BufferedImage resized = ImageUtils.resize(original, size, size);
            float[] pat = ImageUtils.imageToPattern(resized);

            HopfieldNetwork net = new HopfieldNetwork(pat.length, Math::tanh);
            net.trainProjectiveDelta(Collections.singletonList(pat), ETA, MAX_ITERS, TRAIN_TOL);

            long sumIters = 0;
            int runs = 20;
            for (int r = 0; r < runs; r++) {
                float[] noisy = ImageUtils.addNoise(pat, 0.35f, false);
                sumIters += net.recall(noisy, MAX_ITERS, RECALL_TOL).iterations();
            }
            double avg = Math.round((double) sumIters / runs);
            xData.add((double) size);
            yData.add(avg);
            System.out.printf(" Размер %dx%d -> Средн. итер: %.0f%n", size, size, avg);
        }

        GraphUtils.saveTable("table_2.txt", "Размер изображения", "Кол-во итераций", xData, yData);
        GraphUtils.drawGraph("avg_relaxation_vs_size.png", "Зависимость итераций от размера", "Размер (px)", "Итерации", xData, yData);
    }

    private static void plotTrainingVsCount() throws IOException {
        System.out.println("\nПостроение: Обучение vs Кол-во образов...");
        List<float[]> allPatterns = ImageUtils.loadPatternsFromDir("letters/");
        List<Double> xData = new ArrayList<>();
        List<Double> yData = new ArrayList<>();

        for (int count = 1; count <= allPatterns.size(); count++) {
            List<float[]> subset = allPatterns.subList(0, count);
            HopfieldNetwork net = new HopfieldNetwork(subset.getFirst().length, Math::tanh);
            int iters = net.trainProjectiveDelta(subset, ETA, MAX_ITERS, TRAIN_TOL);

            xData.add((double) count);
            yData.add((double) iters);
            System.out.printf(" Образов %d -> Итераций обучения: %d%n", count, iters);
        }

        GraphUtils.saveTable("table_3.txt", "Количество образов", "Кол-во итераций", xData, yData);
        GraphUtils.drawGraph("training_vs_patterns.png", "Обучение vs Кол-во образов", "Образы", "Итерации", xData, yData);
    }
}