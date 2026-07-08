package org.example.calculator.paragraph_8;

import org.example.context.BridgeContext;
import org.example.model.RebarType;
import org.example.model.TrackType;
import org.example.util.Interpolation;

/**
 * Класс для расчета внешней консоли плиты балластного корыта
 * по формуле 8.1 (Раздел 8 Руководства)
 *
 * k = (0.95 * l0) / (ηM * nk * b * (Δ - Z)²) *
 *     [β * (A * Kc * (1+μ₁) * b * (0.5*ls + hb - 0.5*B)² + p₁*lk² + 2*P₀*lt) - 2*Mp]
 */
public class SlabCantileverCalculator {

    // =====================================================================
    // КОНСТАНТЫ
    // =====================================================================

    private static final double A = 8.75;             // Коэффициент для системы СИ (в формуле 8.1)
    private static final double GAMMA_RC = 24.5;     // Удельный вес ж/б, кН/м³ (п. 6.1)
    private static final double GAMMA_BALLAST = 20.0; // Удельный вес балласта с частями пути, кН/м³ (п. 6.1)

    // Табличные данные для Приложения 9
    private static final double[] X_POINTS = {1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 15.0, 18.0, 20.0};
    private static final double[] H7_ALPHA05 = {10.14, 9.30, 8.79, 9.81, 10.01, 9.67, 10.42, 10.67, 10.70, 10.88, 10.79, 10.62, 10.77, 10.77};
    private static final double[] H7_ALPHA00 = {10.17, 10.17, 10.33, 10.82, 10.71, 10.63, 10.79, 10.88, 10.82, 10.67, 10.51, 10.68, 10.68, 10.70};
    private static final double[] H8_ALPHA05 = {10.14, 9.30, 7.84, 8.64, 8.93, 8.61, 9.19, 9.39, 9.35, 9.19, 8.70, 8.83, 9.15, 9.43};
    private static final double[] H8_ALPHA00 = {10.17, 10.17, 10.33, 9.44, 9.43, 9.33, 9.44, 9.29, 9.09, 8.96, 9.01, 9.13, 9.33, 9.31};

    // =====================================================================
    // ОСНОВНОЙ МЕТОД РАСЧЕТА
    // =====================================================================

