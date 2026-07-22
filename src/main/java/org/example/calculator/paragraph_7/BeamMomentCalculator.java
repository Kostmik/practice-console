package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;
import org.example.util.Interpolation;

public class BeamMomentCalculator {

    // Таблица П.1.1 - Эталонная нагрузка kc для α=0.5
    private static final double[] LAMBDA_POINTS = {1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30};
    private static final double[] KC_POINTS_ALPHA_05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};

    public static void calculateAndPrintReport(BridgeContext ctx) {

        double Rb_kPa = ctx.Rb * 1000;
        double Rs_kPa = ctx.Rs * 1000;

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Высота балки h = %.3f м%n", ctx.beamHeight);
        System.out.printf("   Ширина ребра b = %.2f м%n", ctx.beamWidth);
        System.out.printf("   Расчетная ширина плиты bf = %.2f м%n", ctx.bf);
        System.out.printf("   Приведенная толщина плиты hf = %.3f м%n", ctx.hf);
        System.out.printf("   Площадь растянутой арматуры As = %.4f м²%n", ctx.As_beam_tensile);
        System.out.printf("   Площадь сжатой арматуры As' = %.4f м²%n", ctx.As_beam_compressive);

        double h0 = ctx.beamHeight - ctx.as_beam_tensile;
        System.out.printf("   Рабочая высота h0 = h - as = %.3f - %.3f = %.3f м%n",
                ctx.beamHeight, ctx.as_beam_tensile, h0);

        System.out.println("\n[2. Высота сжатой зоны бетона]");

        double numerator = Rs_kPa * ctx.As_beam_tensile - Rs_kPa * ctx.As_beam_compressive
                - Rb_kPa * (ctx.bf - ctx.beamWidth) * ctx.hf;
        double x = numerator / (Rb_kPa * ctx.beamWidth);

        System.out.printf("   Проверяем положение границы сжатой зоны:%n");
        System.out.printf("   x = (Rs·As - Rs·As' - Rb·(bf-b)·hf) / (Rb·b)%n");
        System.out.printf("   x = (%.0f·%.4f - %.0f·%.4f - %.0f·(%.2f-%.2f)·%.3f) / (%.0f·%.2f)%n",
                Rs_kPa, ctx.As_beam_tensile, Rs_kPa, ctx.As_beam_compressive,
                Rb_kPa, ctx.bf, ctx.beamWidth, ctx.hf, Rb_kPa, ctx.beamWidth);
        System.out.printf("   x = %.4f м%n", x);

        boolean xInFlange = (x <= ctx.hf);
        if (xInFlange) {
            System.out.printf("   Так как x (%.4f) ≤ hf (%.3f), граница сжатой зоны проходит в плите%n", x, ctx.hf);
            System.out.println("   Используем формулу для прямоугольного сечения (ф. 7.24)");

            x = (Rs_kPa * ctx.As_beam_tensile - Rs_kPa * ctx.As_beam_compressive) / (Rb_kPa * ctx.bf);
            System.out.printf("   Пересчет x = (Rs·As - Rs·As') / (Rb·bf) = %.4f м%n", x);
        } else {
            System.out.printf("   Так как x (%.4f) > hf (%.3f), граница сжатой зоны проходит в ребре%n", x, ctx.hf);
            System.out.println("   Используем формулу для таврового сечения (ф. 7.23)");
        }

        System.out.println("\n[3. Проверка условий]");

        if (x < 2 * ctx.as_beam_compressive) {
            System.out.printf("   Так как x (%.4f) < 2·as' (%.4f), сжатую арматуру не учитываем%n",
                    x, 2 * ctx.as_beam_compressive);
        } else {
            System.out.printf("   x (%.4f) ≥ 2·as' (%.4f), сжатая арматура учитывается%n",
                    x, 2 * ctx.as_beam_compressive);
        }

        System.out.println("\n[4. Предельный изгибающий момент]");

        double M_pred;
        if (xInFlange) {
            M_pred = Rb_kPa * ctx.bf * x * (h0 - 0.5 * x)
                    + Rs_kPa * ctx.As_beam_compressive * (h0 - ctx.as_beam_compressive);
            System.out.printf("   M = Rb·bf·x·(h0-0.5x) + Rs·As'·(h0-as')%n");
        } else {
            M_pred = Rb_kPa * ctx.beamWidth * x * (h0 - 0.5 * x)
                    + Rb_kPa * (ctx.bf - ctx.beamWidth) * ctx.hf * (h0 - 0.5 * ctx.hf)
                    + Rs_kPa * ctx.As_beam_compressive * (h0 - ctx.as_beam_compressive);
            System.out.printf("   M = Rb·b·x·(h0-0.5x) + Rb·(bf-b)·hf·(h0-0.5hf) + Rs·As'·(h0-as')%n");
        }

        // ВАЖНО: M_pred уже в кН·м, НЕ делим на 1000!
        ctx.M_pred_beam = M_pred;
        System.out.printf("   Предельный момент M = %.2f кН·м%n", ctx.M_pred_beam);

        System.out.println("\n[5. Момент от постоянных нагрузок]");

        ctx.Omega_M = 0.5 * ctx.spanLength * (ctx.spanLength / 4.0);
        System.out.printf("   Площадь линии влияния Ω = 0.5·l·(l/4) = 0.5·%.2f·%.2f = %.2f м²%n",
                ctx.spanLength, ctx.spanLength / 4.0, ctx.Omega_M);

        ctx.Mp_beam = (ctx.np * ctx.ppBeam + ctx.npPrime * ctx.pbBeam) * ctx.Omega_M;
        System.out.printf("   Mp = (np·pp + np'·pb)·Ω = (%.2f·%.2f + %.2f·%.2f)·%.2f = %.2f кН·м%n",
                ctx.np, ctx.ppBeam, ctx.npPrime, ctx.pbBeam, ctx.Omega_M, ctx.Mp_beam);

        System.out.println("\n[6. Допускаемая временная нагрузка и класс балки]");

        double epsilonM = ctx.epsilonM_Beam1 != null ? ctx.epsilonM_Beam1 : 0.5;
        ctx.k_beam_moment = (ctx.M_pred_beam - ctx.Mp_beam) / (ctx.nk * epsilonM * ctx.Omega_M);
        System.out.printf("   k = (M - Mp) / (nk·εM·Ω) = (%.2f - %.2f) / (%.2f·%.2f·%.2f) = %.2f кН/м%n",
                ctx.M_pred_beam, ctx.Mp_beam, ctx.nk, epsilonM, ctx.Omega_M, ctx.k_beam_moment);

        ctx.kc_beam = Interpolation.linear(LAMBDA_POINTS, KC_POINTS_ALPHA_05, ctx.spanLength);
        System.out.printf("   Эталонная нагрузка kc = %.2f кН/м (Таблица П.1.1, λ=%.1f м, α=0.5, интерполяция)%n",
                ctx.kc_beam, ctx.spanLength);

        double mu_beam = ctx.dynamicCoeffBeam != null ? ctx.dynamicCoeffBeam : 1.325;
        ctx.K_beam_moment = ctx.k_beam_moment / (ctx.kc_beam * mu_beam);
        System.out.printf("   КЛАСС БАЛКИ K = k / (kc·(1+μ)) = %.2f / (%.2f·%.3f) = %.2f (ф. 4.1)%n",
                ctx.k_beam_moment, ctx.kc_beam, mu_beam, ctx.K_beam_moment);
    }
}