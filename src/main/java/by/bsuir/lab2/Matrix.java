// Индивидуальная лабораторная работа 2 по дисциплине МРЗвИС вариант 13
// Выполнена студентом группы 221702 БГУИР Потоцким Даниилом Александровичем
// Реализация матрицы
// Последние изменения: 03.11.2025, версия: 1
//
// Использованные источники:
// Формальные модели обработки информации и параллельные модели решения задач. Практикум: учебно-методическое пособие / В. П. Ивашенко. – Минск: БГУИР, 2020.
//
package by.bsuir.lab2;

import java.util.Arrays;

public class Matrix {
    private final float[] data;
    public final int cols;
    public final int rows;

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new float[rows * cols];
    }

    public void set(int row, int col, float value) {
        data[row * cols + col] = value;
    }

    public float get(int row, int col) {
        return data[row * cols + col];
    }

    public void add(int row, int col, float delta) {
        data[row * cols + col] += delta;
    }

    public void reset() {
        Arrays.fill(data, 0.0f);
    }
}