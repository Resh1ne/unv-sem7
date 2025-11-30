// Индивидуальная лабораторная работа 2 по дисциплине МРЗвИС вариант 13
// Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
// Файл реализации сети Хопфилда
// Последние изменения: 03.11.2025, версия: 1
//
// Использованные источники:
// Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В. П. Ивашенко. – Минск: БГУИР, 2020.
//
package by.bsuir.lab2;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

public class HopfieldNetwork {
    private final Matrix weights;
    private final DoubleUnaryOperator activation;
    private final int size;

        public record RecallResult(float[] state, int iterations) {
    }

    public HopfieldNetwork(int size, DoubleUnaryOperator activation) {
        this.size = size;
        this.activation = activation;
        this.weights = new Matrix(size, size);
    }

    public int trainProjectiveDelta(List<float[]> patterns, float eta, int maxIters, float tolerance) {
        this.weights.reset();
        int n = this.size;

        System.out.println("--- Начало процесса обучения (Delta Rule) ---");

        for (int iter = 0; iter < maxIters; iter++) {
            float maxChange = 0.0f;

            for (float[] pat : patterns) {
                // W * x
                float[] wx = new float[n];
                IntStream.range(0, n).parallel().forEach(i -> {
                    float sum = 0.0f;
                    for (int j = 0; j < n; j++) {
                        sum += weights.get(i, j) * pat[j];
                    }
                    wx[i] = sum;
                });

                // ошибка
                float[] error = new float[n];
                IntStream.range(0, n).parallel().forEach(i -> {
                    error[i] = pat[i] - wx[i];
                });

                // веса
                for (int i = 0; i < n; i++) {
                    float factor = (eta / n) * error[i];
                    for (int j = 0; j < n; j++) {
                        float dw = factor * pat[j];
                        weights.add(i, j, dw);
                        if (Math.abs(dw) > maxChange) maxChange = Math.abs(dw);
                    }
                }
            }

            System.out.printf("Обучение: итерация %d, макс. изменение весов = %.12f%n", iter + 1, maxChange);
            if (maxChange < tolerance) {
                System.out.printf("--- Обучение завершено: сошлось на итерации %d (max_change = %.8f) ---%n", iter + 1, maxChange);
                return iter + 1;
            }
        }

        System.out.println("--- Обучение остановлено: превышен лимит итераций ---");
        return maxIters;
    }

    public RecallResult recall(float[] input, int maxIters, float tolerance) {
        float[] state = Arrays.copyOf(input, input.length);
        float[] prevState = new float[size];

        System.out.println("\n--- Начало процесса релаксации (Recall) ---");

        for (int i = 0; i < maxIters; i++) {
            System.arraycopy(state, 0, prevState, 0, size);

            for (int j = 0; j < size; j++) {
                state[j] = updateNeuron(j, state);
            }

            if (stopRecalling(i, prevState, state, tolerance)) {
                System.out.println("--- Релаксация завершена ---");
                return new RecallResult(state, i + 1);
            }
        }
        System.out.println("--- Релаксация остановлена (превышен лимит итераций) ---");
        return new RecallResult(state, maxIters);
    }

    private float updateNeuron(int i, float[] state) {
        float sum = 0.0f;
        for (int j = 0; j < size; j++) {
            sum += weights.get(i, j) * state[j];
        }
        return (float) activation.applyAsDouble(sum);
    }

    private boolean stopRecalling(int iter, float[] prev, float[] curr, float tolerance) {
        float maxDiff = 0.0f;
        for (int i = 0; i < size; i++) {
            float diff = Math.abs(curr[i] - prev[i]);
            if (diff > maxDiff) maxDiff = diff;
        }
        System.out.printf("Релаксация: итерация %d, изменение (max_diff) = %.12f, макс. порог измен. = %.12f%n",
                iter + 1, maxDiff, tolerance);

        return maxDiff < tolerance;
    }
}