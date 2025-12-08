// Индивидуальная лабораторная работа 3 по дисциплине МРЗвИС вариант 13
// Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
// Файл реализации класса сети Джордана-Элмана
// Последние изменения: 25.11.2025, версия: 1
//
// Использованные источники:
// Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
//
package by.bsuir.lab3;

import java.util.ArrayList;
import java.util.List;

public class ElmanJordanNetwork {

    // Параметры сети
    private final int inputSize;
    private final int hiddenSize;
    private final int contextSize;
    private final int effectorSize;

    // Гиперпараметры
    private final double learningRate;
    private final double maxError;
    private final int maxIters;
    private final int predictLen;
    private final boolean resetContext;
    private final boolean verbose;

    // Данные
    private final List<Double> sequence;
    private final List<Double> expected;

    // Матрицы весов
    private Matrix weightInputHidden;    // W (вход -> скрытый)
    private Matrix weightHiddenOutput;   // W_ (скрытый -> выход)
    private Matrix weightContextHidden;  // W_C (контекст -> скрытый)
    private Matrix weightEffectorHidden; // W_O (эффектор -> скрытый)

    // Состояние сети
    private Matrix context;

    private final ActivationFunction effectorActivation;
    private final double hiddenAlpha;

    public ElmanJordanNetwork(List<Double> seq, int inputSize, int hiddenSize,
                              int contextSize, int effectorSize, double alpha,
                              double maxError, int maxIters, int predictLen,
                              boolean resetContext, ActivationFunction effAct,
                              double hiddenAlpha, boolean verbose) {

        this.sequence = seq;
        this.inputSize = inputSize;
        this.hiddenSize = hiddenSize;
        this.contextSize = contextSize;
        this.effectorSize = effectorSize;
        this.learningRate = alpha;
        this.maxError = maxError;
        this.maxIters = maxIters;
        this.predictLen = predictLen;
        this.resetContext = resetContext;
        this.effectorActivation = effAct;
        this.hiddenAlpha = hiddenAlpha;
        this.verbose = verbose;

        if (contextSize < 1 || contextSize > hiddenSize)
            throw new IllegalArgumentException("Context size must be between 1 and hidden size");

        this.weightInputHidden = Matrix.random(inputSize, hiddenSize);
        this.weightHiddenOutput = Matrix.random(hiddenSize, 1);
        this.weightContextHidden = Matrix.random(contextSize, hiddenSize);
        this.weightEffectorHidden = Matrix.random(effectorSize, hiddenSize);

        this.expected = new ArrayList<>(seq.subList(inputSize, seq.size()));
        this.context = Matrix.zero(1, contextSize);
    }

