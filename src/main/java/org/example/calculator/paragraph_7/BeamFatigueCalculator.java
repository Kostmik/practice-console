package org.example.calculator;

import org.example.context.BridgeContext;
import org.example.util.Interpolation;

public class BeamFatigueCalculator {

    private static final double[] RHO_B_POINTS = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
    private static final double[] EPS_B_POINTS = {1.00, 1.06, 1.10, 1.15, 1.20, 1.24};

    private static final double[] RHO_S_SMOOTH_POINTS = {0, 0.1, 0.2, 0.3, 0.35, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0};
    private static final double[] EPS_S_SMOOTH_POINTS = {0.81, 0.85, 0.89, 0.97, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    private static final double[] RHO_S_RIBBED_POINTS = {0, 0.1, 0.2, 0.3, 0.35, 0.4, 0.5, 0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0};
    private static final double[] EPS_S_RIBBED_POINTS = {0.67, 0.70, 0.74, 0.81, 0.83, 0.87, 0.94, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    public static void calculateAndPrintReport(BridgeContext ctx) {
        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ ГЛАВНОЙ БАЛКИ НА ВЫНОСЛИВОСТЬ [п. 7.3.2]");
        System.out.println("============================================================");

        double h0 = ctx.beamHeight - ctx.as_beam_tensile;
        double nPrime = ctx.nPrime;

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Рабочая высота сечения h0 = %.3f м%n", h0);
        System.out.printf("   Ширина ребра b = %.2f м%n", ctx.beamWidth);
        System.out.printf("   Расчетная ширина плиты bf = %.2f м%n", ctx.bf);
        System.out.printf("   Приведенная толщина плиты hf = %.3f м%n", ctx.hf);
        System.out.printf("   Площадь растянутой арматуры As = %.4f м²%n", ctx.As_beam_tensile);
        System.out.printf("   Площадь сжатой арматуры As' = %.4f м²%n", ctx.As_beam_compressive);

        System.out.println("\n[2. Коэффициент уменьшения динамического воздействия Θ (Приложение 3)]");

        double mu0_beam = 10.0;
        double lambda_beam = ctx.spanLength;
        double mu_beam_fatigue = mu0_beam / (20.0 + lambda_beam);

        ctx.Theta_beam = (1.0 + (2.0/3.0) * mu_beam_fatigue) / (1.0 + mu_beam_fatigue);
        System.out.printf("   μ₀ = %.1f (для hb=%.2f м, звеньевой путь)%n", mu0_beam, ctx.ballastThickness);
        System.out.printf("   Θ = (1 + (2/3)·μ₀) / (1 + μ₀) = (1 + 0.667·%.3f) / (1 + %.3f) = %.3f%n",
                mu_beam_fatigue, mu_beam_fatigue, ctx.Theta_beam);

        System.out.println("\n[3. Высота сжатой зоны x' (ф. 7.43)]");

        double term1_rect = nPrime * (ctx.As_beam_tensile + ctx.As_beam_compressive) / ctx.bf;
        double term2_rect = 2.0 * (nPrime * ctx.As_beam_tensile * h0 + nPrime * ctx.As_beam_compressive * ctx.as_beam_compressive) / ctx.bf;
        double x_prime_rect = -term1_rect + Math.sqrt(term1_rect * term1_rect + term2_rect);

        System.out.printf("   n' = %.1f%n", nPrime);

        if (x_prime_rect <= ctx.hf) {
            ctx.x_prime_beam = x_prime_rect;
            System.out.printf("   Нейтральная ось проходит в полке (x' ≤ hf = %.3f м)%n", ctx.hf);
            System.out.printf("   x' = -n'(As+As')/bf + √[(n'(As+As')/bf)² + 2(n'As·h0 + n'As'·as')/bf]%n");
            System.out.printf("   x' = %.4f м%n", ctx.x_prime_beam);
        } else {
            System.out.printf("   Нейтральная ось проходит в ребре (x' > hf = %.3f м)%n", ctx.hf);

            // ИСПРАВЛЕНО: Правильное квадратное уравнение для таврового сечения
            // Условие равновесия: b·x'²/2 + (bf-b)·hf·(x'-hf/2) = n'·As·(h0-x') + n'·As'·(x'-as')
            // После преобразований: b·x'² + 2·[(bf-b)·hf + n'·As - n'·As']·x' - (bf-b)·hf² - 2·n'·As·h0 + 2·n'·As'·as' = 0

            double a = ctx.beamWidth;
            double b_coeff = 2.0 * ((ctx.bf - ctx.beamWidth) * ctx.hf + nPrime * ctx.As_beam_tensile - nPrime * ctx.As_beam_compressive);
            double c = -(ctx.bf - ctx.beamWidth) * ctx.hf * ctx.hf - 2.0 * nPrime * ctx.As_beam_tensile * h0 + 2.0 * nPrime * ctx.As_beam_compressive * ctx.as_beam_compressive;

            double discriminant = b_coeff * b_coeff - 4.0 * a * c;
            ctx.x_prime_beam = (-b_coeff + Math.sqrt(discriminant)) / (2.0 * a);

            System.out.printf("   Решаем квадратное уравнение для таврового сечения:%n");
            System.out.printf("   a = b = %.2f м%n", a);
            System.out.printf("   b = 2·[(bf-b)·hf + n'·As - n'·As'] = %.4f%n", b_coeff);
            System.out.printf("   c = -(bf-b)·hf² - 2·n'·As·h0 + 2·n'·As'·as' = %.4f%n", c);
            System.out.printf("   x' = (-b + √(b²-4ac)) / (2a) = %.4f м%n", ctx.x_prime_beam);
        }

        System.out.println("\n[4. Приведенный момент инерции I_red (ф. 7.42)]");

        if (ctx.x_prime_beam <= ctx.hf) {
            ctx.I_red_beam = ctx.bf * Math.pow(ctx.x_prime_beam, 3) / 3.0
                    + nPrime * ctx.As_beam_tensile * Math.pow(h0 - ctx.x_prime_beam, 2)
                    + nPrime * ctx.As_beam_compressive * Math.pow(ctx.x_prime_beam - ctx.as_beam_compressive, 2);
            System.out.printf("   I_red = bf·(x')³/3 + n'·As·(h0-x')² + n'·As'·(x'-as')²%n");
        } else {
            // ИСПРАВЛЕНО: Правильная формула для таврового сечения
            ctx.I_red_beam = ctx.beamWidth * Math.pow(ctx.x_prime_beam, 3) / 3.0
                    + (ctx.bf - ctx.beamWidth) * ctx.hf * Math.pow(ctx.x_prime_beam - ctx.hf / 2.0, 2)
                    + nPrime * ctx.As_beam_tensile * Math.pow(h0 - ctx.x_prime_beam, 2)
                    + nPrime * ctx.As_beam_compressive * Math.pow(ctx.x_prime_beam - ctx.as_beam_compressive, 2);
            System.out.printf("   I_red = b·(x')³/3 + (bf-b)·hf·(x'-hf/2)² + n'·As·(h0-x')² + n'·As'·(x'-as')²%n");
        }

        System.out.printf("   I_red = %.6f м%n", ctx.I_red_beam);

        System.out.println("\n[5. Момент от постоянных нагрузок Mp (при np=np'=1)]");

        double Mp_fatigue = (ctx.ppBeam + ctx.pbBeam) * ctx.Omega_M;
        System.out.printf("   Mp = (pp + pb)·Ω = (%.2f + %.2f)·%.2f = %.2f кН·м%n",
                ctx.ppBeam, ctx.pbBeam, ctx.Omega_M, Mp_fatigue);

        System.out.println("\n[6. Момент от временной нагрузки Mk]");

        double epsilonM = ctx.epsilonM_Beam1 != null ? ctx.epsilonM_Beam1 : 0.5;
        double k_prime = ctx.k_beam_moment;

        double Mk = k_prime * ctx.Theta_beam * epsilonM * ctx.Omega_M;
        System.out.printf("   k' = %.1f кН/м (из расчета на прочность)%n", k_prime);
        System.out.printf("   Mk = k'·Θ·εM·Ω = %.1f·%.3f·%.3f·%.2f = %.2f кН·м%n",
                k_prime, ctx.Theta_beam, epsilonM, ctx.Omega_M, Mk);

        System.out.println("\n[7. Асимметрия цикла напряжений]");

        ctx.rho_b_beam = Mp_fatigue / (Mp_fatigue + Mk);
        System.out.printf("   Для бетона: ρb = Mp/(Mp+Mk) = %.2f/(%.2f+%.2f) = %.3f (ф. 5.4)%n",
                Mp_fatigue, Mp_fatigue, Mk, ctx.rho_b_beam);

        if (ctx.rho_b_beam <= 0.2) {
            ctx.rho_s_beam = 0.3;
            System.out.printf("   Для арматуры: ρb ≤ 0.2, принимаем  = 0.3%n");
        } else if (ctx.rho_b_beam <= 0.75) {
            ctx.rho_s_beam = 0.15 + 0.8 * ctx.rho_b_beam;
            System.out.printf("   Для арматуры: ρ = 0.15 + 0.8·ρb = 0.15 + 0.8·%.3f = %.3f%n",
                    ctx.rho_b_beam, ctx.rho_s_beam);
        } else {
            ctx.rho_s_beam = ctx.rho_b_beam;
            System.out.printf("   Для арматуры: ρb > 0.75, принимаем ρ = ρb = %.3f%n", ctx.rho_s_beam);
        }

        System.out.println("\n[8. Коэффициенты εb и ερs (Таблица 5.3)]");

        double eps_b = Interpolation.linear(RHO_B_POINTS, EPS_B_POINTS, Math.abs(ctx.rho_b_beam));
        System.out.printf("   εb = %.2f (по интерполяции для |ρb| = %.3f)%n", eps_b, Math.abs(ctx.rho_b_beam));

        double eps_s;
        if (ctx.rebarType != null && ctx.rebarType.equals("Гладкая (А240)")) {
            eps_s = Interpolation.linear(RHO_S_SMOOTH_POINTS, EPS_S_SMOOTH_POINTS, ctx.rho_s_beam);
            System.out.printf("   ερs = %.2f (гладкая арматура, по интерполяции для ρ = %.3f)%n", eps_s, ctx.rho_s_beam);
        } else {
            eps_s = Interpolation.linear(RHO_S_RIBBED_POINTS, EPS_S_RIBBED_POINTS, ctx.rho_s_beam);
            System.out.printf("   ερs = %.2f (периодическая арматура, по интерполяции для ρ = %.3f)%n", eps_s, ctx.rho_s_beam);
        }

        System.out.println("\n[9. Расчетные сопротивления на выносливость]");

        ctx.Rbf_beam = 0.6 * eps_b * ctx.Rb;
        System.out.printf("   Rbf = 0.6·εb·Rb = 0.6·%.2f·%.2f = %.2f МПа (ф. 5.1)%n", eps_b, ctx.Rb, ctx.Rbf_beam);

        ctx.Rsf_beam = eps_s * ctx.Rs;
        System.out.printf("   Rsf = ερs·Rs = %.2f·%.0f = %.1f МПа (ф. 5.2)%n", eps_s, ctx.Rs, ctx.Rsf_beam);

        System.out.println("\n[10. Допускаемая нагрузка по выносливости бетона (ф. 7.40)]");

        ctx.k_fatigue_beam_concrete = (ctx.Rbf_beam * 1000.0 * ctx.I_red_beam / ctx.x_prime_beam - Mp_fatigue)
                / (ctx.Theta_beam * epsilonM * ctx.Omega_M);
        System.out.printf("   k = (Rbf·Ired/x' - Mp) / (Θ·εM·Ω)%n");
        System.out.printf("   k = (%.2f·1000·%.6f/%.4f - %.2f) / (%.3f·%.3f·%.2f)%n",
                ctx.Rbf_beam, ctx.I_red_beam, ctx.x_prime_beam, Mp_fatigue,
                ctx.Theta_beam, epsilonM, ctx.Omega_M);
        System.out.printf("   k = %.1f кН/м%n", ctx.k_fatigue_beam_concrete);

        double kc = getKcForBeam(ctx.spanLength);
        double mu_beam = ctx.dynamicCoeffBeam != null ? ctx.dynamicCoeffBeam : 1.325;
        ctx.K_fatigue_beam_concrete = ctx.k_fatigue_beam_concrete / (kc * mu_beam);
        System.out.printf("   Класс K = k/(kc·(1+μ)) = %.1f/(%.2f·%.3f) = %.2f (ф. 4.1)%n",
                ctx.k_fatigue_beam_concrete, kc, mu_beam, ctx.K_fatigue_beam_concrete);

        System.out.println("\n[11. Допускаемая нагрузка по выносливости арматуры (ф. 7.41)]");

        ctx.k_fatigue_beam_rebar = (ctx.Rsf_beam * 1000.0 * ctx.I_red_beam / (nPrime * (h0 - ctx.x_prime_beam)) - Mp_fatigue)
                / (ctx.Theta_beam * epsilonM * ctx.Omega_M);
        System.out.printf("   k = (Rsf·Ired/(n'·(h0-x')) - Mp) / (Θ·εM·Ω)%n");
        System.out.printf("   k = (%.1f·1000·%.6f/(%.1f·(%.3f-%.4f)) - %.2f) / (%.3f·%.3f·%.2f)%n",
                ctx.Rsf_beam, ctx.I_red_beam, nPrime, h0, ctx.x_prime_beam, Mp_fatigue,
                ctx.Theta_beam, epsilonM, ctx.Omega_M);
        System.out.printf("   k = %.1f кН/м%n", ctx.k_fatigue_beam_rebar);

        ctx.K_fatigue_beam_rebar = ctx.k_fatigue_beam_rebar / (kc * mu_beam);
        System.out.printf("   Класс K = k/(kc·(1+μ)) = %.1f/(%.2f·%.3f) = %.2f (ф. 4.1)%n",
                ctx.k_fatigue_beam_rebar, kc, mu_beam, ctx.K_fatigue_beam_rebar);

        System.out.println("\n============================================================");
        System.out.println(" ИТОГОВЫЙ РЕЗУЛЬТАТ ПО ВЫНОСЛИВОСТИ ГЛАВНОЙ БАЛКИ");
        System.out.println("============================================================");
        System.out.printf("   Класс по выносливости бетона:    K = %.2f%n", ctx.K_fatigue_beam_concrete);
        System.out.printf("   Класс по выносливости арматуры:  K = %.2f%n", ctx.K_fatigue_beam_rebar);
        double minK_fatigue = Math.min(ctx.K_fatigue_beam_concrete, ctx.K_fatigue_beam_rebar);
        System.out.printf("   >>> МИНИМАЛЬНЫЙ КЛАСС ПО ВЫНОСЛИВОСТИ: K = %.2f <<<%n", minK_fatigue);
        System.out.println("============================================================\n");
    }

    private static double getKcForBeam(double lambda) {
        double[] LAMBDA_POINTS = {1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30};
        double[] KC_POINTS_ALPHA_05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};
        return Interpolation.linear(LAMBDA_POINTS, KC_POINTS_ALPHA_05, lambda);
    }
}