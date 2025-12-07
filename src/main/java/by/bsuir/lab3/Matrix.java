package by.bsuir.lab3;

import java.util.Random;

/**
 * Класс для работы с матрицами.
 * Заменяет библиотеку Eigen.
 */
public class Matrix {
    private final double[][] data;
    public final int rows;
    public final int cols;
    private static final Random random = new Random();

    public Matrix(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.data = new double[rows][cols];
    }

    public static Matrix random(int rows, int cols) {
        Matrix m = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                m.data[i][j] = random.nextDouble() * 2.0 - 1.0; // от -1.0 до 1.0
            }
        }
        return m;
    }

    public static Matrix zero(int rows, int cols) {
        return new Matrix(rows, cols);
    }

    public double get(int r, int c) {
        return data[r][c];
    }

    public void set(int r, int c, double val) {
        data[r][c] = val;
    }

    public Matrix multiply(Matrix other) {
        if (this.cols != other.rows) {
            throw new IllegalArgumentException("Dimension mismatch: " + this.cols + " vs " + other.rows);
        }
        Matrix res = new Matrix(this.rows, other.cols);
        for (int i = 0; i < this.rows; i++) {
            for (int k = 0; k < this.cols; k++) {
                double val = this.data[i][k];
                if (val == 0) continue; // Оптимизация разреженности
                for (int j = 0; j < other.cols; j++) {
                    res.data[i][j] += val * other.data[k][j];
                }
            }
        }
        return res;
    }

    public Matrix add(Matrix other) {
        Matrix res = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                res.data[i][j] = this.data[i][j] + other.data[i][j];
        return res;
    }

    public Matrix subtract(Matrix other) {
        Matrix res = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                res.data[i][j] = this.data[i][j] - other.data[i][j];
        return res;
    }

    public Matrix multiplyScalar(double scalar) {
        Matrix res = new Matrix(rows, cols);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                res.data[i][j] = this.data[i][j] * scalar;
        return res;
    }

    public Matrix transpose() {
        Matrix res = new Matrix(cols, rows);
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                res.data[j][i] = this.data[i][j];
        return res;
    }

    /**
     * Возвращает первые n элементов.
     * Работает как для вектора-строки, так и для вектора-столбца.
     */
    public Matrix head(int n) {
        boolean isRow = (rows == 1);
        Matrix res = isRow ? new Matrix(1, n) : new Matrix(n, 1);
        for (int i = 0; i < n; i++) {
            if (isRow) res.data[0][i] = this.data[0][i];
            else res.data[i][0] = this.data[i][0];
        }
        return res;
    }

    public void setZero() {
        for (int i = 0; i < rows; i++)
            for (int j = 0; j < cols; j++)
                data[i][j] = 0.0;
    }

    // Создание вектора из массива
    public static Matrix fromVector(double[] vec, boolean isRow) {
        Matrix m = isRow ? new Matrix(1, vec.length) : new Matrix(vec.length, 1);
        for (int i = 0; i < vec.length; i++) {
            if (isRow) m.data[0][i] = vec[i];
            else m.data[i][0] = vec[i];
        }
        return m;
    }
}