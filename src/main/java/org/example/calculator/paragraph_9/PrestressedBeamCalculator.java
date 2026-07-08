package org.example.calculator.paragraph_9;

import org.example.context.BridgeContext;
import org.example.model.TrackType;
import org.example.util.Interpolation;

/**
 * Класс для расчета грузоподъемности пролетных строений
 * с напрягаемой арматурой (Раздел 9 Руководства)
 */
public class PrestressedBeamCalculator {

    // =====================================================================
    // КОНСТАНТЫ
    // =====================================================================

    private static final double EPSILON_B = 0.85;
    private static final double SIGMA_P2_LOSS = 500.0;
    private static final double GAMMA_RC = 24.5;
    private static final double GAMMA_BALLAST = 20.0;

    // Табличные данные для Приложения 1 (эталонная нагрузка kc)
    private static final double[] KC_X_POINTS = {1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 25.0, 30.0};
    private static final double[] KC_ALPHA05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};
    private static final double[] KC_ALPHA00 = {49.03, 39.15, 30.55, 24.16, 21.69, 20.37, 19.50, 18.84, 18.32, 17.87, 17.47, 16.78, 16.19, 15.66, 15.19, 14.76, 13.85, 13.10};

    // =====================================================================
    // 9.1.1 - ГРАНИЧНАЯ ВЫСОТА СЖАТОЙ ЗОНЫ (Формула 9.1)
    // =====================================================================

    public static double calculateXiY(double Rb, double Rp, double sigmaP2, double Rpc) {
        double numerator = EPSILON_B - 0.008 * Rb;
        double denominator = 1 + (Rp + 500 - sigmaP2) / Rpc * (1 - (EPSILON_B - 0.008 * Rb) / 1.1);
        return numerator / denominator;
    }

    // =====================================================================