    /**
     * Расчет допускаемой временной нагрузки для внешней консоли плиты
     * по формуле 8.1
     *
     * @param ctx контекст моста
     * @param designYear год выпуска норм проектирования
     * @param l0 длина распределения временной нагрузки, м (вводит пользователь)
     * @param etaM коэффициент надежности по назначению (вводит пользователь)
     * @param rebarType тип арматуры
     * @param j относительное изменение площади арматуры
     * @param loadType тип нагрузки ("Н7" или "Н8")
     * @param alpha положение вершины линии влияния (0.0 или 0.5)
     * @param delta расстояние от ребра до точки приложения нагрузки, м
     * @param Z расстояние от ребра до расчетного сечения, м
     * @param lk длина внешней консоли плиты, м
     * @param P0 нагрузка от веса перил, кН
     * @param lt расстояние до центра тяжести перил, м
     * @param Mp изгибающий момент от постоянных нагрузок, кН·м
     * @return допускаемая временная нагрузка k, кН/м
     */
    public static double calculate(
        BridgeContext ctx,
        int designYear,
        double l0,
        double etaM,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha,
        double delta,
        double Z,
        double lk,
        double P0,
        double lt,
        double Mp
    ) {
        // Константы из BridgeContext
        double b = 1.0;                    // Расчетная ширина плиты (всегда 1 м)
        double nk = ctx.nk;                // 1.15 (п. 6.5)

        // ===== 1. ВЫЧИСЛЯЕМ β (формула 8.3) =====
        double beta = BetaCalculator.calculateBeta(designYear, rebarType, j);

        // ===== 2. ВЫЧИСЛЯЕМ Kc (Приложение 9) =====
        double Kc = getHistoricalLoadClass(loadType, ctx.spanLength, alpha);

        // ===== 3. ВЫЧИСЛЯЕМ (1+μ₁) (Приложение 10) =====
        double mu1 = getDesignDynamicCoefficient(
            designYear, ctx.spanLength, ctx.ballastThickness, ctx.trackType
        );

        // ===== 4. ВЫЧИСЛЯЕМ ПОСТОЯННЫЕ НАГРУЗКИ =====
        // Нагрузка от веса плиты на консоли: pp = h_slab * γ_rc
        double pp = ctx.slabHeight * GAMMA_RC;

        // Нагрузка от веса балласта на консоли: pb = h_b * γ_b
        double pb = ctx.ballastThickness * GAMMA_BALLAST;

        // Суммарная постоянная нагрузка на консоль: p₁ = pp + pb
        double p1 = pp + pb;

        // ===== 5. ВЫЧИСЛЯЕМ ПО ФОРМУЛЕ 8.1 =====
        // Часть 1: A * Kc * (1+μ₁) * b * (0.5*ls + hb - 0.5*B)²
        double term1 = A * Kc * mu1 * b * Math.pow(0.5 * ctx.ls + ctx.ballastThickness - 0.5 * ctx.B, 2);

        // Часть 2: p₁ * lk²
        double term2 = p1 * Math.pow(lk, 2);

        // Часть 3: 2 * P₀ * lt
        double term3 = 2 * P0 * lt;

        // Сумма в скобках: β * (term1 + term2 + term3) - 2 * Mp
        double bracket = beta * (term1 + term2 + term3) - 2 * Mp;

        // Знаменатель: (0.95 * l0) / (ηM * nk * b * (Δ - Z)²)
        double denominator = etaM * nk * b * Math.pow(delta - Z, 2);
        double numerator = 0.95 * l0;

        double k = (numerator / denominator) * bracket;
        return Math.max(0, k);
    }

