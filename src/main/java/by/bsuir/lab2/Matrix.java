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