// 9.2 - ПРЕДЕЛЬНЫЙ МОМЕНТ (граница в ребре) - Формула 9.2
// =====================================================================
    public static double calculateMomentWithWeb(
        double Rb, double b, double x, double h0,
        double bf, double hf,
        double Rsc, double As, double as,
        double sigmaPc, double Ap, double ap
    ) {
        double Rb_kPa = Rb * 1000;
        double Rsc_kPa = Rsc * 1000;
        double sigmaPc_kPa = sigmaPc * 1000;

        double term1 = Rb_kPa * b * x * (h0 - 0.5 * x);
        double term2 = Rb_kPa * (bf - b) * hf * (h0 - 0.5 * hf);
        double term3 = Rsc_kPa * As * (h0 - as);
        double term4 = sigmaPc_kPa * Ap * (h0 - ap);

        // ИСПРАВЛЕНО: убрано деление на 1000.0, результат сразу в кН*м
        return (term1 + term2 + term3 + term4);
    }

    // =====================================================================
    // 9.3 - ВЫСОТА СЖАТОЙ ЗОНЫ (граница в ребре) - Формула 9.3
    // =====================================================================

    public static double calculateXWithWeb(
        double Rs, double As, double Rp, double Ap,
        double Rsc, double As_s, double sigmaPx, double Ap_s,
        double Rb, double bf, double b, double hf
    ) {
        double Rs_kPa = Rs * 1000;
        double Rp_kPa = Rp * 1000;
        double Rsc_kPa = Rsc * 1000;
        double sigmaPx_kPa = sigmaPx * 1000;
        double Rb_kPa = Rb * 1000;

        double numerator = Rs_kPa * As + Rp_kPa * Ap - Rsc_kPa * As_s - sigmaPx_kPa * Ap_s
            - Rb_kPa * (bf - b) * hf;
        double denominator = Rb_kPa * b;

        return numerator / denominator;
    }

    // =====================================================================
    // 9.4 - НАПРЯЖЕНИЕ В АРМАТУРЕ СЖАТОЙ ЗОНЫ - Формула 9.4
    // =====================================================================

    public static double calculateSigmaPc(double Rpc, double sigmaP2) {
        return Rpc - sigmaP2;
    }

    // =====================================================================
    // 9.5 - ПРЕДЕЛЬНЫЙ МОМЕНТ (граница в плите) - Формула 9.5
    // =====================================================================
    public static double calculateMomentWithFlange(
        double Rb, double bf, double x, double h0,
        double Rsc, double As, double as,
        double sigmaPc, double Ap, double ap
    ) {
        double Rb_kPa = Rb * 1000;
        double Rsc_kPa = Rsc * 1000;
        double sigmaPc_kPa = sigmaPc * 1000;

        double term1 = Rb_kPa * bf * x * (h0 - 0.5 * x);
        double term2 = Rsc_kPa * As * (h0 - as);
        double term3 = sigmaPc_kPa * Ap * (h0 - ap);

        // ИСПРАВЛЕНО: убрано деление на 1000.0, результат сразу в кН*м
        return (term1 + term2 + term3);
    }

    // =====================================================================
    // 9.6 - ВЫСОТА СЖАТОЙ ЗОНЫ (граница в плите) - Формула 9.6
    // =====================================================================

    public static double calculateXWithFlange(
        double Rs, double As, double Rp, double Ap,
        double Rsc, double As_s, double sigmaPc, double Ap_s,
        double Rb, double bf
    ) {
        double Rs_kPa = Rs * 1000;
        double Rp_kPa = Rp * 1000;
        double Rsc_kPa = Rsc * 1000;
        double sigmaPc_kPa = sigmaPc * 1000;
        double Rb_kPa = Rb * 1000;

        double numerator = Rs_kPa * As + Rp_kPa * Ap - Rsc_kPa * As_s - sigmaPc_kPa * Ap_s;
        double denominator = Rb_kPa * bf;

        return numerator / denominator;
    }

    // =====================================================================
    // 9.7 - ПОПЕРЕЧНАЯ СИЛА ПО НАКЛОННОЙ ТРЕЩИНЕ - Формула 9.7
    // =====================================================================
    public static double calculateShearForce(
        double Rp, double sumApi, double sinAlpha,
        double Rs, double Asw, double c, double s,
        double Qb
    ) {
        double Rp_kPa = Rp * 1000;
        double Rs_kPa = Rs * 1000;

        double term1 = 0.7 * Rp_kPa * sumApi * sinAlpha;
        double term2 = 0.8 * Rs_kPa * Asw * c / s;

        // ИСПРАВЛЕНО: убрано деление на 1000.0, результат сразу в кН
        return (term1 + term2 + Qb);
    }

    // =====================================================================
    // 9.8 - СОПРОТИВЛЕНИЕ НА ВЫНОСЛИВОСТЬ - Формула 9.8
    // =====================================================================

    public static double calculateRpf(double epsilonRo, double Rb) {
        return epsilonRo * Rb;
    }

    // =====================================================================
    // 9.9 - АСИММЕТРИЯ ЦИКЛА НАПРЯЖЕНИЙ - Формула 9.9
    // =====================================================================

    public static double calculateRho(double sigmaMin, double sigmaMax) {
        if (sigmaMax == 0) return 0;
        return sigmaMin / sigmaMax;
    }

    // =====================================================================
    // 9.10-9.11 - НАПРЯЖЕНИЯ В АРМАТУРЕ
    // =====================================================================

    public static double calculateSigmaInRebar(
        double sigmaP2, double nPrime,
        double Ap, double h01, double x,
        double Ared, double Ired,
        double Mg, double Mk,
        boolean isMax
    ) {
        double term1 = sigmaP2 - nPrime * (sigmaP2 * Ap + sigmaP2 * Ap * Math.pow(h01 - x, 2) / Ared);
        double term2 = nPrime * (Mg * 1000) / Ired * (h01 - x);

        double term3 = 0;
        if (isMax) {
            term3 = nPrime * (Mk * 1000) / Ired * (h01 - x);
        }

        return term1 + term2 + term3;
    }

    // =====================================================================
    // 9.12-9.13 - НАПРЯЖЕНИЯ В БЕТОНЕ
    // =====================================================================

    public static double calculateSigmaInConcrete(
        double sigmaP2, double Ap, double sigmaP2s, double Aps,
        double h01, double x, double ap,
        double Ared, double Ired,
        double Mg, double Mk,
        boolean isMax
    ) {
        double term1 = (sigmaP2 * Ap + sigmaP2s * Aps) / Ared;
        double term2 = (sigmaP2 * Ap * (h01 - x) + sigmaP2s * Aps * (x - ap)) / Ired * x;
        double term3 = (Mg * 1000) / Ired * x;

        double term4 = 0;
        if (isMax) {
            term4 = (Mk * 1000) / Ired * x;
        }

        return term1 + term2 + term3 + term4;
    }

    // =====================================================================
    // 9.14 - ВЫСОТА СЖАТОЙ ЗОНЫ ДЛЯ ВЫНОСЛИВОСТИ - Формула 9.14
    // =====================================================================

    public static double calculateXForFatigue(double h, double hc) {
        return h - hc;
    }

    // =====================================================================
    // 9.15 - МОМЕНТ ОТ ВРЕМЕННОЙ НАГРУЗКИ - Формула 9.15
    // =====================================================================

    public static double calculateMk(double Omega, double epsilon, double k, double Theta) {
        return Omega * epsilon * k * Theta;
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЙ МЕТОД - ЗНАЧЕНИЯ КОЭФФИЦИЕНТА ε_ρ
    // =====================================================================

    public static double getEpsilonRo(double rho) {
        if (rho <= 0.1) return 1.00;
        if (rho <= 0.2) return 1.06;
        if (rho <= 0.3) return 1.10;
        if (rho <= 0.4) return 1.15;
        if (rho <= 0.5) return 1.20;
        return 1.24;
    }

    /**
     * @param spanLength длина загружения (м)
     * @param alpha положение вершины линии влияния (0.0 или 0.5)
     * @return эталонная нагрузка (кН/м)
     */
    public static double getStandardLoad(double spanLength, double alpha) {
        double[] yPoints = (alpha >= 0.5) ? KC_ALPHA05 : KC_ALPHA00;
        return Interpolation.linear(KC_X_POINTS, yPoints, spanLength);
    }

    // =====================================================================
    // МЕТОД ВЫВОДА ОТЧЕТА
    // =====================================================================

    public static void printReport(
        BridgeContext ctx,
        double Rb, double Rp, double Rpc,
        double sigmaP2, double sigmaP2s,
        double As, double Ap, double As_s, double Ap_s,
        double b, double bf, double hf,
        double h, double h0, double h01,
        double as, double ap,
        double Mg, double Mk,
        double epsilon, double Omega,
        double Theta, double nPrime,
        double Ared, double Ired,
        double k
    ) {
        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ ПРОЛЕТНОГО СТРОЕНИЯ С НАПРЯГАЕМОЙ АРМАТУРОЙ");
        System.out.println(" РАЗДЕЛ 9");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Расчетный пролет: %.2f м%n", ctx.spanLength);
        System.out.printf("   Толщина балласта: %.2f м%n", ctx.ballastThickness);
        System.out.printf("   Прочность бетона R: %.1f МПа%n", ctx.concreteStrengthR);
        System.out.printf("   Rb (сжатие): %.1f МПа%n", Rb);
        System.out.printf("   Rp (напрягаемая арматура): %.1f МПа%n", Rp);
        System.out.printf("   Rpc (напрягаемая арматура сжатие): %.1f МПа%n", Rpc);
        System.out.printf("   sigma_p2 (предв. напряжение): %.1f МПа%n", sigmaP2);
        System.out.printf("   sigma'_p2 (предв. напряжение сжатой зоны): %.1f МПа%n", sigmaP2s);
        System.out.printf("   As (ненапрягаемая арматура): %.4f м²%n", As);
        System.out.printf("   Ap (напрягаемая арматура): %.4f м²%n", Ap);
        System.out.printf("   As' (сжатая ненапрягаемая): %.4f м²%n", As_s);
        System.out.printf("   Ap' (сжатая напрягаемая): %.4f м²%n", Ap_s);
        System.out.printf("   b (ширина ребра): %.2f м%n", b);
        System.out.printf("   bf (ширина плиты): %.2f м%n", bf);
        System.out.printf("   hf (толщина плиты): %.2f м%n", hf);
        System.out.printf("   h (высота балки): %.2f м%n", h);
        System.out.printf("   h0 (рабочая высота): %.2f м%n", h0);
        System.out.printf("   h01 (рабочая высота для напрягаемой): %.2f м%n", h01);
        System.out.printf("   as (расстояние до сжатой арматуры): %.2f м%n", as);
        System.out.printf("   ap (расстояние до напрягаемой арматуры): %.2f м%n", ap);
        System.out.printf("   Mg (момент от пост. нагрузок): %.2f кН·м%n", Mg);
        System.out.printf("   Mk (момент от временной нагрузки): %.2f кН·м%n", Mk);
        System.out.printf("   εM (доля нагрузки): %.3f%n", epsilon);
        System.out.printf("   Ω (площадь линии влияния): %.2f м²%n", Omega);
        System.out.printf("   Θ (коэф. уменьшения динамики): %.3f%n", Theta);
        System.out.printf("   n' (отношение модулей): %.1f%n", nPrime);
        System.out.printf("   A_red (приведенная площадь): %.4f м²%n", Ared);
        System.out.printf("   I_red (приведенный момент инерции): %.4f м⁴%n", Ired);

        System.out.println("\n[2. Расчет по формуле 9.1 (граничная высота сжатой зоны)]");
        double xiY = calculateXiY(Rb, Rp, sigmaP2, Rpc);
        System.out.printf("   ξ_y = %.3f%n", xiY);

        System.out.println("\n[3. Расчет по формуле 9.4 (напряжение в арматуре сжатой зоны)]");
        double sigmaPc = calculateSigmaPc(Rpc, sigmaP2s);
        System.out.printf("   σ_pc = R_pc - σ'_p2 = %.1f - %.1f = %.1f МПа%n", Rpc, sigmaP2s, sigmaPc);

        System.out.println("\n[4. Результат]");
        System.out.printf("   >>> k = %.2f кН/м <<<%n", k);
        System.out.println("============================================================\n");
    }
}