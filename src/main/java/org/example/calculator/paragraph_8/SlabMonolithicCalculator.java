package org.example.calculator.paragraph_8;

import org.example.context.BridgeContext;
import org.example.model.RebarType;
import org.example.model.TrackType;
import org.example.util.Interpolation;

/**
 * Класс для расчета монолитного участка плиты балластного корыта
 * по формуле 8.2 (Раздел 8 Руководства)
 *
 * k = (0.95 * l0) / (ηM * nk * b) *
 *     [β * (8.75 * Kc * (1+μ₁) * b + p₁) - np * pp - np' * pb]
 */
public class SlabMonolithicCalculator {

    // =====================================================================
    // КОНСТАНТЫ
    // =====================================================================

    private static final double A = 8.75;
    private static final double GAMMA_RC = 24.5;
    private static final double GAMMA_BALLAST = 20.0;

    // Табличные данные для Приложения 9
    private static final double[] X_POINTS = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 18.0, 20.0};
    private static final double[] H7_ALPHA05 = {10.14, 9.30, 8.79, 9.81, 10.01, 9.67, 10.42, 10.67, 10.70, 10.88, 10.79, 10.62, 10.77, 10.77};
    private static final double[] H7_ALPHA00 = {10.17, 10.17, 10.33, 10.82, 10.71, 10.63, 10.79, 10.88, 10.82, 10.67, 10.51, 10.68, 10.68, 10.70};
    private static final double[] H8_ALPHA05 = {10.14, 9.30, 7.84, 8.64, 8.93, 8.61, 9.19, 9.39, 9.35, 9.19, 8.70, 8.83, 9.15, 9.43};
    private static final double[] H8_ALPHA00 = {10.17, 10.17, 10.33, 9.44, 9.43, 9.33, 9.44, 9.29, 9.09, 8.96, 9.01, 9.13, 9.33, 9.31};

    // =====================================================================
    // ОСНОВНОЙ МЕТОД РАСЧЕТА
    // =====================================================================

    public static double calculate(
        BridgeContext ctx,
        int designYear,
        double l0,
        double etaM,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha
    ) {
        double b = 1.0;
        double nk = ctx.nk;
        double np = ctx.np;
        double npPrime = ctx.npPrime;

        // 1. β (формула 8.3)
        double beta = BetaCalculator.calculateBeta(designYear, rebarType, j);

        // 2. Kc (Приложение 9)
        double Kc = getHistoricalLoadClass(loadType, ctx.spanLength, alpha);

        // 3. (1+μ₁) (Приложение 10) — уже возвращает готовое значение!
        double mu1 = getDesignDynamicCoefficient(
            designYear, ctx.spanLength, ctx.ballastThickness, ctx.trackType
        );

        // 4. Постоянные нагрузки
        double pp = ctx.slabHeight * GAMMA_RC;
        double pb = ctx.ballastThickness * GAMMA_BALLAST;
        double p1 = pp + pb;

        // 5. Формула 8.2
        double bracket1 = beta * (A * Kc * mu1 * b + p1);  // ← mu1 уже (1+μ₁)!
        double bracket2 = np * pp + npPrime * pb;
        double bracket = bracket1 - bracket2;

        double denominator = etaM * nk * b;
        double numerator = 0.95 * l0;

        double k = (numerator / denominator) * bracket;
        return Math.max(0, k);
    }

    // =====================================================================
    // ПРИЛОЖЕНИЕ 9
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
                return 1.20;  // ← для l=10.8 м → 1.20
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
        double l0,
        double etaM,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha,
        double k
    ) {
        double b = 1.0;
        double nk = ctx.nk;
        double np = ctx.np;
        double npPrime = ctx.npPrime;

        double beta = BetaCalculator.calculateBeta(designYear, rebarType, j);
        double Kc = getHistoricalLoadClass(loadType, ctx.spanLength, alpha);
        double mu1 = getDesignDynamicCoefficient(
            designYear, ctx.spanLength, ctx.ballastThickness, ctx.trackType
        );
        double Ra = BetaCalculator.getAllowedStress(designYear);
        double RaKgscm2 = BetaCalculator.getAllowedStressKgscm2(designYear);
        double pp = ctx.slabHeight * GAMMA_RC;
        double pb = ctx.ballastThickness * GAMMA_BALLAST;
        double p1 = pp + pb;

        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ МОНОЛИТНОГО УЧАСТКА ПЛИТЫ БАЛЛАСТНОГО КОРЫТА");
        System.out.println(" ФОРМУЛА 8.2");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Год выпуска норм: %d%n", designYear);
        System.out.printf("   Тип нагрузки: %s%n", loadType);
        System.out.printf("   Расчетный пролет: %.2f м%n", ctx.spanLength);
        System.out.printf("   Толщина балласта: %.2f м%n", ctx.ballastThickness);
        System.out.printf("   Толщина плиты: %.2f м%n", ctx.slabHeight);
        System.out.printf("   Тип арматуры: %s%n", rebarType.getDisplayName());
        System.out.printf("   j (отн. изменение площади): %.3f%n", j);
        System.out.printf("   l0 (длина распределения): %.2f м%n", l0);
        System.out.printf("   ηM (коэф. надежности): %.2f%n", etaM);
        System.out.printf("   α (положение вершины): %.1f%n", alpha);

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
        System.out.printf("        (1+μ₁) = %.3f  %n", mu1);  // ← mu1, а не 1+mu1!

        System.out.println("\n[3. Постоянные нагрузки]");
        System.out.printf("   3.1. Вес плиты: pp = h_slab × γ_rc = %.2f × %.1f = %.2f кН/м%n",
            ctx.slabHeight, GAMMA_RC, pp);
        System.out.printf("   3.2. Вес балласта: pb = h_b × γ_b = %.2f × %.1f = %.2f кН/м%n",
            ctx.ballastThickness, GAMMA_BALLAST, pb);
        System.out.printf("   3.3. Суммарная: p₁ = pp + pb = %.2f + %.2f = %.2f кН/м%n",
            pp, pb, p1);

        System.out.println("\n[4. Коэффициенты надежности (константы)]");
        System.out.printf("   nₖ = %.2f (к временной нагрузке, п. 6.5)%n", nk);
        System.out.printf("   n_p = %.2f (для ж/б, п. 6.3)%n", np);
        System.out.printf("   n'_p = %.2f (для балласта, п. 6.3)%n", npPrime);
        System.out.printf("   b = %.1f м (расчетная ширина плиты)%n", b);
        System.out.printf("   A = %.2f (коэф. системы СИ)%n", A);

        System.out.println("\n[5. Расчет по формуле 8.2]");
        System.out.println("   k = (0.95·l₀)/(ηM·nₖ·b) × [β·(8.75·Kc·(1+μ₁)·b + p₁) - nₚ·pₚ - n'ₚ·p_b]");

        double stepA = A * Kc * mu1 * b;
        double stepB = stepA + p1;
        double stepC = beta * stepB;
        double stepD = np * pp + npPrime * pb;
        double stepE = stepC - stepD;
        double stepF = (0.95 * l0) / (etaM * nk * b);
        double result = stepF * stepE;

        System.out.println("\n   Пошаговый расчет:");
        System.out.printf("   1) 8.75·Kc·(1+μ₁)·b = 8.75 × %.2f × %.3f × %.1f = %.2f%n",
            Kc, mu1, b, stepA);
        System.out.printf("   2) [8.75·Kc·(1+μ₁)·b] + p₁ = %.2f + %.2f = %.2f%n", stepA, p1, stepB);
        System.out.printf("   3) β × [(8.75·Kc·(1+μ₁)·b) + p₁] = %.3f × %.2f = %.2f%n", beta, stepB, stepC);
        System.out.printf("   4) nₚ·pₚ + n'ₚ·p_b = %.1f × %.2f + %.1f × %.2f = %.2f%n",
            np, pp, npPrime, pb, stepD);
        System.out.printf("   5) [β × ((8.75·Kc·(1+μ₁)·b) + p₁)] - [nₚ·pₚ + n'ₚ·p_b] = %.2f - %.2f = %.2f%n", stepC, stepD, stepE);
        System.out.printf("   6) (0.95·l₀)/(ηM·nₖ·b) = (0.95 × %.2f) / (%.2f × %.2f × %.1f) = %.4f%n",
            l0, etaM, nk, b, stepF);
        System.out.printf("   7) k = %.4f × %.2f = %.2f кН/м%n", stepF, stepE, result);

        System.out.println("\n[6. Результат]");
        System.out.printf("   >>> k = %.2f кН/м <<<%n", result);
        System.out.println("============================================================\n");
    }
}