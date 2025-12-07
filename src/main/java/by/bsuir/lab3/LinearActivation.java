package by.bsuir.lab3;

/**
 * Линейная функция активации: f(x) = x.
 */
public class LinearActivation implements ActivationFunction {
    @Override
    public double activate(double x) {
        return x;
    }

    @Override
    public double derivative(double x) {
        return 1.0;
    }
}