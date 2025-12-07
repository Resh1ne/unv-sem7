package by.bsuir.lab3;

import java.util.ArrayList;
import java.util.List;

public class Main {

    record NormalizationStats(double mean, double stdDev, List<Double> data) {}

    private static NormalizationStats zScoreNormalize(List<Double> seq) {
        double sum = 0.0;
        for (double x : seq) sum += x;
        double mean = sum / seq.size();

        double m2 = 0.0;
        for (double x : seq) m2 += (x - mean) * (x - mean);

        double stdDev = (seq.size() > 1) ? Math.sqrt(m2 / (seq.size() - 1)) : 1.0;
        if (stdDev == 0) stdDev = 1.0;

        List<Double> norm = new ArrayList<>();
        for (double x : seq) norm.add((x - mean) / stdDev);

        return new NormalizationStats(mean, stdDev, norm);
    }

    private static List<Double> zScoreDenormalize(List<Double> norm, double mean, double stdDev) {
        List<Double> out = new ArrayList<>();
        for (double x : norm) out.add(x * stdDev + mean);
        return out;
    }

    public static void main(String[] args) {
        // --- Настройки по умолчанию ---
        String sequenceName = "fib";
        int seqLen = 10;
        int predictLen = 10;
        int windowSize = 2;
        int hiddenSize = 15;
        int contextSize = 5;
        int effectorSize = 2;
        double alpha = 0.000008;
        double hiddenAlpha = 0.01;
        double maxError = 1e-5;
        int maxIters = 500000; // Уменьшил дефолт для Java, чтобы быстрее запускалось
        boolean useLogTransform = false;
        boolean zscore = false;
        boolean resetContext = true;

        // --- Парсинг аргументов ---
        for (int i = 0; i < args.length; i++) {
            try {
                switch (args[i]) {
                    case "--seq" -> sequenceName = args[++i];
                    case "--seq-len" -> seqLen = Integer.parseInt(args[++i]);
                    case "--win-len" -> windowSize = Integer.parseInt(args[++i]);
                    case "--con-len" -> contextSize = Integer.parseInt(args[++i]);
                    case "--eff-len" -> effectorSize = Integer.parseInt(args[++i]);
                    case "--hidden" -> hiddenSize = Integer.parseInt(args[++i]);
                    case "--alpha" -> alpha = Double.parseDouble(args[++i]);
                    case "--alpha-hidden" -> hiddenAlpha = Double.parseDouble(args[++i]);
                    case "--iters" -> maxIters = Integer.parseInt(args[++i]);
                    case "--max-error" -> maxError = Double.parseDouble(args[++i]);
                    case "--no-reset-ctx" -> resetContext = false;
                    case "--log" -> useLogTransform = true;
                    case "--zscore" -> zscore = true;
                }
            } catch (Exception e) {
                System.err.println("Error parsing argument: " + args[i]);
                return;
            }
        }

        System.out.println("Starting training on sequence: " + sequenceName);

        // Получение данных
        List<Double> fullSequence = SequenceGenerator.getSequence(sequenceName);
        // Берем подмножество
        if (seqLen > fullSequence.size()) seqLen = fullSequence.size();
        List<Double> inputRaw = new ArrayList<>(fullSequence.subList(0, seqLen));

        // Предобработка
        List<Double> processedInput;
        double inputMean = 0.0;
        double inputStd = 1.0;

        if (useLogTransform) {
            System.out.println("INFO: Using LOG TRANSFORM preprocessing.");
            processedInput = new ArrayList<>();
            for (double val : inputRaw) processedInput.add(Math.log(val + 1.0));
        } else if (zscore) {
            System.out.println("INFO: Using Z-SCORE NORMALIZATION preprocessing.");
            NormalizationStats stats = zScoreNormalize(inputRaw);
            processedInput = stats.data();
            inputMean = stats.mean();
            inputStd = stats.stdDev();
        } else {
            System.out.println("INFO: Using NO normalization.");
            processedInput = inputRaw;
        }

        // Обучение
        long startTime = System.currentTimeMillis();

        ElmanJordanNetwork rnn = new ElmanJordanNetwork(
                processedInput, windowSize, hiddenSize, contextSize, effectorSize,
                alpha, maxError, maxIters, predictLen, resetContext,
                new LinearActivation(), hiddenAlpha, true
        );

        rnn.train();

        long endTime = System.currentTimeMillis();
        System.out.println("Train duration: " + (endTime - startTime) / 1000.0 + " seconds");

        // Предсказание
        List<Double> predictedProcessed = rnn.predict();
        List<Double> predicted;

        if (useLogTransform) {
            predicted = new ArrayList<>();
            for (double val : predictedProcessed) predicted.add(Math.exp(val) - 1.0);
        } else if (zscore) {
            predicted = zScoreDenormalize(predictedProcessed, inputMean, inputStd);
        } else {
            predicted = predictedProcessed;
        }

        // Вывод результатов
        System.out.println("Input size: " + inputRaw.size() + ", predicted size: " + predicted.size());

        int checkLen = Math.min(predictLen, fullSequence.size() - seqLen);
        if (checkLen <= 0) {
            System.out.println("Note: Predicted sequence extends beyond known ground truth data.");
            // Вывод просто предсказаний, если реальных данных больше нет
            for (int i = 0; i < predicted.size(); i++) {
                System.out.printf("%d. Pred: %.6f%n", i, predicted.get(i));
            }
        } else {
            for (int i = 0; i < checkLen; i++) {
                double actual = fullSequence.get(seqLen + i);
                double pred = predicted.get(i);
                double diff = pred - actual;
                String equalMsg = (Math.abs(diff) < 1e-6) ? "(equal)" : "";
                System.out.printf("%d. %.6f -> %.6f diff: %.6f %s%n", i, actual, pred, diff, equalMsg);
            }
        }
    }
}