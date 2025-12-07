package by.bsuir.lab3;

import java.util.ArrayList;
import java.util.List;

/**
 * Класс-утилита для генерации числовых последовательностей.
 */
public class SequenceGenerator {
    public static final int SEQ_SIZE = 100;

    public static List<Double> getSequence(String name) {
        double[] arr = switch (name) {
            case "fib" -> fibonacci();
            case "geom" -> geometric();
            case "recip" -> reciprocal();
            case "per1" -> periodic1();
            case "per2" -> periodic2();
            case "nat" -> natural();
            case "sqr" -> squares();
            case "fib-like" -> fibonacciLike();
            case "fact" -> factorial();
            default -> throw new IllegalArgumentException("Unknown sequence: " + name);
        };

        List<Double> list = new ArrayList<>();
        for (double d : arr) list.add(d);
        return list;
    }

    private static double[] fibonacci() {
        double[] seq = new double[SEQ_SIZE];
        if (SEQ_SIZE > 1) seq[1] = 1;
        for (int i = 2; i < SEQ_SIZE; i++) seq[i] = seq[i - 1] + seq[i - 2];
        return seq;
    }

    private static double[] geometric() {
        double[] seq = new double[SEQ_SIZE];
        double val = 1.0;
        for (int i = 0; i < SEQ_SIZE; i++) {
            val /= 2.0;
            seq[i] = val;
        }
        return seq;
    }

    private static double[] periodic1() {
        double[] seq = new double[SEQ_SIZE];
        for (int i = 0; i < SEQ_SIZE; i++) {
            seq[i] = switch (i % 4) {
                case 1 -> -1.0;
                case 3 -> 1.0;
                default -> 0.0;
            };
        }
        return seq;
    }

    private static double[] periodic2() {
        double[] seq = new double[SEQ_SIZE];
        for (int i = 0; i < SEQ_SIZE; i++) seq[i] = (i % 3 == 1) ? 1.0 : 0.0;
        return seq;
    }

    private static double[] reciprocal() {
        double[] seq = new double[SEQ_SIZE];
        for (int i = 0; i < SEQ_SIZE; i++) seq[i] = 1.0 / (i + 2);
        return seq;
    }

    private static double[] natural() {
        double[] seq = new double[SEQ_SIZE];
        for (int i = 0; i < SEQ_SIZE; i++) seq[i] = i + 1;
        return seq;
    }

    private static double[] squares() {
        double[] seq = new double[SEQ_SIZE];
        for (int i = 0; i < SEQ_SIZE; i++) seq[i] = (i + 1) * (i + 1);
        return seq;
    }

    private static double[] fibonacciLike() {
        double[] seq = new double[SEQ_SIZE];
        if (SEQ_SIZE > 0) seq[0] = 1;
        if (SEQ_SIZE > 1) seq[1] = 2;
        for (int i = 2; i < SEQ_SIZE; i++) seq[i] = seq[i - 1] + seq[i - 2];
        return seq;
    }

    private static double[] factorial() {
        double[] seq = new double[SEQ_SIZE];
        if (SEQ_SIZE > 0) seq[0] = 1;
        for (int i = 1; i < SEQ_SIZE; i++) seq[i] = seq[i - 1] * (i + 1);
        return seq;
    }
}