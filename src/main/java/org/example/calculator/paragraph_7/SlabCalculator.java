package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;

public class SlabCalculator {

    public static void calculateAndPrintReport(BridgeContext ctx) {

        double b = 1.0; // Расчетная ширина плиты, м

        // Переводим сопротивления в кПа для корректного расчёта
        double Rb_kPa = ctx.Rb * 1000;  // МПа → кПа
        double Rs_kPa = ctx.Rs * 1000;  // МПа → кПа

        // =====================================================================
        // 1. ДЛИНА РАСПРЕДЕЛЕНИЯ НАГРУЗКИ l0 (Формулы 7.4, 7.5)
        // =====================================================================
        System.out.println("\n[1. Длина распределения временной нагрузки l0 (ф. 7.4, 7.5)]");

        double delta_prime = 0.5 * (ctx.ls - ctx.B) + ctx.trackOffsetLeft + ctx.hb_prime;
        double delta_doubleprime = 0.5 * (ctx.ls - ctx.B) - ctx.trackOffsetLeft + ctx.hb_doubleprime;

        System.out.printf("   Δ' = 0.5·(ls - B) + e + h'b = 0.5·(%.2f - %.2f) + %.2f + %.2f = %.2f м%n",
                ctx.ls, ctx.B, ctx.trackOffsetLeft, ctx.hb_prime, delta_prime);
        System.out.printf("   Δ'' = 0.5·(ls - B) - e + h''b = 0.5·(%.2f - %.2f) - %.2f + %.2f = %.2f м%n",
                ctx.ls, ctx.B, ctx.trackOffsetLeft, ctx.hb_doubleprime, delta_doubleprime);

        boolean useCalculatedDeltas = (delta_prime <= ctx.lb_prime) && (delta_doubleprime <= ctx.lb_doubleprime);

        if (useCalculatedDeltas) {
            System.out.printf("   Так как Δ' (%.2f) ≤ l'b (%.2f) и Δ'' (%.2f) ≤ l''b (%.2f):%n",
                    delta_prime, ctx.lb_prime, delta_doubleprime, ctx.lb_doubleprime);
            System.out.printf("   Принимаем Δ' = %.2f м, Δ'' = %.2f м%n", delta_prime, delta_doubleprime);
        } else {
            System.out.printf("   Условия Δ' ≤ l'b или Δ'' ≤ l''b не выполняются.%n");
            System.out.printf("   Принимаем Δ' = l'b = %.2f м, Δ'' = l''b = %.2f м%n", ctx.lb_prime, ctx.lb_doubleprime);
            delta_prime = ctx.lb_prime;
            delta_doubleprime = ctx.lb_doubleprime;
        }

        double l0 = ctx.B + delta_prime + delta_doubleprime;
        ctx.l0 = l0;

        System.out.printf("   l0 = B + Δ' + Δ'' = %.2f + %.2f + %.2f = %.2f м%n", ctx.B, delta_prime, delta_doubleprime, l0);

        // =====================================================================
        // 2. СЕЧЕНИЕ I-I (МОНОЛИТНЫЙ УЧАСТОК)
        // =====================================================================
        System.out.println("\n[2. Сечение I-I (Монолитный участок между ребрами)]");

        double h0_I = ctx.slabHeight - ctx.as_tensile;
        System.out.printf("   Рабочая высота h0 = h - as = %.2f - %.2f = %.3f м%n", ctx.slabHeight, ctx.as_tensile, h0_I);

        // Высота сжатой зоны БЕЗ учёта сжатой арматуры (первое приближение)
        double x_without_compressive = (Rs_kPa * ctx.As_tensile) / (Rb_kPa * b);
        System.out.printf("   Высота сжатой зоны x без учёта As' = %.4f м%n", x_without_compressive);

        // Проверяем условие x < as'
        if (x_without_compressive < ctx.as_compressive) {
            System.out.printf("   Так как x (%.4f) < as' (%.4f), сжатую арматуру не учитываем%n",
                    x_without_compressive, ctx.as_compressive);

            // Предельный момент по ф. 7.11 без сжатой арматуры
            // Rb в кПа, b в м, x в м, h0 в м → результат в кН·м
            ctx.M_pred_I = Rb_kPa * b * x_without_compressive * (h0_I - 0.5 * x_without_compressive);
            System.out.printf("   Предельный момент M = Rb·b·x·(h0-0.5x) = %.2f кН·м (ф. 7.11 без As')%n",
                    ctx.M_pred_I);
        } else if (x_without_compressive < 2 * ctx.as_compressive) {
            System.out.printf("   Так как as' ≤ x (%.4f) < 2·as' (%.4f), используем ф. 7.14%n",
                    x_without_compressive, 2 * ctx.as_compressive);

            // Предельный момент по ф. 7.14
            ctx.M_pred_I = Rs_kPa * ctx.As_tensile * (h0_I - ctx.as_compressive);
            System.out.printf("   Предельный момент M = Rs·As·(h0 - as') = %.2f кН·м (ф. 7.14)%n",
                    ctx.M_pred_I);
        } else {
            System.out.printf("   Так как x (%.4f) ≥ 2·as' (%.4f), учитываем сжатую арматуру%n",
                    x_without_compressive, 2 * ctx.as_compressive);

            // Высота сжатой зоны с учётом сжатой арматуры
            double x_with_compressive = (Rs_kPa * ctx.As_tensile - Rs_kPa * ctx.As_compressive) / (Rb_kPa * b);
            System.out.printf("   Высота сжатой зоны x с учётом As' = %.4f м%n", x_with_compressive);

            // Предельный момент по ф. 7.11
            ctx.M_pred_I = Rb_kPa * b * x_with_compressive * (h0_I - 0.5 * x_with_compressive)
                    + Rs_kPa * ctx.As_compressive * (h0_I - ctx.as_compressive);
            System.out.printf("   Предельный момент M = Rb·b·x·(h0-0.5x) + Rs·As'·(h0-as') = %.2f кН·м (ф. 7.11)%n",
                    ctx.M_pred_I);
        }

        // Момент от постоянных нагрузок (ф. 7.10)
        double Mp_calc = (ctx.np * ctx.ppSlab + ctx.npPrime * ctx.pbSlab) * Math.pow(ctx.lp, 2) / 8.0;
        System.out.printf("   Момент от пост. нагрузки Mp = (np·pp + np'·pb)·lp²/8 = %.3f кН·м (ф. 7.10)%n", Mp_calc);
        double Mp_used = (ctx.Mp_monolithic > 0) ? ctx.Mp_monolithic : Mp_calc;
        if (ctx.Mp_monolithic > 0) {
            System.out.printf("   Принят введенный момент Mp = %.3f кН·м%n", Mp_used);
        }

        // Допускаемая временная нагрузка (ф. 7.3)
        // M_pred_I уже в кН·м, Mp_used в кН·м
        double M_II_approx = ctx.M_pred_I; // Для упрощения берем M_I ≈ M_II
        ctx.k_monolithic = 8 * l0 * (ctx.M_pred_I + M_II_approx - Mp_used) / (ctx.nk * b * Math.pow(ctx.lp, 2));
        System.out.printf("   Допускаемая врем. нагрузка k = 8·l0·(M_I+M_II-Mp) / (nk·b·lp²) = %.1f кН/м (ф. 7.3)%n", ctx.k_monolithic);

        // Класс плиты (ф. 4.1)
        double kc = getKcByBallast(ctx.ballastThickness);
        double mu_slab = ctx.dynamicCoeffSlab != null ? ctx.dynamicCoeffSlab : 1.5;
        ctx.K_monolithic = ctx.k_monolithic / (kc * mu_slab);
        System.out.printf("   КЛАСС ПЛИТЫ K = k / (kc·(1+μ)) = %.2f / (%.1f · %.2f) = %.2f (ф. 4.1)%n",
                ctx.k_monolithic, kc, mu_slab, ctx.K_monolithic);

        // =====================================================================
        // 3. СЕЧЕНИЕ III-III (ВНЕШНЯЯ КОНСОЛЬ)
        // =====================================================================
        System.out.println("\n[3. Сечение III-III (Внешняя консоль)]");

        double h0_III = ctx.slabHeight - ctx.as_tensile;
        System.out.printf("   Рабочая высота h0 = %.3f м%n", h0_III);

        double Z = 0.0;
        double la = delta_prime - Z;
        System.out.printf("   Длина распределения la = Δ' - Z = %.2f - %.2f = %.2f м%n", delta_prime, Z, la);

        // Аналогичная логика для консоли
        double x_without_compressive_III = (Rs_kPa * ctx.As_tensile) / (Rb_kPa * b);

        if (x_without_compressive_III < ctx.as_compressive) {
            ctx.M_pred_III = Rb_kPa * b * x_without_compressive_III * (h0_III - 0.5 * x_without_compressive_III);
            System.out.printf("   Предельный момент M = Rb·b·x·(h0-0.5x) = %.2f кН·м (ф. 7.11 без As')%n",
                    ctx.M_pred_III);
        } else if (x_without_compressive_III < 2 * ctx.as_compressive) {
            ctx.M_pred_III = Rs_kPa * ctx.As_tensile * (h0_III - ctx.as_compressive);
            System.out.printf("   Предельный момент M = Rs·As·(h0 - as') = %.2f кН·м (ф. 7.14)%n",
                    ctx.M_pred_III);
        } else {
            double x_with_compressive_III = (Rs_kPa * ctx.As_tensile - Rs_kPa * ctx.As_compressive) / (Rb_kPa * b);
            ctx.M_pred_III = Rb_kPa * b * x_with_compressive_III * (h0_III - 0.5 * x_with_compressive_III)
                    + Rs_kPa * ctx.As_compressive * (h0_III - ctx.as_compressive);
            System.out.printf("   Предельный момент M = Rb·b·x·(h0-0.5x) + Rs·As'·(h0-as') = %.2f кН·м (ф. 7.11)%n",
                    ctx.M_pred_III);
        }

        double Mp_ext = ctx.Mp_external_cantilever;
        System.out.printf("   Момент от пост. нагрузки Mp = %.3f кН·м (введен пользователем)%n", Mp_ext);

        ctx.k_external_cantilever = 2 * l0 * (ctx.M_pred_III - Mp_ext) / (ctx.nk * b * Math.pow(la, 2));
        System.out.printf("   Допускаемая врем. нагрузка k = 2·l0·(M-Mp) / (nk·b·la²) = %.1f кН/м (ф. 7.1)%n", ctx.k_external_cantilever);

        ctx.K_external_cantilever = ctx.k_external_cantilever / (kc * mu_slab);
        System.out.printf("   КЛАСС КОНСОЛИ K = k / (kc·(1+μ)) = %.2f / (%.1f · %.2f) = %.2f (ф. 4.1)%n",
                ctx.k_external_cantilever, kc, mu_slab, ctx.K_external_cantilever);

        // =====================================================================
        // ИТОГ
        // =====================================================================
        System.out.println("\n============================================================");
        System.out.println("============================================================");
        System.out.printf("   Класс монолитного участка (I-I):  K = %.2f%n", ctx.K_monolithic);
        System.out.printf("   Класс внешней консоли (III-III):  K = %.2f%n", ctx.K_external_cantilever);
        double minK = Math.min(ctx.K_monolithic, ctx.K_external_cantilever);
        System.out.printf("   МИНИМАЛЬНЫЙ КЛАСС ПЛИТЫ:      K = %.2f%n", minK);
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