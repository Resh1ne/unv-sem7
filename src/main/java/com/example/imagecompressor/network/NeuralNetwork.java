//Лабораторная работа 1, вариант 13
//Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
//Файл реализации линейной рециркуляционной сети
//Использованные источники:
//Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
package com.example.imagecompressor.network;

public class NeuralNetwork {
    private final double learningRate;
    private final double maxError;
    private final long maxEpochs;

    public Matrix weights1;
    public Matrix weights2;
    private Matrix encoded;

    public NeuralNetwork(int inputSize, int hiddenSize, double learningRate, double maxError, long maxEpochs) {
        this.learningRate = learningRate;
        this.maxError = maxError;
        this.maxEpochs = maxEpochs;
        initializeWeights(inputSize, hiddenSize);
    }

    private void initializeWeights(int inputSize, int hiddenSize) {
        this.weights1 = Matrix.createRandom(inputSize, hiddenSize);
        this.weights2 = Matrix.createRandom(hiddenSize, inputSize);
    }

    public Matrix forwardPass(Matrix input) {
        this.encoded = Matrix.multiply(input, this.weights1);
        return Matrix.multiply(this.encoded, this.weights2);
    }

    public void backwardPass(Matrix input, Matrix output) {
        Matrix error = Matrix.subtract(output, input);

        Matrix w2Update = Matrix.multiply(this.encoded.transpose(), error);
        Matrix w1Update = Matrix.multiply(input.transpose(), Matrix.multiply(error, this.weights2.transpose()));

        this.weights2.subtractInPlace(w2Update, this.learningRate);
        this.weights1.subtractInPlace(w1Update, this.learningRate);
    }

    public void train(Matrix inputs) {
        long currentEpoch = 0;
        double currentError = Double.POSITIVE_INFINITY;

        while (currentError >= this.maxError && currentEpoch < this.maxEpochs) {
            currentEpoch++;

            for (int i = 0; i < inputs.rows; i++) {
                Matrix currentInput = new Matrix(new double[][]{inputs.data[i]});
                Matrix output = forwardPass(currentInput);
                backwardPass(currentInput, output);
            }

            Matrix fullOutput = Matrix.multiply(Matrix.multiply(inputs, this.weights1), this.weights2);
            Matrix errorMatrix = Matrix.subtract(fullOutput, inputs);
            currentError = 0.5 * sumOfSquares(errorMatrix);

            System.out.printf("Epoch %d, error = %.6f%n", currentEpoch, currentError);
        }
    }


    private double sumOfSquares(Matrix m) {
        double sum = 0;
        for (int i = 0; i < m.rows; i++) {
            for (int j = 0; j < m.cols; j++) {
                sum += m.data[i][j] * m.data[i][j];
            }
        }
        return sum;
    }
}