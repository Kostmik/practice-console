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
        double Mp2 = sigmaP2 * Ap * (h01 - xPrime) + sigmaP2s * Ap_s * (xPrime - ap_s);

        double sigma_p = Np2 / Ared + (Mp2 / Ired) * xPrime;
        double sigma_g = (Mg / 1000.0) / Ired * xPrime;

        return sigma_p + sigma_g;
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

    // =====================================================================
    // МЕТОД ВЫВОДА ОТЧЕТА (Разделы 9.1, 9.2, 9.3 по методичке)
    // =====================================================================
    public static void printReport(
        BridgeContext ctx,
        double Rb, double Rp, double Rpc,
        double sigmaP2, double sigmaP2s,
        double As, double Ap, double As_s, double Ap_s,
        double b, double bf, double hf,
        double h, double h01,
        double as_s, double ap_s,
        double Mg, double Mk,
        double epsilonM, double epsilonQ, // <-- ДОБАВЛЕНО: epsilonQ
        double OmegaM, double OmegaQ,     // <-- ДОБАВЛЕНО: OmegaQ
        double Theta, double nPrime,
        double Ared, double Ired,
        double x, double M,
        double xiY, double sigmaPc,
        double k,
        double Q_pred,
        double hc,
        double sigmaRebarMin,
        double sigmaRebarMax,
        double sigmaConcreteMin,
        double sigmaConcreteMax
    ) {
        double Rs = ctx.Rs;
        double nk = ctx.nk;
        double np = ctx.np;
        double npPrime = ctx.npPrime;

        double pp = (ctx.ppBeam != null) ? ctx.ppBeam : 34.0;
        double pb = (ctx.pbBeam != null) ? ctx.pbBeam : 20.6;
        double Mp_calc = (np * pp + npPrime * pb) * OmegaM;

        boolean boundaryInFlange = (x <= hf);
        double xPrime = h - hc;

        // Вспомогательные расчеты
        double Qp = (np * pp + npPrime * pb) * OmegaQ;
        double k_moment = Math.max(0, (M - Mp_calc) / (nk * epsilonM * OmegaM));
        // ИСПРАВЛЕНО: используем epsilonQ и OmegaQ для поперечной силы
        double k_shear = Math.max(0, (Q_pred - Qp) / (nk * epsilonQ * OmegaQ));

        // Промежуточные переменные для выносливости
        double Np2 = sigmaP2 * Ap + sigmaP2s * Ap_s;
        double Mp2 = sigmaP2 * Ap * (h01 - xPrime) + sigmaP2s * Ap_s * (xPrime - ap_s);
        double rho = (sigmaRebarMax != 0) ? sigmaRebarMin / sigmaRebarMax : 0;
        double epsilonRo = getEpsilonRo(Math.abs(rho));
        double Rpf = epsilonRo * Rp;
        double ap = h - h01;

        System.out.println("================================================================================");
        System.out.println(" РАЗДЕЛ 9. ОПРЕДЕЛЕНИЕ ГРУЗОПОДЪЕМНОСТИ ПРОЛЕТНЫХ СТРОЕНИЙ");
        System.out.println(" С НАПРЯГАЕМОЙ АРМАТУРОЙ");
        System.out.println("================================================================================");

        // =====================================================================
        // ПОДРАЗДЕЛ 9.1. РАСЧЕТ НОРМАЛЬНОГО СЕЧЕНИЯ ПО ИЗГИБАЮЩЕМУ МОМЕНТУ
        // =====================================================================
        System.out.println("\n9.1. РАСЧЕТ НОРМАЛЬНОГО СЕЧЕНИЯ ПО ИЗГИБАЮЩЕМУ МОМЕНТУ");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        System.out.println("\n1. Исходные данные");
        System.out.printf("   Высота балки h = %.3f м%n", h);
        System.out.printf("   Ширина ребра b = %.2f м%n", b);
        System.out.printf("   Расчетная ширина плиты bf = %.2f м%n", bf);
        System.out.printf("   Приведенная толщина плиты hf = %.3f м%n", hf);
        System.out.printf("   Площадь растянутой арматуры As = %.4f м²%n", As);
        System.out.printf("   Площадь сжатой арматуры As' = %.4f м²%n", As_s);
        System.out.printf("   Площадь напрягаемой арматуры Ap = %.4f м²%n", Ap);
        System.out.printf("   Площадь сжатой напрягаемой арматуры Ap' = %.4f м²%n", Ap_s);
        System.out.printf("   Рабочая высота h01 = h - ap = %.3f - %.2f = %.3f м%n", h, ap, h01);
        System.out.printf("   Rb = %.1f МПа, Rs = %.1f МПа, Rp = %.1f МПа, Rpc = %.1f МПа%n", Rb, Rs, Rp, Rpc);
        System.out.printf("   σp2 = %.1f МПа, σ'p2 = %.1f МПа%n", sigmaP2, sigmaP2s);
        System.out.printf("   Ared = %.4f м², Ired = %.6f м⁴, n' = %.1f%n", Ared, Ired, nPrime);

        System.out.println("\n2. Граничная высота сжатой зоны (ф. 9.1)");
        System.out.println("   ξ_y = (0.85 - 0.008·Rb) / (1 + (Rp + 500 - σp2)/Rpc · (1 - (0.85 - 0.008·Rb)/1.1))");
        double numXi = 0.85 - 0.008 * Rb;
        double ratio1 = (Rp + 500 - sigmaP2) / Rpc;
        double ratio2 = 1 - numXi / 1.1;
        double denXi = 1 + ratio1 * ratio2;
        System.out.printf("   ξ_y = (%.4f) / (1 + %.4f · %.4f) = %.4f / %.4f = %.3f%n", numXi, ratio1, ratio2, numXi, denXi, xiY);

        System.out.println("\n3. Напряжение в арматуре сжатой зоны (ф. 9.4)");
        System.out.println("   σ_pc = Rpc - σ'p2");
        System.out.printf("   σ_pc = %.1f - %.1f = %.1f МПа%n", Rpc, sigmaP2s, sigmaPc);

        System.out.println("\n4. Высота сжатой зоны бетона");
        if (boundaryInFlange) {
            System.out.println("   Проверяем положение границы сжатой зоны:");
            System.out.println("   x = (Rs·As + Rp·Ap - Rsc·As' - σ_pc·Ap') / (Rb·bf)");
            double numX = Rs * As + Rp * Ap - Rs * As_s - sigmaPc * Ap_s;
            double denX = Rb * bf;
            System.out.printf("   x = (%.1f·%.4f + %.1f·%.4f - %.1f·%.4f - %.1f·%.4f) / (%.1f·%.2f) = %.4f м%n",
                Rs, As, Rp, Ap, Rs, As_s, sigmaPc, Ap_s, Rb, bf, numX/denX);
            System.out.printf("   Так как x (%.4f) ≤ hf (%.3f), граница сжатой зоны проходит в плите%n", x, hf);
            System.out.println("   Используем формулу для прямоугольного сечения (ф. 9.5)");
        } else {
            System.out.println("   Проверяем положение границы сжатой зоны:");
            System.out.println("   x = (Rs·As + Rp·Ap - Rsc·As' - σ_pc·Ap' - Rb·(bf-b)·hf) / (Rb·b)");
            double numX = Rs * As + Rp * Ap - Rs * As_s - sigmaPc * Ap_s - Rb * (bf - b) * hf;
            double denX = Rb * b;
            System.out.printf("   x = (%.1f·%.4f + %.1f·%.4f - %.1f·%.4f - %.1f·%.4f - %.1f·(%.2f-%.2f)·%.2f) / (%.1f·%.2f) = %.4f м%n",
                Rs, As, Rp, Ap, Rs, As_s, sigmaPc, Ap_s, Rb, bf, b, hf, Rb, b, numX/denX);
            System.out.printf("   Так как x (%.4f) > hf (%.3f), граница сжатой зоны проходит в ребре%n", x, hf);
            System.out.println("   Используем формулу для таврового сечения (ф. 9.2)");
        }
        System.out.printf("   Рассчитанное x = %.4f м (Предельное x_lim = ξ_y·h01 = %.3f · %.3f = %.4f м)%n", x, xiY, h01, xiY * h01);

        System.out.println("\n5. Предельный изгибающий момент");
        if (boundaryInFlange) {
            System.out.println("   M = Rb·bf·x·(h01 - 0.5x) + Rsc·As'·(h01 - as') + σ_pc·Ap'·(h01 - ap')");
            double t1 = Rb * 1000 * bf * x * (h01 - 0.5 * x);
            double t2 = Rs * 1000 * As_s * (h01 - as_s);
            double t3 = sigmaPc * 1000 * Ap_s * (h01 - ap_s);
            System.out.printf("   M = %.1f·1000·%.2f·%.4f·(%.3f - 0.5·%.4f) + %.1f·1000·%.4f·(%.3f - %.2f) + %.1f·1000·%.4f·(%.3f - %.2f)%n",
                Rb, bf, x, h01, x, Rs, As_s, h01, as_s, sigmaPc, Ap_s, h01, ap_s);
            System.out.printf("   Предельный момент M = %.2f кН·м%n", M);
        } else {
            System.out.println("   M = Rb·b·x·(h01 - 0.5x) + Rb·(bf-b)·hf·(h01 - 0.5hf) + Rsc·As'·(h01 - as') + σ_pc·Ap'·(h01 - ap')");
            double t1 = Rb * 1000 * b * x * (h01 - 0.5 * x);
            double t2 = Rb * 1000 * (bf - b) * hf * (h01 - 0.5 * hf);
            double t3 = Rs * 1000 * As_s * (h01 - as_s);
            double t4 = sigmaPc * 1000 * Ap_s * (h01 - ap_s);
            System.out.printf("   M = %.1f·1000·%.2f·%.4f·(%.3f - 0.5·%.4f) + %.1f·1000·(%.2f-%.2f)·%.2f·(%.3f - 0.5·%.2f) + %.1f·1000·%.4f·(%.3f - %.2f) + %.1f·1000·%.4f·(%.3f - %.2f)%n",
                Rb, b, x, h01, x, Rb, bf, b, hf, h01, hf, Rs, As_s, h01, as_s, sigmaPc, Ap_s, h01, ap_s);
            System.out.printf("   Предельный момент M = %.2f кН·м%n", M);
        }

        System.out.println("\n6. Момент от постоянных нагрузок (ф. 7.21)");
        System.out.printf("   Площадь линии влияния Ω = l²/8 = %.2f²/8 = %.2f м²%n", ctx.spanLength, OmegaM);
        System.out.println("   Mp = (np·pp + np'·pb)·Ω");
        System.out.printf("   Mp = (%.2f·%.2f + %.2f·%.2f)·%.2f = %.2f кН·м%n", np, pp, npPrime, pb, OmegaM, Mp_calc);

        System.out.println("\n7. Допускаемая временная нагрузка по моменту (ф. 7.20)");
        System.out.println("   k_moment = (M - Mp) / (nk·εM·Ω)");
        System.out.printf("   k_moment = (%.2f - %.2f) / (%.2f·%.3f·%.2f) = %.2f кН/м%n", M, Mp_calc, nk, epsilonM, OmegaM, k_moment);

        // =====================================================================
        // ПОДРАЗДЕЛ 9.2. РАСЧЕТ НАКЛОННОГО СЕЧЕНИЯ ПО ПОПЕРЕЧНОЙ СИЛЕ
        // =====================================================================
        System.out.println("\n9.2. РАСЧЕТ НАКЛОННОГО СЕЧЕНИЯ ПО ПОПЕРЕЧНОЙ СИЛЕ");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        System.out.println("\n1. Предельная поперечная сила (ф. 9.7)");
        System.out.println("   Q_pred = 0.7·Rp·ΣApi·sinα + 0.8·Rs·Asw·c/s + Qb");
        System.out.printf("   Q_pred = 0.7·%.1f·ΣApi·sinα + 0.8·%.1f·Asw·c/s + Qb = %.2f кН%n", Rp, Rs, Q_pred);

        System.out.println("\n2. Поперечная сила от постоянных нагрузок (ф. 7.26)");
        System.out.printf("   Площадь линии влияния Ω_Q = l/2 = %.2f/2 = %.2f м²%n", ctx.spanLength, OmegaQ);
        System.out.println("   Qp = (np·pp + np'·pb)·Ω_Q");
        System.out.printf("   Qp = (%.2f·%.2f + %.2f·%.2f)·%.2f = %.2f кН%n", np, pp, npPrime, pb, OmegaQ, Qp);

        System.out.println("\n3. Допускаемая нагрузка по поперечной силе (ф. 7.25)");
        System.out.println("   k_shear = (Q_pred - Qp) / (nk·εQ·Ω_Q)");
        // ИСПРАВЛЕНО: выводим epsilonQ и OmegaQ
        System.out.printf("   k_shear = (%.2f - %.2f) / (%.2f·%.3f·%.2f) = %.2f кН/м%n", Q_pred, Qp, nk, epsilonQ, OmegaQ, k_shear);

        System.out.println("\n4. Итоговая допускаемая нагрузка");
        System.out.printf("   k = min(k_moment, k_shear) = min(%.2f, %.2f) = %.2f кН/м%n", k_moment, k_shear, k);

        // =====================================================================
        // ПОДРАЗДЕЛ 9.3. РАСЧЕТ ПО ВЫНОСЛИВОСТИ
        // =====================================================================
        System.out.println("\n9.3. РАСЧЕТ ПО ВЫНОСЛИВОСТИ");
        System.out.println("────────────────────────────────────────────────────────────────────────────");

        System.out.println("\n1. Момент от временной нагрузки (ф. 9.15)");
        System.out.println("   Mk = Ω·εM·k·Θ");
        System.out.printf("   Mk = %.2f · %.3f · %.2f · %.3f = %.2f кН·м%n", OmegaM, epsilonM, k, Theta, Mk);

        System.out.println("\n2. Высота сжатой зоны для выносливости (ф. 9.14)");
        System.out.println("   x' = h - hc");
        System.out.printf("   x' = %.2f - %.2f = %.3f м%n", h, hc, xPrime);

        System.out.println("\n3. Минимальное напряжение в арматуре (ф. 9.10)");
        System.out.println("   σ_s,min = σp2 - n'·[Np2/Ared + (Mp2/Ired)·(h01-x')] + n'·(Mp·1000/Ired)·(h01-x')/10⁶");
        System.out.printf("   Np2 = σp2·Ap + σ'p2·Ap' = %.1f·%.4f + %.1f·%.4f = %.4f МПа·м²%n", sigmaP2, Ap, sigmaP2s, Ap_s, Np2);
        System.out.printf("   Mp2 = σp2·Ap·(h01-x') + σ'p2·Ap'·(x'-ap') = %.4f МПа·м³%n", Mp2);
        double term1_rebar = sigmaP2 - nPrime * (Np2 / Ared + (Mp2 / Ired) * (h01 - xPrime));
        double term2_rebar = nPrime * (Mg * 1000.0) / Ired * (h01 - xPrime) / 1_000_000.0;
        System.out.printf("   σ_s,min = %.2f + %.2f = %.2f МПа%n", term1_rebar, term2_rebar, sigmaRebarMin);

        System.out.println("\n4. Максимальное напряжение в арматуре (ф. 9.11)");
        System.out.println("   σ_s,max = σ_s,min + n'·(Mk·1000/Ired)·(h01-x')/10⁶");
        double term_extra_rebar = nPrime * (Mk * 1000.0) / Ired * (h01 - xPrime) / 1_000_000.0;
        System.out.printf("   σ_s,max = %.2f + %.2f = %.2f МПа%n", sigmaRebarMin, term_extra_rebar, sigmaRebarMax);

        System.out.println("\n5. Минимальное напряжение в бетоне (ф. 9.12)");
        System.out.println("   σ_b,min = Np2/Ared + (Mp2/Ired)·x' + (Mp·1000/Ired)·x'/10⁶");
        double term1_conc = Np2 / Ared;
        double term2_conc = (Mp2 / Ired) * xPrime;
        double term3_conc = (Mg * 1000.0) / Ired * xPrime / 1_000_000.0;
        System.out.printf("   σ_b,min = %.2f + %.2f + %.2f = %.2f МПа%n", term1_conc, term2_conc, term3_conc, sigmaConcreteMin);

        System.out.println("\n6. Максимальное напряжение в бетоне (ф. 9.13)");
        System.out.println("   σ_b,max = σ_b,min + (Mk·1000/Ired)·x'/10⁶");
        double term_extra_conc = (Mk * 1000.0) / Ired * xPrime / 1_000_000.0;
        System.out.printf("   σ_b,max = %.2f + %.2f = %.2f МПа%n", sigmaConcreteMin, term_extra_conc, sigmaConcreteMax);

        System.out.println("\n7. Асимметрия цикла напряжений (ф. 9.9)");
        System.out.println("   ρ = σ_s,min / σ_s,max");
        System.out.printf("   ρ = %.2f / %.2f = %.3f%n", sigmaRebarMin, sigmaRebarMax, rho);

        System.out.println("\n8. Сопротивление арматуры на выносливость (ф. 9.8)");
        System.out.println("   R_pf = ε_ρ · Rp");
        System.out.printf("   По таблице 5.3 для |ρ| = %.3f принимаем ε_ρ = %.2f%n", Math.abs(rho), epsilonRo);
        System.out.printf("   R_pf = %.2f · %.1f = %.1f МПа%n", epsilonRo, Rp, Rpf);

        System.out.println("\n================================================================================");
        System.out.println(" ИТОГОВЫЙ РЕЗУЛЬТАТ");
        System.out.println("================================================================================");
        System.out.printf("   Допускаемая временная нагрузка: k = %.2f кН/м%n", k);
        System.out.printf("   Предельный момент: M = %.2f кН·м%n", M);
        System.out.printf("   Предельная поперечная сила: Q = %.2f кН%n", Q_pred);
        System.out.printf("   Расчетное сопротивление выносливости: R_pf = %.1f МПа%n", Rpf);
        System.out.println("================================================================================\n");
    }
}