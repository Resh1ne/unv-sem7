//Лабораторная работа 1, вариант 13
//Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
//Файл описывающий матрицу и её поведение
//Использованные источники:
//Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В.П.Ивашенко. – Минск: БГУИР, 2020.
package com.example.imagecompressor.network;

import java.util.Random;
import java.util.concurrent.Executors;

public class Matrix {
    public final double[][] data;
    public final int rows;
    public final int cols;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public Matrix(double[][] data) {
        this.rows = data.length;
        this.cols = data[0].length;
        this.data = data;
    }

    public static Matrix createRandom(int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
        Random rand = new Random();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.data[i][j] = rand.nextDouble() * 2 - 1;
            }
        }
        return m;
    }

    public Matrix transpose() {
        Matrix result = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                result.data[j][i] = data[i][j];
            }
        }
        return result;
    }

    public static Matrix multiply(Matrix a, Matrix b) {
        if (a.cols != b.rows) {
            throw new IllegalArgumentException("Matrix dimensions are not compatible for multiplication.");
        }
        Matrix result = new Matrix(a.rows, b.cols);

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < a.rows; i++) {
                final int row = i;
                executor.submit(() -> {
                    for (int j = 0; j < b.cols; j++) {
                        double sum = 0;
                        for (int k = 0; k < a.cols; k++) {
                            sum += a.data[row][k] * b.data[k][j];
                        }
                        result.data[row][j] = sum;
                    }
                });
            }
        }

        return result;
    }

    public static Matrix subtract(Matrix a, Matrix b) {
        if (a.rows != b.rows || a.cols != b.cols) {
            throw new IllegalArgumentException("Matrices must have the same dimensions for subtraction.");
        }
        Matrix result = new Matrix(a.rows, a.cols);
        for (int i = 0; i < a.rows; i++) {
            for (int j = 0; j < a.cols; j++) {
                result.data[i][j] = a.data[i][j] - b.data[i][j];
            }
        }
        return result;
    }

    public void subtractInPlace(Matrix other, double learningRate) {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                this.data[i][j] -= other.data[i][j] * learningRate;
            }
        }
    }
}