    // =====================================================================
    // ПРИЛОЖЕНИЕ 9 — с использованием Interpolation
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
        double l0,
        double etaM,
        RebarType rebarType,
        double j,
        String loadType,
        double alpha,
        double delta,
        double Z,
        double lk,
        double P0,
        double lt,
        double Mp,
        double k
    ) {
        double b = 1.0;
        double nk = ctx.nk;

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
        System.out.println(" РАСЧЕТ ВНЕШНЕЙ КОНСОЛИ ПЛИТЫ БАЛЛАСТНОГО КОРЫТА");
        System.out.println(" ФОРМУЛА 8.1");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Год выпуска норм: %d%n", designYear);
        System.out.printf("   Тип нагрузки: %s%n", loadType);
        System.out.printf("   Расчетный пролет: %.2f м%n", ctx.spanLength);
        System.out.printf("   Толщина балласта: %.2f м%n", ctx.ballastThickness);
        System.out.printf("   Толщина плиты: %.2f м%n", ctx.slabHeight);
        System.out.printf("   Расстояние между ребрами B: %.2f м%n", ctx.B);
        System.out.printf("   Длина шпалы ls: %.2f м%n", ctx.ls);
        System.out.printf("   Тип арматуры: %s%n", rebarType.getDisplayName());
        System.out.printf("   j (отн. изменение площади): %.3f%n", j);
        System.out.printf("   l0 (длина распределения): %.2f м%n", l0);
        System.out.printf("   ηM (коэф. надежности): %.2f%n", etaM);
        System.out.printf("   Δ (расстояние от ребра): %.2f м%n", delta);
        System.out.printf("   Z (расстояние до сечения): %.2f м%n", Z);
        System.out.printf("   lk (длина консоли): %.2f м%n", lk);
        System.out.printf("   P0 (вес перил): %.2f кН%n", P0);
        System.out.printf("   lt (расстояние до перил): %.2f м%n", lt);
        System.out.printf("   Mp (момент от пост. нагрузок): %.2f кН·м%n", Mp);
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
        System.out.printf("        (1+μ₁) = %.3f%n", mu1);

        System.out.println("\n[3. Постоянные нагрузки]");
        System.out.printf("   3.1. Вес плиты: pp = h_slab × γ_rc = %.2f × %.1f = %.2f кН/м%n",
            ctx.slabHeight, GAMMA_RC, pp);
        System.out.printf("   3.2. Вес балласта: pb = h_b × γ_b = %.2f × %.1f = %.2f кН/м%n",
            ctx.ballastThickness, GAMMA_BALLAST, pb);
        System.out.printf("   3.3. Суммарная на консоли: p₁ = pp + pb = %.2f + %.2f = %.2f кН/м%n",
            pp, pb, p1);

        System.out.println("\n[4. Коэффициенты надежности (константы)]");
        System.out.printf("   nₖ = %.2f (к временной нагрузке, п. 6.5)%n", nk);
        System.out.printf("   b = %.1f м (расчетная ширина плиты)%n", b);
        System.out.printf("   A = %.1f (коэф. системы СИ, формула 8.1)%n", A);

        System.out.println("\n[5. Расчет по формуле 8.1]");
        System.out.println("   k = (0.95·l₀)/(ηM·nₖ·b·(Δ-Z)²) ×");
        System.out.println("       [β·(A·Kc·(1+μ₁)·b·(0.5·ls+hb-0.5·B)² + p₁·lk² + 2·P₀·lt) - 2·Mp]");

        // Пошаговый расчет
        double step1 = A * Kc * mu1 * b * Math.pow(0.5 * ctx.ls + ctx.ballastThickness - 0.5 * ctx.B, 2);
        double step2 = p1 * Math.pow(lk, 2);
        double step3 = 2 * P0 * lt;
        double step4 = beta * (step1 + step2 + step3);
        double step5 = step4 - 2 * Mp;
        double step6 = (0.95 * l0) / (etaM * nk * b * Math.pow(delta - Z, 2));
        double result = step6 * step5;

        System.out.println("\n   Пошаговый расчет:");
        System.out.printf("   1) (0.5·ls+hb-0.5·B) = (0.5×%.2f + %.2f - 0.5×%.2f) = %.3f м%n",
            ctx.ls, ctx.ballastThickness, ctx.B,
            0.5 * ctx.ls + ctx.ballastThickness - 0.5 * ctx.B);
        System.out.printf("   2) A·Kc·(1+μ₁)·b·(...)² = %.1f × %.2f × %.3f × %.1f × %.3f² = %.2f%n",
            A, Kc, mu1, b, 0.5 * ctx.ls + ctx.ballastThickness - 0.5 * ctx.B, step1);
        System.out.printf("   3) p₁·lk² = %.2f × %.2f² = %.2f%n", p1, lk, step2);
        System.out.printf("   4) 2·P₀·lt = 2 × %.2f × %.2f = %.2f%n", P0, lt, step3);
        System.out.printf("   5) β·(...) = %.3f × (%.2f + %.2f + %.2f) = %.2f%n",
            beta, step1, step2, step3, step4);
        System.out.printf("   6) - 2·Mp = %.2f - 2×%.2f = %.2f%n", step4, Mp, step5);
        System.out.printf("   7) (0.95·l₀)/(ηM·nₖ·b·(Δ-Z)²) = (0.95×%.2f)/(%.2f×%.2f×%.1f×(%.2f-%.2f)²) = %.4f%n",
            l0, etaM, nk, b, delta, Z, step6);
        System.out.printf("   8) k = %.4f × %.2f = %.2f кН/м%n", step6, step5, result);

        System.out.println("\n[6. Результат]");
        System.out.printf("   >>> k = %.2f кН/м <<<%n", result);
        System.out.println("============================================================\n");
    }
}