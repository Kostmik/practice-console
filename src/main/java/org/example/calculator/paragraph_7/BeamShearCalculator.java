package org.example.calculator.paragraph_7;

import org.example.context.BridgeContext;
import org.example.util.Interpolation;

public class BeamShearCalculator {

    // Таблица П.1.1 - Эталонная нагрузка kc для α=0.5
    private static final double[] LAMBDA_POINTS = {1, 1.5, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 18, 20, 25, 30};
    private static final double[] KC_POINTS_ALPHA_05 = {49.03, 34.25, 26.73, 21.14, 18.99, 17.82, 17.06, 16.48, 16.02, 15.63, 15.28, 14.68, 14.16, 13.71, 13.30, 12.92, 12.12, 11.46};

    public static void calculateAndPrintReport(BridgeContext ctx) {
        System.out.println("============================================================");
        System.out.println(" РАСЧЕТ ГЛАВНОЙ БАЛКИ ПО ПОПЕРЕЧНОЙ СИЛЕ [п. 7.2.7-7.2.8]");
        System.out.println("============================================================");

        double Rb_kPa = ctx.Rb * 1000;
        double Rbt_kPa = ctx.Rbt * 1000;
        double Rs_kPa = ctx.Rs * 1000;
        double Rsw_kPa = ctx.Rs * 1000;
        double Eb_MPa = ctx.Eb;
        double Es_MPa = ctx.Es;

        double h0 = ctx.beamHeight - ctx.as_beam_tensile;
        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Рабочая высота сечения h0 = %.3f м%n", h0);
        System.out.printf("   Ширина ребра b = %.2f м%n", ctx.beamWidth);
        System.out.printf("   Площадь ветви хомутов Asw = %.6f м²%n", ctx.Asw);
        System.out.printf("   Шаг хомутов s = %.2f м%n", ctx.s_stirrups);
        System.out.printf("   Сумма площадей отгибов ΣAsi = %.6f м²%n", ctx.sum_Asi);
        System.out.printf("   Угол наклона отгибов α = %.1f°%n", ctx.alpha_bent);

        System.out.println("\n[2. Расчет коэффициентов (ф. 7.28)]");

        double mu_w = ctx.Asw / (ctx.beamWidth * ctx.s_stirrups);
        System.out.printf("   Относительное армирование μw = Asw / (b·s) = %.6f / (%.2f·%.2f) = %.5f%n",
                ctx.Asw, ctx.beamWidth, ctx.s_stirrups, mu_w);

        double alpha_E = Es_MPa / Eb_MPa;
        System.out.printf("   Отношение модулей упругости αE = Es / Eb = %.0f / %.0f = %.2f%n",
                Es_MPa, Eb_MPa, alpha_E);

        double phi_w = 1.0 + 5.0 * alpha_E * mu_w;
        if (phi_w > 1.3) {
            System.out.printf("   φw = 1 + 5·αE·μw = 1 + 5·%.2f·%.5f = %.3f > 1.3. Принимаем φw = 1.300%n",
                    alpha_E, mu_w, phi_w);
            phi_w = 1.3;
        } else {
            System.out.printf("   φw = 1 + 5·αE·μw = 1 + 5·%.2f·%.5f = %.3f (≤ 1.3)%n",
                    alpha_E, mu_w, phi_w);
        }

        double phi_b = 1.0 - 0.01 * ctx.Rb;
        System.out.printf("   φb = 1 - 0.01·Rb = 1 - 0.01·%.1f = %.3f%n", ctx.Rb, phi_b);

        System.out.println("\n[3. Предельная сила по сжатому бетону между трещинами (ф. 7.27)]");
        double Q_concrete = 0.3 * phi_w * phi_b * Rb_kPa * ctx.beamWidth * h0;
        System.out.printf("   Q1 = 0.3·φw·φb·Rb·b·h0 = 0.3·%.3f·%.3f·%.0f·%.2f·%.3f = %.2f кН%n",
                phi_w, phi_b, Rb_kPa, ctx.beamWidth, h0, Q_concrete);

        System.out.println("\n[4. Предельная сила по наклонной трещине (ф. 7.29-7.31)]");

        double c_calc = 2.5 * (Rbt_kPa * ctx.beamWidth * Math.pow(h0, 2) * ctx.s_stirrups) / (Rs_kPa * ctx.Asw);
        double c = Math.min(c_calc, 2.0 * h0);
        System.out.printf("   Расчетная длина проекции c = 2.5·(Rbt·b·h0²·s) / (Rs·Asw) = %.2f м%n", c_calc);
        if (c < c_calc) {
            System.out.printf("   Так как c > 2·h0 (%.2f м), принимаем c = 2·h0 = %.2f м%n", 2.0 * h0, c);
        } else {
            System.out.printf("   Принимаем c = %.2f м (≤ 2·h0 = %.2f м)%n", c, 2.0 * h0);
        }

        double Qb = (2.0 * Rbt_kPa * ctx.beamWidth * Math.pow(h0, 2)) / c;
        System.out.printf("   Сила, воспринимаемая бетоном Qb = 2·Rbt·b·h0² / c = %.2f кН%n", Qb);

        double sinAlpha = Math.sin(Math.toRadians(ctx.alpha_bent));
        double Q_inclined = 0.8 * Rs_kPa * ctx.sum_Asi * sinAlpha
                + 0.8 * Rsw_kPa * ctx.Asw * (c / ctx.s_stirrups)
                + Qb;

        System.out.printf("   Q2 = 0.8·Rs·ΣAsi·sinα + 0.8·Rsw·Asw·(c/s) + Qb%n");
        System.out.printf("   Q2 = 0.8·%.0f·%.6f·%.3f + 0.8·%.0f·%.6f·(%.2f/%.2f) + %.2f = %.2f кН%n",
                Rs_kPa, ctx.sum_Asi, sinAlpha, Rsw_kPa, ctx.Asw, c, ctx.s_stirrups, Qb, Q_inclined);

        System.out.println("\n[5. Итоговая предельная поперечная сила]");
        ctx.Q_ultimate = Math.min(Q_concrete, Q_inclined);
        System.out.printf("   Q = min(Q1, Q2) = min(%.2f, %.2f) = %.2f кН%n", Q_concrete, Q_inclined, ctx.Q_ultimate);

        System.out.println("\n[6. Допускаемая временная нагрузка и класс балки]");

        ctx.Q_p_shear = (ctx.np * ctx.ppBeam + ctx.npPrime * ctx.pbBeam) * ctx.Omega_p;
        System.out.printf("   Поперечная сила от пост. нагрузок Qp = (np·pp + np'·pb)·Ωp = (%.2f·%.2f + %.2f·%.2f)·%.2f = %.2f кН%n",
                ctx.np, ctx.ppBeam, ctx.npPrime, ctx.pbBeam, ctx.Omega_p, ctx.Q_p_shear);

        double epsilonQ = ctx.epsilonQ_Beam1 != null ? ctx.epsilonQ_Beam1 : 0.5;
        ctx.k_beam_shear = (ctx.Q_ultimate - ctx.Q_p_shear) / (ctx.nk * epsilonQ * ctx.Omega_k);
        System.out.printf("   k = (Q - Qp) / (nk·εQ·Ωk) = (%.2f - %.2f) / (%.2f·%.2f·%.2f) = %.2f кН/м%n",
                ctx.Q_ultimate, ctx.Q_p_shear, ctx.nk, epsilonQ, ctx.Omega_k, ctx.k_beam_shear);

        double kc = Interpolation.linear(LAMBDA_POINTS, KC_POINTS_ALPHA_05, ctx.spanLength);
        System.out.printf("   Эталонная нагрузка kc = %.2f кН/м (Таблица П.1.1, λ=%.1f м, α=0.5, интерполяция)%n",
                kc, ctx.spanLength);

        double mu_beam = ctx.dynamicCoeffBeam != null ? ctx.dynamicCoeffBeam : 1.325;
        ctx.K_beam_shear = ctx.k_beam_shear / (kc * mu_beam);
        System.out.printf("   КЛАСС БАЛКИ K = k / (kc·(1+μ)) = %.2f / (%.2f·%.3f) = %.2f (ф. 4.1)%n",
                ctx.k_beam_shear, kc, mu_beam, ctx.K_beam_shear);

        System.out.println("\n============================================================");
        System.out.println(" РАСЧЕТ ЗАВЕРШЕН");
        System.out.println("============================================================\n");
    }
}