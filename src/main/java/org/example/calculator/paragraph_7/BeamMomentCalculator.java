package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;

public class BeamCalculator {

    /**
     * Расчет главной балки по изгибающему моменту (п. 7.2.5-7.2.6)
     */
    public static void calculateAndPrintReport(BridgeContext ctx) {
        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ ГЛАВНОЙ БАЛКИ ПО ИЗГИБАЮЩЕМУ МОМЕНТУ [п. 7.2.5-7.2.6]");
        System.out.println("============================================================");

        // Переводим сопротивления в кПа
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

        // =====================================================================
        // ВЫСОТА СЖАТОЙ ЗОНЫ
        // =====================================================================
        System.out.println("\n[2. Высота сжатой зоны бетона]");

        // Сначала проверяем, проходит ли граница сжатой зоны в плите или в ребре
        // Формула 7.23: x = (Rs·As - Rs·As' - Rb·(bf-b)·hf) / (Rb·b)
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

            // Пересчитываем x для прямоугольного сечения (ф. 7.24)
            x = (Rs_kPa * ctx.As_beam_tensile - Rs_kPa * ctx.As_beam_compressive) / (Rb_kPa * ctx.bf);
            System.out.printf("   Пересчет x = (Rs·As - Rs·As') / (Rb·bf) = %.4f м%n", x);
        } else {
            System.out.printf("   Так как x (%.4f) > hf (%.3f), граница сжатой зоны проходит в ребре%n", x, ctx.hf);
            System.out.println("   Используем формулу для таврового сечения (ф. 7.23)");
        }

        // =====================================================================
        // ПРОВЕРКА УСЛОВИЙ
        // =====================================================================
        System.out.println("\n[3. Проверка условий]");

        // Проверка x < 2·as'
        if (x < 2 * ctx.as_beam_compressive) {
            System.out.printf("   Так как x (%.4f) < 2·as' (%.4f), сжатую арматуру не учитываем%n",
                    x, 2 * ctx.as_beam_compressive);
        } else {
            System.out.printf("   x (%.4f) ≥ 2·as' (%.4f), сжатая арматура учитывается%n",
                    x, 2 * ctx.as_beam_compressive);
        }

        // =====================================================================
        // ПРЕДЕЛЬНЫЙ МОМЕНТ
        // =====================================================================
        System.out.println("\n[4. Предельный изгибающий момент]");

        double M_pred;
        if (xInFlange) {
            // Формула 7.24: M = Rb·bf·x·(h0 - 0.5x) + Rs·As'·(h0 - as')
            M_pred = Rb_kPa * ctx.bf * x * (h0 - 0.5 * x)
                    + Rs_kPa * ctx.As_beam_compressive * (h0 - ctx.as_beam_compressive);
            System.out.printf("   M = Rb·bf·x·(h0-0.5x) + Rs·As'·(h0-as')%n");
        } else {
            // Формула 7.22: M = Rb·b·x·(h0-0.5x) + Rb·(bf-b)·hf·(h0-0.5hf) + Rs·As'·(h0-as')
            M_pred = Rb_kPa * ctx.beamWidth * x * (h0 - 0.5 * x)
                    + Rb_kPa * (ctx.bf - ctx.beamWidth) * ctx.hf * (h0 - 0.5 * ctx.hf)
                    + Rs_kPa * ctx.As_beam_compressive * (h0 - ctx.as_beam_compressive);
            System.out.printf("   M = Rb·b·x·(h0-0.5x) + Rb·(bf-b)·hf·(h0-0.5hf) + Rs·As'·(h0-as')%n");
        }

        ctx.M_pred_beam = M_pred / 1000; // Переводим в кН·м
        System.out.printf("   Предельный момент M = %.2f кН·м%n", ctx.M_pred_beam);

        // =====================================================================
        // МОМЕНТ ОТ ПОСТОЯННЫХ НАГРУЗОК
        // =====================================================================
        System.out.println("\n[5. Момент от постоянных нагрузок]");

