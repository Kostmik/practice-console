package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;
import org.example.util.Interpolation;

public class SlabFatigueCalculator {

    // Таблица для коэффициента εb (бетон) по п. 5.1.1
    private static final double[] RHO_B_POINTS = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
    private static final double[] EPS_B_POINTS = {1.00, 1.06, 1.10, 1.15, 1.20, 1.24};

    // Таблица 5.3 для коэффициента ερs (гладкая арматура)
    private static final double[] RHO_S_SMOOTH_POINTS = {0, 0.1, 0.2, 0.3, 0.35, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0};
    private static final double[] EPS_S_SMOOTH_POINTS = {0.81, 0.85, 0.89, 0.97, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    // Таблица 5.3 для коэффициента ερs (периодическая арматура)
    private static final double[] RHO_S_RIBBED_POINTS = {0, 0.1, 0.2, 0.3, 0.35, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0};
    private static final double[] EPS_S_RIBBED_POINTS = {0.67, 0.70, 0.74, 0.81, 0.83, 0.87, 0.94, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    public static void calculateAndPrintReport(BridgeContext ctx) {

        double b = 1.0; // Расчетная ширина плиты, м
        double h0 = ctx.slabHeight - ctx.as_tensile;

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Рабочая высота сечения h0 = %.3f м%n", h0);
        System.out.printf("   Площадь растянутой арматуры As = %.4f м²%n", ctx.As_tensile);
        System.out.printf("   Площадь сжатой арматуры As' = %.4f м²%n", ctx.As_compressive);

        // =====================================================================
        // 1. КОЭФФИЦИЕНТ Θ (ПРИЛОЖЕНИЕ 3)
        // =====================================================================
        System.out.println("\n[2. Коэффициент уменьшения динамического воздействия Θ (Приложение 3)]");

        double mu0_slab = 10.0; // Для hb=0.25, звеньевой путь
        double lambda_slab = 2.0;
        double mu_slab_fatigue = mu0_slab / (20.0 + lambda_slab);

        ctx.Theta_slab = (1.0 + (2.0/3.0) * mu_slab_fatigue) / (1.0 + mu_slab_fatigue);
        System.out.printf("   μ₀ = %.1f (для hb=%.2f м, звеньевой путь)%n", mu0_slab, ctx.ballastThickness);
        System.out.printf("   Θ = (1 + (2/3)·μ₀) / (1 + μ₀) = (1 + 0.667·%.3f) / (1 + %.3f) = %.3f%n",
                mu_slab_fatigue, mu_slab_fatigue, ctx.Theta_slab);

        // =====================================================================
        // 2. ВЫСОТА СЖАТОЙ ЗОНЫ x' (ф. 7.39)
        // =====================================================================
        System.out.println("\n[3. Высота сжатой зоны x' (ф. 7.39)]");

        double nPrime = ctx.nPrime;
        double term1 = nPrime * (ctx.As_tensile + ctx.As_compressive) / b;
        double term2 = 2.0 * (nPrime * ctx.As_tensile * h0 + nPrime * ctx.As_compressive * ctx.as_compressive) / b;

        ctx.x_prime_slab = -term1 + Math.sqrt(term1 * term1 + term2);
        System.out.printf("   n' = %.1f%n", nPrime);
        System.out.printf("   x' = -n'(As+As')/b + √[(n'(As+As')/b)² + 2(n'As·h0 + n'As'·as')/b]%n");
        System.out.printf("   x' = -%.1f·(%.4f+%.4f)/%.1f + √[(%.1f·(%.4f+%.4f)/%.1f)² + 2·(%.1f·%.4f·%.3f + %.1f·%.4f·%.3f)/%.1f]%n",
                nPrime, ctx.As_tensile, ctx.As_compressive, b,
                nPrime, ctx.As_tensile, ctx.As_compressive, b,
                nPrime, ctx.As_tensile, h0, nPrime, ctx.As_compressive, ctx.as_compressive, b);
        System.out.printf("   x' = %.4f м%n", ctx.x_prime_slab);

        // =====================================================================
        // 3. ПРИВЕДЕННЫЙ МОМЕНТ ИНЕРЦИИ I_red (ф. 7.38)
        // =====================================================================
        System.out.println("\n[4. Приведенный момент инерции I_red (ф. 7.38)]");

        ctx.I_red_slab = b * Math.pow(ctx.x_prime_slab, 3) / 3.0
                + nPrime * ctx.As_tensile * Math.pow(h0 - ctx.x_prime_slab, 2)
                + nPrime * ctx.As_compressive * Math.pow(ctx.x_prime_slab - ctx.as_compressive, 2);
        System.out.printf("   I_red = b·(x')³/3 + n'·As·(h0-x')² + n'·As'·(x'-as')²%n");
        System.out.printf("   I_red = %.1f·(%.4f)³/3 + %.1f·%.4f·(%.3f-%.4f)² + %.1f·%.4f·(%.4f-%.3f)²%n",
                b, ctx.x_prime_slab, nPrime, ctx.As_tensile, h0, ctx.x_prime_slab,
                nPrime, ctx.As_compressive, ctx.x_prime_slab, ctx.as_compressive);
        System.out.printf("   I_red = %.6f м⁴%n", ctx.I_red_slab);

        // =====================================================================
        // 4. МОМЕНТ ОТ ПОСТОЯННЫХ НАГРУЗОК (ф. 7.10 при np=np'=1)
        // =====================================================================
        System.out.println("\n[5. Момент от постоянных нагрузок Mp (ф. 7.10 при np=np'=1)]");

        double Mp_fatigue = (ctx.ppSlab + ctx.pbSlab) * Math.pow(ctx.lp, 2) / 8.0;
        System.out.printf("   Mp = (pp + pb)·lp²/8 = (%.2f + %.2f)·%.2f²/8 = %.3f кН·м%n",
                ctx.ppSlab, ctx.pbSlab, ctx.lp, Mp_fatigue);

        // =====================================================================
        // 5. МОМЕНТ ОТ ВРЕМЕННОЙ НАГРУЗКИ Mk (для определения ρb)
        // =====================================================================
        System.out.println("\n[6. Момент от временной нагрузки Mk (для определения асимметрии)]");

        double eta_M = 1.1; // Коэффициент неравномерности для монолитного участка
        double k_prime = ctx.k_monolithic; // Минимальная допускаемая нагрузка по прочности

        // Исправленная формула, вытекающая из ф. 7.36
        double Mk = k_prime * ctx.Theta_slab * eta_M * b * Math.pow(ctx.lp, 2) / (8.0 * ctx.l0);
        System.out.printf("   k' = %.1f кН/м (из расчета на прочность)%n", k_prime);
        System.out.printf("   Mk = k'·Θ·ηM·b·lp² / (8·l0) = %.1f·%.3f·%.2f·%.1f·%.2f² / (8·%.2f) = %.2f кН·м%n",
                k_prime, ctx.Theta_slab, eta_M, b, ctx.lp, ctx.l0, Mk);

        // =====================================================================
        // 6. АСИММЕТРИЯ ЦИКЛА НАПРЯЖЕНИЙ
        // =====================================================================
        System.out.println("\n[7. Асимметрия цикла напряжений]");

        // Для бетона (ф. 5.4)
        ctx.rho_b_slab = Mp_fatigue / (Mp_fatigue + Mk);
        System.out.printf("   Для бетона: ρb = Mp/(Mp+Mk) = %.3f/(%.3f+%.2f) = %.3f (ф. 5.4)%n",
                Mp_fatigue, Mp_fatigue, Mk, ctx.rho_b_slab);

        // Для арматуры (п. 5.2.2)
        if (ctx.rho_b_slab <= 0.2) {
            ctx.rho_s_slab = 0.3;
            System.out.printf("   Для арматуры: ρb ≤ 0.2, принимаем ρ = 0.3%n", ctx.rho_s_slab);
        } else if (ctx.rho_b_slab <= 0.75) {
            ctx.rho_s_slab = 0.15 + 0.8 * ctx.rho_b_slab;
            System.out.printf("   Для арматуры: ρ = 0.15 + 0.8·ρb = 0.15 + 0.8·%.3f = %.3f%n",
                    ctx.rho_b_slab, ctx.rho_s_slab);
        } else {
            ctx.rho_s_slab = ctx.rho_b_slab;
            System.out.printf("   Для арматуры: ρb > 0.75, принимаем ρ = ρb = %.3f%n", ctx.rho_s_slab);
        }

        // =====================================================================
        // 7. КОЭФФИЦИЕНТЫ εb И ερs
        // =====================================================================
        System.out.println("\n[8. Коэффициенты εb и ερs (Таблица 5.3)]");

        double eps_b = Interpolation.linear(RHO_B_POINTS, EPS_B_POINTS, Math.abs(ctx.rho_b_slab));
        System.out.printf("   εb = %.2f (по интерполяции для |ρb| = %.3f)%n", eps_b, Math.abs(ctx.rho_b_slab));

        double eps_s;
        if (ctx.rebarType != null && ctx.rebarType.equals("Гладкая (А240)")) {
            eps_s = Interpolation.linear(RHO_S_SMOOTH_POINTS, EPS_S_SMOOTH_POINTS, ctx.rho_s_slab);
            System.out.printf("   ερs = %.2f (гладкая арматура, по интерполяции для ρ = %.3f)%n", eps_s, ctx.rho_s_slab);
        } else {
            eps_s = Interpolation.linear(RHO_S_RIBBED_POINTS, EPS_S_RIBBED_POINTS, ctx.rho_s_slab);
            System.out.printf("   ερs = %.2f (периодическая арматура, по интерполяции для ρ = %.3f)%n", eps_s, ctx.rho_s_slab);
        }

        // =====================================================================
        // 8. РАСЧЕТНЫЕ СОПРОТИВЛЕНИЯ НА ВЫНОСЛИВОСТЬ
        // =====================================================================
        System.out.println("\n[9. Расчетные сопротивления на выносливость]");

        ctx.Rbf = 0.6 * eps_b * ctx.Rb;
        System.out.printf("   Rbf = 0.6·εb·Rb = 0.6·%.2f·%.2f = %.2f МПа (ф. 5.1)%n", eps_b, ctx.Rb, ctx.Rbf);

        ctx.Rsf = eps_s * ctx.Rs;
        System.out.printf("   Rsf = ερs·Rs = %.2f·%.0f = %.1f МПа (ф. 5.2)%n", eps_s, ctx.Rs, ctx.Rsf);

        // =====================================================================
        // 9. ДОПУСКАЕМАЯ НАГРУЗКА ПО ВЫНОСЛИВОСТИ БЕТОНА (ф. 7.36)
        // =====================================================================
        System.out.println("\n[10. Допускаемая нагрузка по выносливости бетона (ф. 7.36)]");

        // ИСПРАВЛЕНО: убран лишний множитель A_I *
        ctx.k_fatigue_slab_concrete = 8.0 * ctx.l0 / (ctx.Theta_slab * eta_M * b * Math.pow(ctx.lp, 2))
                * (ctx.Rbf * 1000.0 * ctx.I_red_slab / ctx.x_prime_slab - Mp_fatigue);
        System.out.printf("   k = 8·l0/(Θ·ηM·b·lp²)·(Rbf·Ired/x' - Mp)%n");
        System.out.printf("   k = 8·%.2f/(%.3f·%.2f·%.1f·%.2f²)·(%.2f·1000·%.6f/%.4f - %.3f)%n",
                ctx.l0, ctx.Theta_slab, eta_M, b, ctx.lp,
                ctx.Rbf, ctx.I_red_slab, ctx.x_prime_slab, Mp_fatigue);
        System.out.printf("   k = %.1f кН/м%n", ctx.k_fatigue_slab_concrete);

        double kc = getKcByBallast(ctx.ballastThickness);
        double mu_slab = ctx.dynamicCoeffSlab != null ? ctx.dynamicCoeffSlab : 1.5;
        ctx.K_fatigue_slab_concrete = ctx.k_fatigue_slab_concrete / (kc * mu_slab);
        System.out.printf("   Класс K = k/(kc·(1+μ)) = %.1f/(%.1f·%.2f) = %.2f (ф. 4.1)%n",
                ctx.k_fatigue_slab_concrete, kc, mu_slab, ctx.K_fatigue_slab_concrete);

        // =====================================================================
        // 10. ДОПУСКАЕМАЯ НАГРУЗКА ПО ВЫНОСЛИВОСТИ АРМАТУРЫ (ф. 7.37)
        // =====================================================================
        System.out.println("\n[11. Допускаемая нагрузка по выносливости арматуры (ф. 7.37)]");

        // ИСПРАВЛЕНО: убран лишний множитель A_I *
        ctx.k_fatigue_slab_rebar = 8.0 * ctx.l0 / (ctx.Theta_slab * eta_M * b * Math.pow(ctx.lp, 2))
                * (ctx.Rsf * 1000.0 * ctx.I_red_slab / (nPrime * (h0 - ctx.x_prime_slab)) - Mp_fatigue);
        System.out.printf("   k = 8·l0/(Θ·ηM·b·lp²)·(Rsf·Ired/(n'·(h0-x')) - Mp)%n");
        System.out.printf("   k = 8·%.2f/(%.3f·%.2f·%.1f·%.2f²)·(%.1f·1000·%.6f/(%.1f·(%.3f-%.4f)) - %.3f)%n",
                ctx.l0, ctx.Theta_slab, eta_M, b, ctx.lp,
                ctx.Rsf, ctx.I_red_slab, nPrime, h0, ctx.x_prime_slab, Mp_fatigue);
        System.out.printf("   k = %.1f кН/м%n", ctx.k_fatigue_slab_rebar);

        ctx.K_fatigue_slab_rebar = ctx.k_fatigue_slab_rebar / (kc * mu_slab);
        System.out.printf("   Класс K = k/(kc·(1+μ)) = %.1f/(%.1f·%.2f) = %.2f (ф. 4.1)%n",
                ctx.k_fatigue_slab_rebar, kc, mu_slab, ctx.K_fatigue_slab_rebar);

        // =====================================================================
        // ИТОГ
        // =====================================================================
        System.out.println("\n============================================================");
        System.out.println("============================================================");
        System.out.printf("   Класс по выносливости бетона:    K = %.2f%n", ctx.K_fatigue_slab_concrete);
        System.out.printf("   Класс по выносливости арматуры:  K = %.2f%n", ctx.K_fatigue_slab_rebar);
        double minK_fatigue = Math.min(ctx.K_fatigue_slab_concrete, ctx.K_fatigue_slab_rebar);
        System.out.printf("   МИНИМАЛЬНЫЙ КЛАСС ПО ВЫНОСЛИВОСТИ: K = %.2f%n", minK_fatigue);
        System.out.println("============================================================\n");
    }

    private static double getKcByBallast(double hb) {
        if (hb < 0.30) return 19.1;
        if (hb < 0.40) return 19.0;
        if (hb < 0.50) return 18.7;
        if (hb < 0.60) return 18.4;
        if (hb < 0.70) return 18.3;
        if (hb < 0.80) return 18.2;
        if (hb < 0.90) return 18.2;
        if (hb < 1.00) return 18.1;
        return 18.1;
    }
}