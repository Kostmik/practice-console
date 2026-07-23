package org.example.calculator.paragraph_9;

import org.example.context.BridgeContext;
import org.example.util.Interpolation;

/**
 * Калькулятор Раздела 9 "Определение грузоподъемности пролетных строений с напрягаемой арматурой"
 * Реализует формулы 9.1 – 9.15 Руководства ОАО "РЖД" № 249/р.
 */
public final class PrestressedBeamCalculator {
    private static final double EPSILON_B = 0.85;

    // Эталонная нагрузка C1 (Приложение 1)
    private static final double[] KC_X_POINTS = {1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0, 12.0, 14.0, 16.0, 18.0, 20.0, 25.0, 30.0};
    private static final double[] KC_ALPHA05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};
    private static final double[] KC_ALPHA00 = {49.03, 39.15, 30.55, 24.16, 21.69, 20.37, 19.50, 18.84, 18.32, 17.87, 17.47, 16.78, 16.19, 15.66, 15.19, 14.76, 13.85, 13.10};

    private PrestressedBeamCalculator() {}

    // ФОРМУЛА 9.1: Граничная высота сжатой зоны
    public static double calculateXiY(double Rb, double Rp, double sigmaP2, double Rpc) {
        double omega = EPSILON_B - 0.008 * Rb;
        double sigmaP = Rp + 500.0 - sigmaP2;
        return omega / (1.0 + (sigmaP / Rpc) * (1.0 - omega / 1.1));
    }

    // ФОРМУЛА 9.4: Напряжение в сжатой напрягаемой арматуре
    public static double calculateSigmaPc(double Rpc, double sigmaP2s) {
        return Rpc - sigmaP2s;
    }

    // ФОРМУЛА 9.6: Высота сжатой зоны (граница в плите x <= hf)
    public static double calculateXWithFlange(double Rs, double As, double Rp, double Ap,
                                              double Rsc, double As_s, double sigmaPc, double Ap_s,
                                              double Rb, double bf) {
        return (Rs * As + Rp * Ap - Rsc * As_s - sigmaPc * Ap_s) / (Rb * bf);
    }

    // ФОРМУЛА 9.3: Высота сжатой зоны (граница в ребре x > hf)
    public static double calculateXWithWeb(double Rs, double As, double Rp, double Ap,
                                           double Rsc, double As_s, double sigmaPc, double Ap_s,
                                           double Rb, double bf, double b, double hf) {
        return (Rs * As + Rp * Ap - Rsc * As_s - sigmaPc * Ap_s - Rb * (bf - b) * hf) / (Rb * b);
    }

    // ФОРМУЛА 9.5: Предельный изгибающий момент (граница в плите, кН·м)
    public static double calculateMomentWithFlange(double Rb, double bf, double x, double h01,
                                                   double Rsc, double As_s, double as_s,
                                                   double sigmaPc, double Ap_s, double ap_s) {
        double M_b = Rb * 1000.0 * bf * x * (h01 - 0.5 * x);
        double M_s = Rsc * 1000.0 * As_s * (h01 - as_s);
        double M_p = sigmaPc * 1000.0 * Ap_s * (h01 - ap_s);
        return M_b + M_s + M_p;
    }

    // ФОРМУЛА 9.2: Предельный изгибающий момент (граница в ребре, кН·м)
    public static double calculateMomentWithWeb(double Rb, double b, double x, double h01,
                                                double bf, double hf,
                                                double Rsc, double As_s, double as_s,
                                                double sigmaPc, double Ap_s, double ap_s) {
        double M_b_web = Rb * 1000.0 * b * x * (h01 - 0.5 * x);
        double M_b_flange = Rb * 1000.0 * (bf - b) * hf * (h01 - 0.5 * hf);
        double M_s = Rsc * 1000.0 * As_s * (h01 - as_s);
        double M_p = sigmaPc * 1000.0 * Ap_s * (h01 - ap_s);
        return M_b_web + M_b_flange + M_s + M_p;
    }

    // ФОРМУЛА 9.7: Предельная поперечная сила (кН)
    public static double calculateShearForce(double Rp, double sumApi, double sinAlpha,
                                             double Rs, double Asw, double c, double s,
                                             double Qb) {
        double Q_p = 0.7 * Rp * 1000.0 * sumApi * sinAlpha;
        double Q_sw = 0.8 * Rs * 1000.0 * Asw * c / s;
        return Q_p + Q_sw + Qb;
    }

    // ФОРМУЛА 9.14: Высота сжатой зоны для выносливости
    public static double calculateXForFatigue(double h, double hc) {
        return Math.max(0, h - hc);
    }

    // ФОРМУЛА 9.10: Минимальное напряжение в арматуре (МПа)
    public static double calculateSigmaInRebarMin(double sigmaP2, double sigmaP2s, double nPrime,
                                                  double Ap, double Ap_s, double h01, double xPrime, double ap_s,
                                                  double Ared, double Ired, double Mg) {
        double Np2 = sigmaP2 * Ap + sigmaP2s * Ap_s; // МПа·м²
        double Mp2 = sigmaP2 * Ap * (h01 - xPrime) - sigmaP2s * Ap_s * (xPrime - ap_s); // МПа·м³

        double sigma_p_loss = nPrime * (Np2 / Ared + (Mp2 / Ired) * (h01 - xPrime));
        double sigma_g = nPrime * (Mg / 1000.0) / Ired * (h01 - xPrime); // Mg в кН·м -> МН·м

        return sigmaP2 - sigma_p_loss + sigma_g;
    }

    // ФОРМУЛА 9.11: Максимальное напряжение в арматуре (МПа)
    public static double calculateSigmaInRebarMax(double sigmaMin, double nPrime,
                                                  double Mk, double Ired, double h01, double xPrime) {
        double sigma_k = nPrime * (Mk / 1000.0) / Ired * (h01 - xPrime);
        return sigmaMin + sigma_k;
    }

    // ФОРМУЛА 9.12: Минимальное напряжение в бетоне (МПа)
    public static double calculateSigmaInConcreteMin(double sigmaP2, double Ap, double sigmaP2s, double Ap_s,
                                                     double h01, double xPrime, double ap_s,
                                                     double Ared, double Ired, double Mg) {
        double Np2 = sigmaP2 * Ap + sigmaP2s * Ap_s;
        double Mp2 = sigmaP2 * Ap * (h01 - xPrime) - sigmaP2s * Ap_s * (xPrime - ap_s);

        double sigma_p = Np2 / Ared + (Mp2 / Ired) * xPrime;
        double sigma_g = (Mg / 1000.0) / Ired * xPrime;

        return sigma_p - sigma_g;
    }

    // ФОРМУЛА 9.13: Максимальное напряжение в бетоне (МПа)
    public static double calculateSigmaInConcreteMax(double sigmaConcreteMin, double Mk, double Ired, double xPrime) {
        double sigma_k = (Mk / 1000.0) / Ired * xPrime;
        return sigmaConcreteMin + sigma_k;
    }

    // ФОРМУЛА 9.9: Асимметрия цикла
    public static double calculateRho(double sigmaMin, double sigmaMax) {
        if (Math.abs(sigmaMax) < 1e-6) return 0.0;
        return sigmaMin / sigmaMax;
    }

    // ФОРМУЛА 9.8: Расчетное сопротивление на выносливость
    public static double calculateRpf(double epsilonRo, double Rp) {
        return epsilonRo * Rp;
    }

    // ФОРМУЛА 9.15: Момент от временной нагрузки (кН·м)
    public static double calculateMk(double Omega, double epsilon, double k, double Theta) {
        return Omega * epsilon * k * Theta;
    }

    public static double getEpsilonRo(double rho) {
        double absRho = Math.abs(rho);
        if (absRho <= 0.1) return 1.00;
        if (absRho <= 0.2) return 1.06;
        if (absRho <= 0.3) return 1.10;
        if (absRho <= 0.4) return 1.15;
        if (absRho <= 0.5) return 1.20;
        return 1.24;
    }

    public static double getStandardLoad(double spanLength, double alpha) {
        double[] yPoints = (alpha >= 0.5) ? KC_ALPHA05 : KC_ALPHA00;
        return Interpolation.linear(KC_X_POINTS, yPoints, spanLength);
    }

    // ПОДРОБНЫЙ ОТЧЕТ СО ВСЕМИ 15 ФОРМУЛАМИ
    public static void printReport(BridgeContext ctx, double Rb, double Rs, double Rsc, double Rp, double Rpc,
                                   double sigmaP2, double sigmaP2s, double As, double Ap, double As_s, double Ap_s,
                                   double b, double bf, double hf, double h, double h01, double as_s, double ap_s,
                                   double Mp, double Mk, double epsilonM, double OmegaM, double Theta, double nPrime,
                                   double Ared, double Ired, double x, double M, double xiY, double sigmaPc,
                                   double k_moment, double Q_pred, double Qp, double k_shear, double k_result,
                                   double hc, double xPrime, double Rpf, double rho,
                                   double sigmaRebarMin, double sigmaRebarMax,
                                   double sigmaConcreteMin, double sigmaConcreteMax) {

        System.out.println("================================================================================");
        System.out.println(" РАСЧЕТ ГРУЗОПОДЪЕМНОСТИ БАЛКИ С НАПРЯГАЕМОЙ АРМАТУРОЙ (РАЗДЕЛ 9)");
        System.out.println("================================================================================");

        System.out.println("\n[1. Исходные параметры]");
        System.out.printf("   Пролет l = %.2f м, h = %.2f м, b = %.2f м, bf = %.2f м, hf = %.2f м, h01 = %.3f м%n", ctx.spanLength, h, b, bf, hf, h01);
        System.out.printf("   Rb = %.1f МПа, Rs = %.1f МПа, Rp = %.1f МПа, Rpc = %.1f МПа%n", Rb, Rs, Rp, Rpc);
        System.out.printf("   As = %.4f м², Ap = %.4f м², As' = %.4f м², Ap' = %.4f м²%n", As, Ap, As_s, Ap_s);
        System.out.printf("   Преднапряжение: σp2 = %.1f МПа, σ'p2 = %.1f МПа%n", sigmaP2, sigmaP2s);
        System.out.printf("   Ared = %.4f м², Ired = %.6f м⁴, n' = %.1f%n", Ared, Ired, nPrime);

        System.out.println("\n[Формула 9.1] Граничная высота сжатой зоны (ξ_y)");
        System.out.printf("   ξ_y = %.4f%n", xiY);

        System.out.println("\n[Формула 9.4] Напряжение в сжатой напрягаемой арматуре (σ_pc)");
        System.out.printf("   σ_pc = %.1f МПа%n", sigmaPc);

        System.out.println("\n[Формулы 9.3 / 9.6] Высота сжатой зоны бетона (x)");
        System.out.printf("   Рассчитанное x = %.4f м (Предельное x_lim = %.4f м)%n", x, xiY * h01);

        System.out.println("\n[Формулы 9.2 / 9.5] Предельный изгибающий момент (M)");
        System.out.printf("   M_pred = %.2f кН·м%n", M);

        System.out.println("\n[Формула 7.21] Изгибающий момент от постоянных нагрузок (Mp)");
        System.out.printf("   Mp = %.2f кН·м%n", Mp);

        System.out.println("\n[Формула 7.20] Допускаемая нагрузка по изгибающему моменту (k_moment)");
        System.out.printf("   k_moment = %.2f кН/м%n", k_moment);

        System.out.println("\n[Формула 9.7] Предельная поперечная сила (Q_pred)");
        System.out.printf("   Q_pred = %.2f кН%n", Q_pred);

        System.out.println("\n[Формула 7.25] Допускаемая нагрузка по поперечной силе (k_shear)");
        System.out.printf("   k_shear = %.2f кН/м%n", k_shear);

        System.out.println("\n[Итоговая нагрузка для расчета выносливости]");
        System.out.printf("   k_result = min(k_moment, k_shear) = %.2f кН/м%n", k_result);

        System.out.println("\n[Формула 9.15] Момент от временной нагрузки (Mk)");
        System.out.printf("   Mk = Ω_M · εM · k_result · Θ = %.2f кН·м%n", Mk);

        System.out.println("\n[Формула 9.14] Высота сжатой зоны при расчете на выносливость (x')");
        System.out.printf("   x' = %.3f м%n", xPrime);

        System.out.println("\n[Формула 9.10] Минимальное напряжение в растянутой арматуре (σ_s,min)");
        System.out.printf("   σ_s,min = %.2f МПа%n", sigmaRebarMin);

        System.out.println("\n[Формула 9.11] Максимальное напряжение в растянутой арматуре (σ_s,max)");
        System.out.printf("   σ_s,max = %.2f МПа%n", sigmaRebarMax);

        System.out.println("\n[Формула 9.12] Минимальное напряжение в бетоне сжатой зоны (σ_b,min)");
        System.out.printf("   σ_b,min = %.2f МПа%n", sigmaConcreteMin);

        System.out.println("\n[Формула 9.13] Максимальное напряжение в бетоне сжатой зоны (σ_b,max)");
        System.out.printf("   σ_b,max = %.2f МПа%n", sigmaConcreteMax);

        System.out.println("\n[Формула 9.9] Коэффициент асимметрии цикла (ρ)");
        System.out.printf("   ρ = %.3f%n", rho);

        System.out.println("\n[Формула 9.8] Сопротивление арматуры на выносливость (R_pf)");
        System.out.printf("   R_pf = %.1f МПа%n", Rpf);

        System.out.println("\n================================================================================");
        System.out.printf(" ИТОГОВАЯ ДОПУСКАЕМАЯ НАГРУЗКА: k = %.2f кН/м%n", k_result);
        System.out.println("================================================================================\n");
    }
}