        // Площадь линии влияния момента (треугольник с вершиной в середине пролета)
        ctx.Omega_M = 0.5 * ctx.spanLength * (ctx.spanLength / 2.0);
        System.out.printf("   Площадь линии влияния Ω = 0.5·l·(l/2) = 0.5·%.2f·%.2f = %.2f м²%n",
                ctx.spanLength, ctx.spanLength / 2.0, ctx.Omega_M);

        // Формула 7.21: Mp = (np·pp + np'·pb)·Ω
        ctx.Mp_beam = (ctx.np * ctx.ppBeam + ctx.npPrime * ctx.pbBeam) * ctx.Omega_M;
        System.out.printf("   Mp = (np·pp + np'·pb)·Ω = (%.2f·%.2f + %.2f·%.2f)·%.2f = %.2f кН·м%n",
                ctx.np, ctx.ppBeam, ctx.npPrime, ctx.pbBeam, ctx.Omega_M, ctx.Mp_beam);

        // =====================================================================
        // ДОПУСКАЕМАЯ НАГРУЗКА И КЛАСС
        // =====================================================================
        System.out.println("\n[6. Допускаемая временная нагрузка и класс балки]");

        // Формула 7.20: k = (M - Mp) / (nk·εM·Ω)
        double epsilonM = ctx.epsilonM_Beam1 != null ? ctx.epsilonM_Beam1 : 0.5;
        ctx.k_beam_moment = (ctx.M_pred_beam - ctx.Mp_beam) / (ctx.nk * epsilonM * ctx.Omega_M);
        System.out.printf("   k = (M - Mp) / (nk·εM·Ω) = (%.2f - %.2f) / (%.2f·%.2f·%.2f) = %.2f кН/м%n",
                ctx.M_pred_beam, ctx.Mp_beam, ctx.nk, epsilonM, ctx.Omega_M, ctx.k_beam_moment);

        // Эталонная нагрузка для балки (берем из приложения 1, упрощенно)
        ctx.kc_beam = getKcForBeam(ctx.spanLength);
        System.out.printf("   Эталонная нагрузка kc = %.2f кН/м (Приложение 1, λ=%.1f м, α=0.5)%n",
                ctx.kc_beam, ctx.spanLength);

        double mu_beam = ctx.dynamicCoeffBeam != null ? ctx.dynamicCoeffBeam : 1.325;
        ctx.K_beam_moment = ctx.k_beam_moment / (ctx.kc_beam * mu_beam);
        System.out.printf("   КЛАСС БАЛКИ K = k / (kc·(1+μ)) = %.2f / (%.2f·%.3f) = %.2f (ф. 4.1)%n",
                ctx.k_beam_moment, ctx.kc_beam, mu_beam, ctx.K_beam_moment);

        System.out.println("\n============================================================");
        System.out.println(" РАСЧЕТ ЗАВЕРШЕН");
        System.out.println("============================================================\n");
    }

    /**
     * Эталонная нагрузка для главной балки (упрощенно)
     */
    private static double getKcForBeam(double lambda) {
        // Упрощенная таблица из Приложения 1, Таблица П.1.1 для α=0.5
        if (lambda <= 1) return 49.03;
        if (lambda <= 2) return 26.73;
        if (lambda <= 3) return 21.14;
        if (lambda <= 4) return 18.99;
        if (lambda <= 5) return 17.82;
        if (lambda <= 6) return 17.06;
        if (lambda <= 7) return 16.48;
        if (lambda <= 8) return 16.02;
        if (lambda <= 9) return 15.63;
        if (lambda <= 10) return 15.28;
        if (lambda <= 12) return 14.68;
        if (lambda <= 14) return 14.16;
        if (lambda <= 16) return 13.71;
        if (lambda <= 18) return 13.30;
        if (lambda <= 20) return 12.92;
        if (lambda <= 25) return 12.12;
        if (lambda <= 30) return 11.46;
        return 11.46;
    }
}