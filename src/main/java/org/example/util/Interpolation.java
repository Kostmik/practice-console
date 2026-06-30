package org.example.util;

public class Interpolation {

    /**
     * Линейная интерполяция
     */
    public static double linear(double[] x, double[] y, double xTarget) {
        if (x.length != y.length || x.length < 2) {
            throw new IllegalArgumentException("Некорректные массивы для интерполяции");
        }

        if (xTarget <= x[0]) {
            return y[0];
        }
        if (xTarget >= x[x.length - 1]) {
            return y[y.length - 1];
        }

        for (int i = 0; i < x.length - 1; i++) {
            if (xTarget >= x[i] && xTarget <= x[i + 1]) {
                double t = (xTarget - x[i]) / (x[i + 1] - x[i]);
                return y[i] + t * (y[i + 1] - y[i]);
            }
        }

        return y[y.length - 1];
    }
}