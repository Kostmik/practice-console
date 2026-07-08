package org.example.calculator.paragraph_8;

import org.example.context.BridgeContext;
import org.example.model.RebarType;
import org.example.model.TrackType;
import org.example.util.Interpolation;

/**
 * Класс для расчета главной балки по формуле 8.4 (Раздел 8 Руководства)
 *
 * k = 0.85 / (εM * nk) *
 *     [β * (kc/m) * Kc * (1+μ₁) + β * p₁ - np * pp - np' * pb]
 *
 * где:
 * - εM - доля временной нагрузки на балку (из Раздела 6)
 * - kc - эталонная нагрузка (из Приложения 1)
 * - m - число главных балок
 * - остальные параметры как в формулах 8.1 и 8.2
 */
public class BeamCalculator {

    // =====================================================================
    // КОНСТАНТЫ
    // =====================================================================

    private static final double GAMMA_RC = 24.5;      // Удельный вес ж/б, кН/м³ (п. 6.1)
    private static final double GAMMA_BALLAST = 20.0;  // Удельный вес балласта с частями пути, кН/м³ (п. 6.1)

    // Табличные данные для Приложения 9 (классы нагрузок)
    private static final double[] X_POINTS = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 18.0, 20.0};
    private static final double[] H7_ALPHA05 = {10.14, 9.30, 8.79, 9.81, 10.01, 9.67, 10.42, 10.67, 10.70, 10.88, 10.79, 10.62, 10.77, 10.77};
    private static final double[] H7_ALPHA00 = {10.17, 10.17, 10.33, 10.82, 10.71, 10.63, 10.79, 10.88, 10.82, 10.67, 10.51, 10.68, 10.68, 10.70};
    private static final double[] H8_ALPHA05 = {10.14, 9.30, 7.84, 8.64, 8.93, 8.61, 9.19, 9.39, 9.35, 9.19, 8.70, 8.83, 9.15, 9.43};
    private static final double[] H8_ALPHA00 = {10.17, 10.17, 10.33, 9.44, 9.43, 9.33, 9.44, 9.29, 9.09, 8.96, 9.01, 9.13, 9.33, 9.31};

    // Табличные данные для Приложения 1 (эталонная нагрузка kc)
    // Для α = 0.5 (середина пролета)
    private static final double[] KC_X_POINTS = {1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 25.0, 30.0};
    private static final double[] KC_ALPHA05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};
    private static final double[] KC_ALPHA00 = {49.03, 39.15, 30.55, 24.16, 21.69, 20.37, 19.50, 18.84, 18.32, 17.87, 17.47, 16.78, 16.19, 15.66, 15.19, 14.76, 13.85, 13.10};

    // =====================================================================
    // ОСНОВНОЙ МЕТОД РАСЧЕТА
    // =====================================================================

    /**
     * Расчет допускаемой временной нагрузки для главной балки
     * по формуле 8.4
     *
     * @param ctx контекст моста
     * @param designYear год выпуска норм проектирования
     * @param rebarType тип арматуры
     * @param j относительное изменение площади арматуры
     * @param loadType тип нагрузки ("Н7" или "Н8")
     * @param alpha положение вершины линии влияния (0.0 или 0.5)
     * @param epsilon доля временной нагрузки на балку (из Раздела 6)
     * @param m число главных балок
     * @param pp вес пролетного строения на балку, кН/м
     * @param pb вес балласта на балку, кН/м
     * @return допускаемая временная нагрузка k, кН/м
     */
    public static double calculate(
        BridgeContext ctx,
        int designYear,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha,
        double epsilon,
        int m,
        double pp,
        double pb
    ) {
        // Константы из BridgeContext
        double nk = ctx.nk;                // 1.15 (п. 6.5)
        double np = ctx.np;                // 1.1 (п. 6.3)
        double npPrime = ctx.npPrime;      // 1.2 (п. 6.3)

        // ===== 1. ВЫЧИСЛЯЕМ β (формула 8.3) =====
        double beta = BetaCalculator.calculateBeta(designYear, rebarType, j);

        // ===== 2. ВЫЧИСЛЯЕМ Kc (Приложение 9) =====
        double Kc = getHistoricalLoadClass(loadType, ctx.spanLength, alpha);

        // ===== 3. ВЫЧИСЛЯЕМ (1+μ₁) (Приложение 10) =====
        double mu1 = getDesignDynamicCoefficient(
            designYear, ctx.spanLength, ctx.ballastThickness, ctx.trackType
        );

        // ===== 4. ВЫЧИСЛЯЕМ kc (Приложение 1) =====
        double kc = getStandardLoad(ctx.spanLength, alpha);

        // ===== 5. ПОСТОЯННАЯ НАГРУЗКА НА БАЛКУ =====
        double p1 = pp + pb;

        // ===== 6. РАСЧЕТ ПО ФОРМУЛЕ 8.4 =====
        // k = 0.85 / (εM * nk) *
        //     [β * (kc/m) * Kc * (1+μ₁) + β * p₁ - np * pp - np' * pb]

        double term1 = beta * (kc / m) * Kc * mu1;
        double term2 = beta * p1;
        double term3 = np * pp;
        double term4 = npPrime * pb;

        double bracket = term1 + term2 - term3 - term4;
        double k = (0.85) / (epsilon * nk) * bracket;

        return Math.max(0, k);
    }

    // =====================================================================
    // ПРИЛОЖЕНИЕ 9 — классы нагрузок
    // =====================================================================

    private static double getHistoricalLoadClass(String loadType, double spanLength, double alpha) {
        double[] yPoints;

        if (alpha >= 0.5) {
            if (loadType.equalsIgnoreCase("Н7") || loadType.equalsIgnoreCase("H7")) {
                yPoints = H7_ALPHA05;
            } else {
                yPoints = H8_ALPHA05;
            }
        } else {
            if (loadType.equalsIgnoreCase("Н7") || loadType.equalsIgnoreCase("H7")) {
                yPoints = H7_ALPHA00;
            } else {
                yPoints = H8_ALPHA00;
            }
        }

        return Interpolation.linear(X_POINTS, yPoints, spanLength);
    }

    // =====================================================================
    // ПРИЛОЖЕНИЕ 1 — эталонная нагрузка kc
    // =====================================================================

    private static double getStandardLoad(double spanLength, double alpha) {
        double[] yPoints;

        if (alpha >= 0.5) {
            yPoints = KC_ALPHA05;
        } else {
            yPoints = KC_ALPHA00;
        }

        return Interpolation.linear(KC_X_POINTS, yPoints, spanLength);
    }

    // =====================================================================
    // ПРИЛОЖЕНИЕ 10 — динамический коэффициент
    // =====================================================================

    private static double getDesignDynamicCoefficient(int designYear, double spanLength,
                                                      double ballastThickness, TrackType trackType) {
        if (designYear >= 1908 && designYear < 1911) {
            return 1.25;
        } else if (designYear >= 1911 && designYear < 1921) {
            return 1.25;
        } else if (designYear >= 1921 && designYear < 1926) {
            return (ballastThickness > 0.15) ? 1.20 : 1.35;
        } else if (designYear >= 1926 && designYear < 1929) {
            return 1.40;
        } else if (designYear >= 1929 && designYear < 1931) {
            return 1.0 + 9.0 / (20.0 + spanLength);
        } else if (designYear >= 1931 && designYear < 1938) {
            if (spanLength <= 5.0) {
                return 1.30;
            } else if (spanLength <= 20.0) {
                return 1.20;
            } else {
                return 1.10;
            }
        } else if (designYear >= 1938 && designYear < 1947) {
            return Math.min(1.0 + 20.0 / (30.0 + spanLength), 1.50);
        } else if (designYear >= 1947) {
            return 1.0 + 12.0 / (20.0 + spanLength);
        } else {
            return 1.25;
        }
    }

    // =====================================================================
    // МЕТОД ВЫВОДА ОТЧЕТА
    // =====================================================================

    public static void printReport(
        BridgeContext ctx,
        int designYear,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha,
        double epsilon,
        int m,
        double pp,
        double pb,
        double k
    ) {
        double nk = ctx.nk;
        double np = ctx.np;
        double npPrime = ctx.npPrime;

        double beta = BetaCalculator.calculateBeta(designYear, rebarType, j);
        double Kc = getHistoricalLoadClass(loadType, ctx.spanLength, alpha);
        double mu1 = getDesignDynamicCoefficient(
            designYear, ctx.spanLength, ctx.ballastThickness, ctx.trackType
        );
        double kc = getStandardLoad(ctx.spanLength, alpha);
        double Ra = BetaCalculator.getAllowedStress(designYear);
        double RaKgscm2 = BetaCalculator.getAllowedStressKgscm2(designYear);
        double p1 = pp + pb;

        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ ГЛАВНОЙ БАЛКИ");
        System.out.println(" ФОРМУЛА 8.4");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Год выпуска норм: %d%n", designYear);
        System.out.printf("   Тип нагрузки: %s%n", loadType);
        System.out.printf("   Расчетный пролет: %.2f м%n", ctx.spanLength);
        System.out.printf("   Толщина балласта: %.2f м%n", ctx.ballastThickness);
        System.out.printf("   Тип арматуры: %s%n", rebarType.getDisplayName());
        System.out.printf("   j (отн. изменение площади): %.3f%n", j);
        System.out.printf("   α (положение вершины): %.1f%n", alpha);
        System.out.printf("   εM (доля нагрузки на балку): %.3f%n", epsilon);
        System.out.printf("   m (число балок): %d%n", m);
        System.out.printf("   pp (вес ПС на балку): %.2f кН/м%n", pp);
        System.out.printf("   pb (вес балласта на балку): %.2f кН/м%n", pb);

        System.out.println("\n[2. Вычисленные коэффициенты]");
        System.out.println("   2.1. Допускаемое напряжение по нормам:");
        System.out.printf("        Rₐ = %d кгс/см² = %.1f МПа (Приложение 10)%n",
            (int)RaKgscm2, Ra);
        System.out.printf("        Rₛ = %.1f МПа (п. 5.2.1)%n", rebarType.getRs());
        System.out.println("   2.2. Коэффициент β (формула 8.3):");
        System.out.printf("        β = (Rₛ / Rₐ) × j = (%.1f / %.1f) × %.3f = %.3f%n",
            rebarType.getRs(), Ra, j, beta);
        System.out.println("   2.3. Класс нагрузки Kc (Приложение 9):");
        System.out.printf("        Kc = %.2f%n", Kc);
        System.out.println("   2.4. Динамический коэффициент (Приложение 10):");
        System.out.printf("        (1+μ₁) = %.3f%n", mu1);
        System.out.println("   2.5. Эталонная нагрузка kc (Приложение 1):");
        System.out.printf("        kc = %.2f кН/м%n", kc);

        System.out.println("\n[3. Постоянные нагрузки]");
        System.out.printf("   3.1. Вес ПС на балку: pp = %.2f кН/м%n", pp);
        System.out.printf("   3.2. Вес балласта на балку: pb = %.2f кН/м%n", pb);
        System.out.printf("   3.3. Суммарная: p₁ = pp + pb = %.2f + %.2f = %.2f кН/м%n", pp, pb, p1);

        System.out.println("\n[4. Коэффициенты надежности (константы)]");
        System.out.printf("   nₖ = %.2f (к временной нагрузке, п. 6.5)%n", nk);
        System.out.printf("   n_p = %.2f (для ж/б, п. 6.3)%n", np);
        System.out.printf("   n'_p = %.2f (для балласта, п. 6.3)%n", npPrime);

        System.out.println("\n[5. Расчет по формуле 8.4]");
        System.out.println("   k = 0.85/(εM·nₖ) × [β·(kc/m)·Kc·(1+μ₁) + β·p₁ - nₚ·pₚ - n'ₚ·p_b]");

        double step1 = (kc / m) * Kc * mu1;
        double step2 = beta * step1;
        double step3 = beta * p1;
        double step4 = np * pp + npPrime * pb;
        double step5 = step2 + step3 - step4;
        double step6 = 0.85 / (epsilon * nk);
        double result = step6 * step5;

        System.out.println("\n   Пошаговый расчет:");
        System.out.printf("   1) (kc/m)·Kc·(1+μ₁) = (%.2f/%d) × %.2f × %.3f = %.2f%n",
            kc, m, Kc, mu1, step1);
        System.out.printf("   2) β × (...) = %.3f × %.2f = %.2f%n", beta, step1, step2);
        System.out.printf("   3) β·p₁ = %.3f × %.2f = %.2f%n", beta, p1, step3);
        System.out.printf("   4) nₚ·pₚ + n'ₚ·p_b = %.1f×%.2f + %.1f×%.2f = %.2f%n",
            np, pp, npPrime, pb, step4);
        System.out.printf("   5) [...] = %.2f + %.2f - %.2f = %.2f%n", step2, step3, step4, step5);
        System.out.printf("   6) 0.85/(εM·nₖ) = 0.85/(%.3f×%.2f) = %.4f%n", epsilon, nk, step6);
        System.out.printf("   7) k = %.4f × %.2f = %.2f кН/м%n", step6, step5, result);

        System.out.println("\n[6. Результат]");
        System.out.printf("   >>> k = %.2f кН/м <<<%n", result);
        System.out.println("============================================================\n");
    }
}