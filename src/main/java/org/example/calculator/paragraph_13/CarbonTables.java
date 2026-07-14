package org.example.calculator.paragraph_13;

import org.example.util.Interpolation;

/**
 * Таблицы Руководства, используемые в расчёте усиления композиционными материалами
 * на основе углеродного волокна (Раздел 13):
 * <ul>
 *   <li>таблица 13.4 — коэффициент k_s, учитывающий тип конструкции усиления (ф. 13.2);</li>
 *   <li>таблица 13.5 — коэффициент e_s перераспределения напряжений между арматурой и
 *       композиционным материалом при расчёте на выносливость (ф. 13.21), интерполяция
 *       по отношению площадей A_f/A_s.</li>
 * </ul>
 *
 * <p>Расчётные характеристики холстов и пластин (таблицы 13.1–13.3: R_f, E_f по классам)
 * не зашиты в код: они задаются напрямую в {@link CarbonInput#Rf} и {@link CarbonInput#Ef},
 * так как выбор типа/класса материала выполняется проектировщиком по паспорту материала.
 */
public final class CarbonTables {

    private CarbonTables() {
    }

    /** Тип конструкции усиления (таблица 13.4) — определяет коэффициент k_s в формуле 13.2. */
    public enum ReinforcementType {
        SHEET_BOTTOM_FREE(0.41, "Холст на нижней грани без закрепления"),
        SHEET_BOTTOM_ANCHORED(0.49, "Холст на нижней грани с закреплением вертикальными холстами"),
        U_JACKET_FREE(0.46, "U-образная обойма без закрепления"),
        U_JACKET_ANCHORED(0.49, "U-образная обойма с закреплением вертикальными холстами"),
        PLATE_ANCHORED(0.45, "Пластины с закреплением холстами или U-образной обоймой");

        private final double ks;
        private final String title;

        ReinforcementType(double ks, String title) {
            this.ks = ks;
            this.title = title;
        }

        public double ks() {
            return ks;
        }

        public String title() {
            return title;
        }
    }

    // --- таблица 13.5: e_s в зависимости от отношения A_f/A_s ---
    private static final double[] RATIO = {0.000, 0.062, 0.125, 0.187, 0.297, 0.375};
    private static final double[] ES = {1.000, 0.929, 0.881, 0.849, 0.808, 0.772};

    /**
     * Коэффициент e_s (таблица 13.5) по отношению площадей композиционного материала и
     * стальной арматуры A_f/A_s. Промежуточные значения — линейная интерполяция; за границами
     * таблицы принимается крайнее значение.
     */
    public static double es(double areaRatio) {
        if (areaRatio <= RATIO[0]) return ES[0];
        if (areaRatio >= RATIO[RATIO.length - 1]) return ES[ES.length - 1];
        return Interpolation.linear(RATIO, ES, areaRatio);
    }
}