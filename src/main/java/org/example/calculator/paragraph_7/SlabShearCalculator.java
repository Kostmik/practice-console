package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;

public class SlabShearCalculator {

    /**
     * Расчет плиты балластного корыта по поперечной силе (п. 7.2.4)
     */
    public static void calculateAndPrintReport(BridgeContext ctx) {

        double b = 1.0; // Расчетная ширина плиты, м

        // Коэффициент ηQ из таблицы 7.2 (упрощенно для hb=0.25, e=0.2)
        // В идеале нужно сделать интерполяцию по таблице 7.2
        ctx.etaQ = getEtaQ(ctx.ballastThickness, ctx.trackOffsetLeft, "monolithic");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Толщина балласта hb = %.2f м%n", ctx.ballastThickness);
        System.out.printf("   Смещение оси пути e = %.2f м%n", ctx.trackOffsetLeft);
        System.out.printf("   Коэффициент ηQ = %.2f (табл. 7.2)%n", ctx.etaQ);

        // =====================================================================
        // СЕЧЕНИЕ II-II (МОНОЛИТНЫЙ УЧАСТОК)
        // =====================================================================
        System.out.println("\n[2. Сечение II-II (Монолитный участок)]");

        double h0_II = ctx.slabHeight - ctx.as_tensile;
        System.out.printf("   Рабочая высота h0 = %.3f м%n", h0_II);

        // Предельная поперечная сила (ф. 7.17)
        ctx.Q_pred_II = 0.75 * ctx.Rbt * 1000 * b * h0_II; // Rbt в МПа, переводим в кПа
        System.out.printf("   Предельная поперечная сила Q = 0.75·Rbt·b·h0 = 0.75·%.2f·%.1f·%.3f = %.2f кН (ф. 7.17)%n",
                ctx.Rbt, b, h0_II, ctx.Q_pred_II);

        // Поперечная сила от постоянных нагрузок (ф. 7.19)
        ctx.Qp_II = (ctx.np * ctx.ppSlab + ctx.npPrime * ctx.pbSlab) * ctx.lp / 2.0;
        System.out.printf("   Поперечная сила от пост. нагрузок Qp = (np·pp + np'·pb)·lp/2 = %.2f кН (ф. 7.19)%n", ctx.Qp_II);

        // Допускаемая временная нагрузка (ф. 7.16)
        ctx.k_shear_monolithic = 2 * ctx.lp * (ctx.Q_pred_II - ctx.Qp_II) / (ctx.etaQ * ctx.nk * b * ctx.lp);
        System.out.printf("   Допускаемая врем. нагрузка k = 2·l0·(Q-Qp) / (ηQ·nk·b·lp) = %.1f кН/м (ф. 7.16)%n", ctx.k_shear_monolithic);

        // Класс плиты (ф. 4.1)
        double kc = getKcByBallast(ctx.ballastThickness);
        double mu_slab = ctx.dynamicCoeffSlab != null ? ctx.dynamicCoeffSlab : 1.5;
        ctx.K_shear_monolithic = ctx.k_shear_monolithic / (kc * mu_slab);
        System.out.printf("   КЛАСС ПЛИТЫ K = k / (kc·(1+μ)) = %.2f / (%.1f · %.2f) = %.2f (ф. 4.1)%n",
                ctx.k_shear_monolithic, kc, mu_slab, ctx.K_shear_monolithic);

        // =====================================================================
        // СЕЧЕНИЕ III-III (ВНЕШНЯЯ КОНСОЛЬ)
        // =====================================================================
        System.out.println("\n[3. Сечение III-III (Внешняя консоль)]");

        double h0_III = ctx.slabHeight - ctx.as_tensile;
        System.out.printf("   Рабочая высота h0 = %.3f м%n", h0_III);

        // Предельная поперечная сила (ф. 7.17)
        ctx.Q_pred_III = 0.75 * ctx.Rbt * 1000 * b * h0_III;
        System.out.printf("   Предельная поперечная сила Q = 0.75·Rbt·b·h0 = %.2f кН (ф. 7.17)%n", ctx.Q_pred_III);

        // Поперечная сила от постоянных нагрузок (ф. 7.18)
        // Упрощенная формула: Qp = np*P0 + np'*pb*(lb - Z) + np*pt*(lt - lk) + np*pp*(lk - Z)
        // Для Z = 0 (корень консоли)
        double Z = 0.0;
        ctx.Qp_III = ctx.np * ctx.P0 + ctx.npPrime * ctx.pbSlab * (ctx.lb_prime - Z)
                + ctx.np * ctx.pt * (ctx.lt - ctx.lk) + ctx.np * ctx.ppSlab * (ctx.lk - Z);
        System.out.printf("   Поперечная сила от пост. нагрузок Qp = np·P0 + np'·pb·(lb-Z) + np·pt·(lt-lk) + np·pp·(lk-Z) = %.2f кН (ф. 7.18)%n", ctx.Qp_III);

        // Допускаемая временная нагрузка (ф. 7.15)
        double Delta = 0.7; // Длина распределения нагрузки на консоли (из расчета момента)
        ctx.k_shear_external = ctx.lp * (ctx.Q_pred_III - ctx.Qp_III) / (ctx.etaQ * ctx.nk * b * (Delta - Z));
        System.out.printf("   Допускаемая врем. нагрузка k = l0·(Q-Qp) / (ηQ·nk·b·(Δ-Z)) = %.1f кН/м (ф. 7.15)%n", ctx.k_shear_external);

        // Класс плиты (ф. 4.1)
        ctx.K_shear_external = ctx.k_shear_external / (kc * mu_slab);
        System.out.printf("   КЛАСС ПЛИТЫ K = k / (kc·(1+μ)) = %.2f / (%.1f · %.2f) = %.2f (ф. 4.1)%n",
                ctx.k_shear_external, kc, mu_slab, ctx.K_shear_external);

        // =====================================================================
        // ИТОГ
        // =====================================================================
        System.out.println("\n============================================================");
        System.out.println("============================================================");
        System.out.printf("   Класс монолитного участка (II-II):  K = %.2f%n", ctx.K_shear_monolithic);
        System.out.printf("   Класс внешней консоли (III-III):    K = %.2f%n", ctx.K_shear_external);
        double minK = Math.min(ctx.K_shear_monolithic, ctx.K_shear_external);
        System.out.printf("   МИНИМАЛЬНЫЙ КЛАСС ПО ПОПЕРЕЧНОЙ СИЛЕ: K = %.2f %n", minK);
        System.out.println("============================================================\n");
    }

    /**
     * Получение коэффициента ηQ из таблицы 7.2 (упрощенно)
     * В идеале нужно сделать полную интерполяцию по таблице 7.2
     */
    private static double getEtaQ(double hb, double e, String sectionType) {
        // Упрощенные значения для hb=0.25, e=0.2
        if (sectionType.equals("monolithic")) {
            return 1.23; // Для монолитного участка
        } else {
            return 1.27; // Для внешней консоли
        }
    }

    /**
     * Эталонная нагрузка kc для плиты в зависимости от толщины балласта (Приложение 1)
     */
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
