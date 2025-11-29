package by.bsuir.lab2;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.IntStream;

public class HopfieldNetwork {
    private final Matrix weights;
    private final DoubleUnaryOperator activation;
    private final int size;

    // Результат работы сети (состояние + кол-во итераций)
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

        for (int iter = 0; iter < maxIters; iter++) {
            float maxChange = 0.0f;

            for (float[] pat : patterns) {
                // 1. Вычисляем W * x параллельно
                float[] wx = new float[n];
                IntStream.range(0, n).parallel().forEach(i -> {
                    float sum = 0.0f;
                    for (int j = 0; j < n; j++) {
                        sum += weights.get(i, j) * pat[j];
                    }
                    wx[i] = sum;
                });

                // 2. Вычисляем ошибку (параллельно)
                float[] error = new float[n];
                IntStream.range(0, n).parallel().forEach(i -> error[i] = pat[i] - wx[i]);

                // 3. Обновляем веса (последовательно для корректного отслеживания maxChange и избежания гонки записи)
                for (int i = 0; i < n; i++) {
                    float factor = (eta / n) * error[i];
                    for (int j = 0; j < n; j++) {
                        float dw = factor * pat[j];
                        weights.add(i, j, dw);
                        if (Math.abs(dw) > maxChange) maxChange = Math.abs(dw);
                    }
                }
            }

            if (maxChange < tolerance) {
                System.out.printf("Converged at iter %d (max_change = %.8f)%n", iter + 1, maxChange);
                return iter + 1;
            }

            if ((iter + 1) % 100 == 0) {
                System.out.printf("Iteration %d: max_change = %.13f%n", iter + 1, maxChange);
            }
        }
        return maxIters;
    }

    public RecallResult recall(float[] input, int maxIters, float tolerance) {
        float[] state = Arrays.copyOf(input, input.length);
        float[] prevState = new float[size];

        for (int i = 0; i < maxIters; i++) {
            System.arraycopy(state, 0, prevState, 0, size);

            for (int j = 0; j < size; j++) {
                state[j] = updateNeuron(j, state);
            }

            if (stopRecalling(prevState, state, tolerance)) {
                return new RecallResult(state, i + 1);
            }
        }
        return new RecallResult(state, maxIters);
    }

    private float updateNeuron(int i, float[] state) {
        float sum = 0.0f;
        for (int j = 0; j < size; j++) {
            sum += weights.get(i, j) * state[j];
        }
        return (float) activation.applyAsDouble(sum);
    }

    private boolean stopRecalling(float[] prev, float[] curr, float tolerance) {
        float maxDiff = 0.0f;
        for (int i = 0; i < size; i++) {
            float diff = Math.abs(curr[i] - prev[i]);
            if (diff > maxDiff) maxDiff = diff;
        }
        return maxDiff < tolerance;
    }
}