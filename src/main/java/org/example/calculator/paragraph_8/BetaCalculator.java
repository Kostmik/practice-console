package org.example.calculator.paragraph_8;

import org.example.model.RebarType;

/**
 * Класс для расчета коэффициента β (бета) по формуле 8.3
 *
 * β = (Rₛ / Rₐ) * j
 *
 * где:
 * - Rₛ - расчетное сопротивление арматуры (зависит от типа арматуры)
 * - Rₐ - допускаемое напряжение по старым нормам (зависит от года)
 * - j - относительное изменение площади арматуры (по умолчанию 1.0)
 */
public class BetaCalculator {

    /**
     * Расчет коэффициента β
     *
     * @param designYear год выпуска норм проектирования
     * @param rebarType тип арматуры (гладкая или периодическая)
     * @param j относительное изменение площади арматуры (0...1), по умолчанию 1.0
     * @return коэффициент β
     */
    public static double calculateBeta(int designYear, RebarType rebarType, double j) {
        double Rs = rebarType.getRs();
        double Ra = getAllowedStress(designYear);
        return (Rs / Ra) * j;
    }

    /**
     * Расчет коэффициента β с j = 1.0 (без дефектов)
     */
    public static double calculateBeta(int designYear, RebarType rebarType) {
        return calculateBeta(designYear, rebarType, 1.0);
    }

    /**
     * Получение допускаемого напряжения для арматуры по старым нормам
     * (из Приложения 10, таблица П.10.2)
     *
     * @param designYear год выпуска норм проектирования
     * @return допускаемое напряжение в МПа
     */
    public static double getAllowedStress(int designYear) {
        // Перевод из кгс/см² в МПа: 1 кгс/см² = 0.0980665 МПа
        if (designYear >= 1908 && designYear < 1911) {
            return 800 * 0.0980665;   // ≈ 78.5 МПа
        } else if (designYear >= 1911 && designYear < 1921) {
            return 1000 * 0.0980665;  // ≈ 98.1 МПа
        } else if (designYear >= 1921 && designYear < 1926) {
            return 1200 * 0.0980665;  // ≈ 117.7 МПа
        } else if (designYear >= 1926 && designYear < 1929) {
            return 900 * 0.0980665;   // ≈ 88.3 МПа
        } else if (designYear >= 1929 && designYear < 1931) {
            return 1100 * 0.0980665;  // ≈ 107.9 МПа
        } else if (designYear >= 1931 && designYear < 1938) {
            return 1300 * 0.0980665;  // ≈ 127.5 МПа (в Приложении 5 = 130 МПа)
        } else if (designYear >= 1938 && designYear < 1947) {
            return 1200 * 0.0980665;  // ≈ 117.7 МПа
        } else if (designYear >= 1947) {
            return 1200 * 0.0980665;  // ≈ 117.7 МПа
        } else {
            // Для норм до 1908 года
            return 800 * 0.0980665;
        }
    }

    /**
     * Получение допускаемого напряжения в кгс/см² (для вывода)
     */
    public static double getAllowedStressKgscm2(int designYear) {
        if (designYear >= 1908 && designYear < 1911) {
            return 800;
        } else if (designYear >= 1911 && designYear < 1921) {
            return 1000;
        } else if (designYear >= 1921 && designYear < 1926) {
            return 1200;
        } else if (designYear >= 1926 && designYear < 1929) {
            return 900;
        } else if (designYear >= 1929 && designYear < 1931) {
            return 1100;
        } else if (designYear >= 1931 && designYear < 1938) {
            return 1300;
        } else if (designYear >= 1938 && designYear < 1947) {
            return 1200;
        } else if (designYear >= 1947) {
            return 1200;
        } else {
            return 800;
        }
    }

    /**
     * Вывод подробного отчета о расчете β
     */
    public static void printBetaReport(int designYear, RebarType rebarType, double j) {
        double Rs = rebarType.getRs();
        double Ra = getAllowedStress(designYear);
        double RaKgscm2 = getAllowedStressKgscm2(designYear);
        double beta = (Rs / Ra) * j;

        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ КОЭФФИЦИЕНТА β (ФОРМУЛА 8.3)");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Год выпуска норм: %d%n", designYear);
        System.out.printf("   Тип арматуры: %s%n", rebarType.getDisplayName());
        System.out.printf("   Расчетное сопротивление арматуры Rₛ: %.1f МПа%n", Rs);
        System.out.printf("   Допускаемое напряжение по нормам %d года: %d кгс/см² = %.1f МПа%n",
            designYear, (int)RaKgscm2, Ra);
        System.out.printf("   Относительное изменение площади j: %.3f%n", j);

        System.out.println("\n[2. Расчет по формуле 8.3]");
        System.out.println("   β = (Rₛ / Rₐ) × j");
        System.out.printf("   β = (%.1f / %.1f) × %.3f%n", Rs, Ra, j);
        System.out.printf("   β = %.3f × %.3f%n", (Rs / Ra), j);
        System.out.printf("   β = %.3f%n", beta);

        System.out.println("\n[3. Результат]");
        System.out.printf("   >>> β = %.3f <<<%n", beta);
        System.out.println("============================================================\n");
    }
}