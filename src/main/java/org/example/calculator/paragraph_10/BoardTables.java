package org.example.calculator.paragraph_10;

import org.example.util.Interpolation;

/**
 * Табличные зависимости Раздела 10 (продольный борт).
 *
 * <p>Функции F(a,z), Ф(a,z) и момент M(q+τ) заданы в Руководстве многочленами первой степени
 * вида (slope·x + intercept), где x — ширина плеча балластной призмы, м. Коэффициенты
 * многочленов зависят от толщины слоя балласта h_br; для промежуточных толщин Руководство
 * предписывает линейную интерполяцию — здесь интерполируются сами коэффициенты slope и
 * intercept, после чего многочлен вычисляется при заданном x.
 *
 * <p>Таблицы 10.2 и 10.4 уже включают в себя коэффициент Δ_br, поэтому при их использовании
 * (примечание 3 к таблице 10.1) отдельный множитель Δ_br принимать не следует.
 */
public final class BoardTables {

    private BoardTables() {
    }

    /** Толщины балласта h_br (м), общие для таблиц 10.2, 10.3, 10.4. */
    private static final double[] HBR = {0.25, 0.35, 0.45, 0.55, 0.65, 0.75, 0.85, 0.95, 1.05};

    // ---------------------------------------------------------------------
    // Таблица 10.2 — функция F(a,z) для расчёта по изгибающему моменту (прямая)
    // ---------------------------------------------------------------------
    private static final double[] F_SLOPE = {2173.7, 771.06, 54.019, 35.091,
            24.501, 24.501, 24.501, 24.501, 24.501};
    private static final double[] F_INTERCEPT = {-166.01, -38.734, 1.9989, 3.4807,
            4.1312, 4.1313, 4.1314, 4.1315, 4.1316};

    // ---------------------------------------------------------------------
    // Таблица 10.4 — функция Ф(a,z) для расчёта по поперечной силе (прямая)
    // ---------------------------------------------------------------------
    private static final double[] PHI_SLOPE = {2173.7, 771.06, 355.81, 191.81,
            114.68, 114.68, 114.68, 114.68, 114.68};
    private static final double[] PHI_INTERCEPT = {-166.01, -38.734, -6.5409, 3.4363,
            6.6141, 6.6141, 6.6141, 6.6141, 6.6141};

    // ---------------------------------------------------------------------
    // Таблица 10.3 — момент M(q+τ) от временной нагрузки: прямая, кривая R=600, R=300
    // ---------------------------------------------------------------------
    private static final double[] MQT_STRAIGHT_SLOPE = {-0.0258, -0.0258, -0.0258, -0.0258,
            -0.0495, -0.0826, -0.1252, -0.1766, -0.2361};
    private static final double[] MQT_STRAIGHT_INTERCEPT = {0.0355, 0.0356, 0.0357, 0.0357,
            0.0715, 0.1249, 0.1984, 0.2938, 0.4125};

    private static final double[] MQT_R600_SLOPE = {-0.0109, -0.0109, -0.0109, -0.0258,
            -0.0495, -0.0827, -0.1252, -0.1767, -0.2362};
    private static final double[] MQT_R600_INTERCEPT = {0.0147, 0.0147, 0.0147, 0.0366,
            0.0723, 0.1262, 0.2004, 0.2967, 0.4166};

    private static final double[] MQT_R300_SLOPE = {-0.0109, -0.0109, -0.0109, -0.0258,
            -0.0495, -0.0827, -0.1253, -0.1767, -0.2362};
    private static final double[] MQT_R300_INTERCEPT = {0.0149, 0.0149, 0.0149, 0.0366,
            0.0731, 0.1275, 0.2024, 0.2996, 0.4207};

    // ---------------------------------------------------------------------
    // Таблица 10.1 — коэффициент Δ_br по a (см) и h_b (см).
    // Используется только при интегральном способе (без таблиц 10.2/10.4).
    // ---------------------------------------------------------------------
    private static final double[] DBR_A = {35, 45, 55, 65, 75};        // расстояние от торца шпалы до борта, см
    private static final double[] DBR_HB = {25, 35, 45, 55, 65, 75};   // толщина балласта под шпалой, см
    private static final double[][] DBR = {
            //  a=35   a=45   a=55   a=65   a=75
            {1.26, 1.36, 1.54, 1.65, 1.69}, // hb = 25
            {0.66, 0.65, 0.69, 0.71, 0.73}, // hb = 35
            {0.57, 0.57, 0.39, 0.40, 0.43}, // hb = 45
            {0.61, 0.61, 0.40, 0.32, 0.34}, // hb = 55
            {0.60, 0.61, 0.39, 0.35, 0.33}, // hb = 65
            {0.64, 0.63, 0.51, 0.37, 0.32}  // hb = 75
    };

    /** Тип участка пути для выбора многочлена момента M(q+τ) из таблицы 10.3. */
    public enum Track {STRAIGHT, CURVE_R600, CURVE_R300}

    private static double poly(double[] slope, double[] intercept, double hbr, double x) {
        double s = Interpolation.linear(HBR, slope, clampHbr(hbr));
        double b = Interpolation.linear(HBR, intercept, clampHbr(hbr));
        return s * x + b;
    }

    private static double clampHbr(double hbr) {
        // При толщине балласта более 1,05 м считать как для 1,05 м (прим. к табл. 10.3).
        if (hbr > HBR[HBR.length - 1]) return HBR[HBR.length - 1];
        return hbr;
    }

    /** Функция F(a,z) по таблице 10.2 (расчёт по моменту). */
    public static double F(double hbr, double x) {
        return poly(F_SLOPE, F_INTERCEPT, hbr, x);
    }

    /** Функция Ф(a,z) по таблице 10.4 (расчёт по поперечной силе). */
    public static double Phi(double hbr, double x) {
        return poly(PHI_SLOPE, PHI_INTERCEPT, hbr, x);
    }

    /** Момент M(q+τ) по таблице 10.3 в зависимости от типа участка (кН·м). */
    public static double momentQTau(Track track, double hbr, double x) {
        switch (track) {
            case CURVE_R600:
                return poly(MQT_R600_SLOPE, MQT_R600_INTERCEPT, hbr, x);
            case CURVE_R300:
                return poly(MQT_R300_SLOPE, MQT_R300_INTERCEPT, hbr, x);
            case STRAIGHT:
            default:
                return poly(MQT_STRAIGHT_SLOPE, MQT_STRAIGHT_INTERCEPT, hbr, x);
        }
    }

    /**
     * Коэффициент Δ_br по таблице 10.1 (билинейная интерполяция).
     *
     * @param aCm  расстояние от торца шпалы до борта, см
     * @param hbCm толщина слоя балласта под шпалой, см
     */
    public static double deltaBr(double aCm, double hbCm) {
        // интерполяция по hb для каждого столбца a, затем по a
        double[] byA = new double[DBR_A.length];
        for (int j = 0; j < DBR_A.length; j++) {
            double[] col = new double[DBR_HB.length];
            for (int i = 0; i < DBR_HB.length; i++) {
                col[i] = DBR[i][j];
            }
            byA[j] = Interpolation.linear(DBR_HB, col, hbCm);
        }
        return Interpolation.linear(DBR_A, byA, aCm);
    }
}