    public int train() {
        int iter = 0;
        double error = maxError + 1.0;
        LeakyReLU hiddenAct = new LeakyReLU(hiddenAlpha);

        while (iter < maxIters && error > maxError) {
            Matrix dW = Matrix.zero(inputSize, hiddenSize);
            Matrix dW_ = Matrix.zero(hiddenSize, 1);
            Matrix dW_C = Matrix.zero(contextSize, hiddenSize);
            Matrix dW_O = Matrix.zero(effectorSize, hiddenSize);

            List<Matrix> inputs = new ArrayList<>();
            List<Matrix> hiddenStates = new ArrayList<>();
            List<Matrix> hInputs = new ArrayList<>();
            List<Double> outputs = new ArrayList<>();

            error = 0;
            if (resetContext) {
                context.setZero();
            }

            Matrix prevOutputs = Matrix.zero(1, effectorSize);

            for (int i = 0; i < expected.size(); i++) {
                double[] inpArr = new double[inputSize];
                for (int j = 0; j < inputSize; j++) inpArr[j] = sequence.get(i + j);
                Matrix input = Matrix.fromVector(inpArr, true);
                inputs.add(input);

                Matrix term1 = input.multiply(weightInputHidden);
                Matrix term2 = context.multiply(weightContextHidden);
                Matrix term3 = prevOutputs.multiply(weightEffectorHidden);

                Matrix hInput = term1.add(term2).add(term3);
                hInputs.add(hInput);

                Matrix currentHidden = Matrix.zero(1, hiddenSize);
                for (int h = 0; h < hiddenSize; h++) {
                    currentHidden.set(0, h, hiddenAct.activate(hInput.get(0, h)));
                }
                hiddenStates.add(currentHidden);

                double outputVal = currentHidden.multiply(weightHiddenOutput).get(0, 0);
                outputs.add(outputVal);

                context = currentHidden.head(contextSize); // head возвращает 1xCtx, так как currentHidden 1xHid

                for (int e = effectorSize - 1; e > 0; e--) {
                    prevOutputs.set(0, e, prevOutputs.get(0, e - 1));
                }
                prevOutputs.set(0, 0, effectorActivation.activate(outputVal));

                double diff = outputVal - expected.get(i);
                error += diff * diff;
            }

            Matrix dHiddenNext = Matrix.zero(1, hiddenSize);

            for (int i = expected.size() - 1; i >= 0; i--) {
                double diff = outputs.get(i) - expected.get(i);

                dW_ = dW_.add(hiddenStates.get(i).transpose().multiplyScalar(diff));

                Matrix dOutputHidden = weightHiddenOutput.transpose().multiplyScalar(diff);
                Matrix totalHiddenError = dOutputHidden.add(dHiddenNext);

                Matrix dHInput = Matrix.zero(1, hiddenSize);
                for (int h = 0; h < hiddenSize; h++) {
                    double derivative = hiddenAct.derivative(hInputs.get(i).get(0, h));
                    dHInput.set(0, h, totalHiddenError.get(0, h) * derivative);
                }

                dW = dW.add(inputs.get(i).transpose().multiply(dHInput));

                Matrix prevContext;
                if (i == 0) prevContext = Matrix.zero(1, contextSize);
                else prevContext = hiddenStates.get(i - 1).head(contextSize);

                dW_C = dW_C.add(prevContext.transpose().multiply(dHInput));

                Matrix prevOutputsForGrad = Matrix.zero(1, effectorSize);
                if (i > 0) {
                    for (int e = 0; e < effectorSize && (i - 1 - e) >= 0; e++) {
                        prevOutputsForGrad.set(0, e, effectorActivation.activate(outputs.get(i - 1 - e)));
                    }
                }
                dW_O = dW_O.add(prevOutputsForGrad.transpose().multiply(dHInput));

                dHiddenNext.setZero();

                Matrix gradWrtContext = dHInput.multiply(weightContextHidden.transpose());

                for (int k = 0; k < contextSize; k++) {
                    dHiddenNext.set(0, k, gradWrtContext.get(0, k));
                }

                if (i > 0) {
                    Matrix gradWrtPrevOutputs = dHInput.multiply(weightEffectorHidden.transpose()); // (1xEff)

                    double gradThroughAct = gradWrtPrevOutputs.get(0, 0) * effectorActivation.derivative(outputs.get(i - 1));

                    dHiddenNext = dHiddenNext.add(weightHiddenOutput.transpose().multiplyScalar(gradThroughAct));
                }
            }

            weightInputHidden = weightInputHidden.subtract(dW.multiplyScalar(learningRate));
            weightHiddenOutput = weightHiddenOutput.subtract(dW_.multiplyScalar(learningRate));
            weightContextHidden = weightContextHidden.subtract(dW_C.multiplyScalar(learningRate));
            weightEffectorHidden = weightEffectorHidden.subtract(dW_O.multiplyScalar(learningRate));

            error /= expected.size();
            iter++;

            if (verbose && (iter % 1000 == 0 || iter == 1)) {
                System.out.printf("Iteration %d, Error: %.6f%n", iter, error);
            }
        }

        System.out.printf("Training finished after %d iterations, final error = %.6f%n", iter, error);
        return iter;
    }

    public List<Double> predict() {
        List<Double> res = new ArrayList<>();
        LeakyReLU hiddenAct = new LeakyReLU(hiddenAlpha);

        context.setZero();
        Matrix prevOutputs = Matrix.zero(1, effectorSize);

        for (int i = 0; i < expected.size(); i++) {
            double[] inpArr = new double[inputSize];
            for (int j = 0; j < inputSize; j++) inpArr[j] = sequence.get(i + j);
            Matrix input = Matrix.fromVector(inpArr, true);

            Matrix hInput = input.multiply(weightInputHidden)
                    .add(context.multiply(weightContextHidden))
                    .add(prevOutputs.multiply(weightEffectorHidden));

            Matrix currentHidden = Matrix.zero(1, hiddenSize);
            for (int h = 0; h < hiddenSize; h++)
                currentHidden.set(0, h, hiddenAct.activate(hInput.get(0, h)));

            double output = currentHidden.multiply(weightHiddenOutput).get(0, 0);

            context = currentHidden.head(contextSize);
            for (int e = effectorSize - 1; e > 0; e--) prevOutputs.set(0, e, prevOutputs.get(0, e - 1));
            prevOutputs.set(0, 0, effectorActivation.activate(output));
        }

        double[] currentInputVec = new double[inputSize];
        for (int i = 0; i < inputSize; i++) {
            currentInputVec[i] = sequence.get(sequence.size() - inputSize + i);
        }
        Matrix input = Matrix.fromVector(currentInputVec, true);

        for (int i = 0; i < predictLen; i++) {
            Matrix hInput = input.multiply(weightInputHidden)
                    .add(context.multiply(weightContextHidden))
                    .add(prevOutputs.multiply(weightEffectorHidden));

            Matrix hidden = Matrix.zero(1, hiddenSize);
            for (int h = 0; h < hiddenSize; h++)
                hidden.set(0, h, hiddenAct.activate(hInput.get(0, h)));

            double output = hidden.multiply(weightHiddenOutput).get(0, 0);
            res.add(output);

            for (int j = 0; j < inputSize - 1; j++) {
                input.set(0, j, input.get(0, j + 1));
            }
            input.set(0, inputSize - 1, output); // Подаем предсказанное значение на вход

            context = hidden.head(contextSize);
            for (int e = effectorSize - 1; e > 0; e--) prevOutputs.set(0, e, prevOutputs.get(0, e - 1));
            prevOutputs.set(0, 0, effectorActivation.activate(output));
        }

        return res;
    }
}