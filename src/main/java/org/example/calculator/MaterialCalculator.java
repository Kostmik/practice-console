package org.example.calculator;

import org.example.context.BridgeContext;

public class MaterialCalculator {

    // Табличные значения из Таблицы 5.1
    private static final double[] R_POINTS = {13.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0, 60.0};
    private static final double[] RB_POINTS = {5.5, 6.5, 8.5, 10.0, 12.0, 16.0, 19.5, 23.0};
    private static final double[] RBT_POINTS = {0.50, 0.55, 0.65, 0.85, 0.90, 1.10, 1.25, 1.35};

    // Табличные значения для Модуля упругости Eb (п. 5.1)
    private static final double[] R_FOR_EB = {25.0, 30.0, 40.0, 50.0, 60.0};
    private static final double[] EB_POINTS_GPA = {27.0, 29.5, 33.5, 36.0, 38.5}; // в ГПа

    // Табличные значения для n' (п. 5.2)
    private static final double[] R_FOR_N = {20.0, 30.0, 40.0, 50.0, 60.0};
    private static final double[] N_PRIME_POINTS = {25, 20, 15, 12, 10};

    /**
     * Расчет и вывод подробного отчета по характеристикам материалов (Раздел 5)
     */
    public static void printMaterialsReport(BridgeContext ctx, int rebarChoice) {
        System.out.println("============================================================");
        System.out.println(" РАСЧЕТНЫЕ ХАРАКТЕРИСТИКИ МАТЕРИАЛОВ [Раздел 5]");
        System.out.println("============================================================");

        double R = ctx.concreteStrengthR;
        System.out.printf("\n[1. Исходные данные]%n");
        System.out.printf("   Фактическая прочность бетона R = %.1f МПа%n", R);

        // =====================================================================
        // БЕТОН: Rb (Таблица 5.1)
        // =====================================================================
        System.out.println("\n[2. Расчетное сопротивление бетона сжатию Rb (Таблица 5.1)]");
        ctx.Rb = printInterpolation("Rb", R_POINTS, RB_POINTS, R);

        // =====================================================================
        // БЕТОН: Rbt (Таблица 5.1)
        // =====================================================================
        System.out.println("\n[3. Расчетное сопротивление бетона растяжению Rbt (Таблица 5.1)]");
        ctx.Rbt = printInterpolation("Rbt", R_POINTS, RBT_POINTS, R);

        // =====================================================================
        // МОДУЛЬ УПРУГОСТИ БЕТОНА Eb (п. 5.1)
        // =====================================================================
        System.out.println("\n[4. Модуль упругости бетона Eb (п. 5.1)]");
        double ebGPa = printInterpolation("Eb", R_FOR_EB, EB_POINTS_GPA, R);
        ctx.Eb = ebGPa * 1000; // Переводим ГПа в МПа
        System.out.printf("   Переводим в МПа: Eb = %.1f · 1000 = %.0f МПа%n", ebGPa, ctx.Eb);

        // =====================================================================
        // АРМАТУРА (п. 5.2)
        // =====================================================================
        System.out.println("\n[5. Расчетные сопротивления арматуры (п. 5.2)]");
        if (rebarChoice == 1) {
            ctx.Rs = 190.0;
            ctx.rebarType = "Гладкая (А240)";
        } else {
            ctx.Rs = 240.0;
            ctx.rebarType = "Периодического профиля (А400)";
        }
        System.out.printf("   Тип арматуры: %s%n", ctx.rebarType);
        System.out.printf("   Расчетное сопротивление растяжению Rs = %.0f МПа (принято по табл. 5.2)%n", ctx.Rs);
        System.out.printf("   Модуль упругости арматуры Es = %.0f МПа (принято по п. 5.2)%n", ctx.Es);

        // =====================================================================
        // ОТНОШЕНИЕ МОДУЛЕЙ n' (п. 5.2)
        // =====================================================================
        System.out.println("\n[6. Условное отношение модулей упругости n' (п. 5.2)]");
        ctx.nPrime = printInterpolation("n'", R_FOR_N, N_PRIME_POINTS, R);
        System.out.printf("   (Используется при расчетах на выносливость)%n");
    }

    /**
     * Универсальный метод для красивой пошаговой интерполяции.
     * Показывает граничные значения, формулу, подстановку и итог.
     */
    private static double printInterpolation(String paramName, double[] x, double[] y, double xTarget) {
        // 1. Проверяем на точное совпадение
        for (int i = 0; i < x.length; i++) {
            if (Math.abs(xTarget - x[i]) < 0.001) {
                System.out.printf("   Так как R = %.1f МПа точно совпадает с табличным значением:%n", xTarget);
                System.out.printf("   %s = %.2f%n", paramName, y[i]);
                return y[i];
            }
        }

        // 2. Если значение меньше минимального в таблице
        if (xTarget < x[0]) {
            System.out.printf("   Так как R = %.1f МПа меньше минимального табличного %.1f МПа:%n", xTarget, x[0]);
            System.out.printf("   Принимаем %s = %.2f (по нижней границе таблицы)%n", paramName, y[0]);
            return y[0];
        }

        // 3. Если значение больше максимального в таблице
        if (xTarget > x[x.length - 1]) {
            System.out.printf("   Так как R = %.1f МПа больше максимального табличного %.1f МПа:%n", xTarget, x[x.length - 1]);
            System.out.printf("   Принимаем %s = %.2f (по верхней границе таблицы)%n", paramName, y[y.length - 1]);
            return y[y.length - 1];
        }

        // 4. Находим интервал и интерполируем
        int idx = -1;
        for (int i = 0; i < x.length - 1; i++) {
            if (xTarget > x[i] && xTarget < x[i + 1]) {
                idx = i;
                break;
            }
        }

        double x1 = x[idx], x2 = x[idx + 1];
        double y1 = y[idx], y2 = y[idx + 1];

        System.out.printf("   Так как R = %.1f МПа находится между табличными %.1f и %.1f МПа:%n", xTarget, x1, x2);
        System.out.printf("   %s(%.1f) = %.2f,  %s(%.1f) = %.2f%n", paramName, x1, y1, paramName, x2, y2);
        System.out.println("   Применяем линейную интерполяцию:");
        System.out.printf("   %s = %.2f + (%.2f - %.2f) · (%.1f - %.1f) / (%.1f - %.1f)%n",
                paramName, y1, y2, y1, xTarget, x1, x2, x1);

        double multiplier = (xTarget - x1) / (x2 - x1);
        double result = y1 + (y2 - y1) * multiplier;

        System.out.printf("   %s = %.2f + %.2f · %.2f = %.2f%n",
                paramName, y1, (y2 - y1), multiplier, result);

        return result;
    }
}