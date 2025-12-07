package by.bsuir.lab3;

/**
 * Функция активации Leaky ReLU.
 */
public class LeakyReLU implements ActivationFunction {
    private final double alpha;

    public LeakyReLU(double alpha) {
        this.alpha = alpha;
    }

    @Override
    public double activate(double x) {
        return x > 0 ? x : alpha * x;
    }

    @Override
    public double derivative(double x) {
        return x > 0 ? 1.0 : alpha;
    }
}