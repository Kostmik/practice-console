package org.example.calculator.paragraph_5;

import org.example.context.BridgeContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

public class MaterialCalculator {

    /**
     * Расчёт характеристик материалов с возвратом отчёта
     */
    public static String calculateAndReturnReport(BridgeContext ctx, int rebarChoice) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

        try {
            printMaterialsReport(ctx, rebarChoice);
        } finally {
            System.setOut(oldOut);
        }

        return baos.toString(StandardCharsets.UTF_8);
    }

    /**
     * Оригинальный метод с подробным выводом формул
     */
    public static void printMaterialsReport(BridgeContext ctx, int rebarChoice) {
        System.out.println("============================================================");
        //System.out.println(" РАСЧЕТНЫЕ ХАРАКТЕРИСТИКИ МАТЕРИАЛОВ (Раздел 5)");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Фактическая прочность бетона R = %.1f МПа%n", ctx.concreteStrengthR);

        // Rb
        System.out.println("\n[2. Расчетное сопротивление бетона сжатию Rb (Таблица 5.1)]");
        ctx.Rb = interpolateRbDetailed(ctx.concreteStrengthR);

        // Rbt
        System.out.println("\n[3. Расчетное сопротивление бетона растяжению Rbt (Таблица 5.1)]");
        ctx.Rbt = interpolateRbtDetailed(ctx.concreteStrengthR);

        // Eb
        System.out.println("\n[4. Модуль упругости бетона Eb (п. 5.1)]");
        ctx.Eb = getEbDetailed(ctx.concreteStrengthR);

        // Rs и Es
        System.out.println("\n[5. Расчетные сопротивления арматуры (п. 5.2)]");
        if (rebarChoice == 1) {
            ctx.Rs = 190;
            ctx.rebarType = "Гладкая (А240)";
        } else {
            ctx.Rs = 355;
            ctx.rebarType = "Периодического профиля (А400)";
        }
        ctx.Es = 206000;
        System.out.printf("   Тип арматуры: %s%n", ctx.rebarType);
        System.out.printf("   Расчетное сопротивление растяжению Rs = %.0f МПа (принято по табл. 5.2)%n", ctx.Rs);
        System.out.printf("   Модуль упругости арматуры Es = %.0f МПа (принято по п. 5.2)%n", ctx.Es);

        // n'
        System.out.println("\n[6. Условное отношение модулей упругости n' (п. 5.2)]");
        ctx.nPrime = interpolateNPrimeDetailed(ctx.concreteStrengthR);

        System.out.println("   (Используется при расчетах на выносливость)");
        System.out.println("\n============================================================\n");
    }

    private static double interpolateRbDetailed(double R) {
        double[][] table = {{20, 8.5}, {25, 10.0}, {30, 11.5}, {35, 13.0}, {40, 14.5}};
        return interpolateDetailed(table, R, "Rb");
    }

    private static double interpolateRbtDetailed(double R) {
        double[][] table = {{20, 0.65}, {25, 0.85}, {30, 1.00}, {35, 1.10}, {40, 1.20}};
        return interpolateDetailed(table, R, "Rbt");
    }

    private static double getEbDetailed(double R) {
        if (R < 25) {
            System.out.printf("   Так как R = %.1f МПа меньше минимального табличного 25,0 МПа:%n", R);
            System.out.println("   Принимаем Eb = 27,00 (по нижней границе таблицы)");
            System.out.printf("   Переводим в МПа: Eb = 27,0 · 1000 = 27000 МПа%n");
            return 27000;
        }
        if (R < 30) {
            System.out.printf("   Так как R = %.1f МПа находится между табличными 25,0 и 30,0 МПа:%n", R);
            System.out.println("   Принимаем Eb = 30,00");
            System.out.printf("   Переводим в МПа: Eb = 30,0 · 1000 = 30000 МПа%n");
            return 30000;
        }
        if (R < 35) {
            System.out.printf("   Так как R = %.1f МПа находится между табличными 30,0 и 35,0 МПа:%n", R);
            System.out.println("   Принимаем Eb = 32,50");
            System.out.printf("   Переводим в МПа: Eb = 32,5 · 1000 = 32500 МПа%n");
            return 32500;
        }
        if (R < 40) {
            System.out.printf("   Так как R = %.1f МПа находится между табличными 35,0 и 40,0 МПа:%n", R);
            System.out.println("   Принимаем Eb = 34,50");
            System.out.printf("   Переводим в МПа: Eb = 34,5 · 1000 = 34500 МПа%n");
            return 34500;
        }
        System.out.printf("   Так как R = %.1f МПа больше максимального табличного 40,0 МПа:%n", R);
        System.out.println("   Принимаем Eb = 36,00 (по верхней границе таблицы)");
        System.out.printf("   Переводим в МПа: Eb = 36,0 · 1000 = 36000 МПа%n");
        return 36000;
    }

    private static double interpolateNPrimeDetailed(double R) {
        double[][] table = {{20, 25}, {30, 20}, {40, 15}};
        return interpolateDetailed(table, R, "n'");
    }

    private static double interpolateDetailed(double[][] table, double x, String paramName) {
        if (x <= table[0][0]) {
            System.out.printf("   Так как R = %.1f МПа меньше минимального табличного %.1f МПа:%n", x, table[0][0]);
            System.out.printf("   Принимаем %s = %.2f (по нижней границе таблицы)%n", paramName, table[0][1]);
            return table[0][1];
        }
        if (x >= table[table.length - 1][0]) {
            System.out.printf("   Так как R = %.1f МПа больше максимального табличного %.1f МПа:%n", x, table[table.length - 1][0]);
            System.out.printf("   Принимаем %s = %.2f (по верхней границе таблицы)%n", paramName, table[table.length - 1][1]);
            return table[table.length - 1][1];
        }

        for (int i = 0; i < table.length - 1; i++) {
            if (x >= table[i][0] && x <= table[i + 1][0]) {
                System.out.printf("   Так как R = %.1f МПа находится между табличными %.1f и %.1f МПа:%n",
                        x, table[i][0], table[i + 1][0]);
                System.out.printf("   %s(%.1f) = %.2f,  %s(%.1f) = %.2f%n",
                        paramName, table[i][0], table[i][1], paramName, table[i + 1][0], table[i + 1][1]);
                System.out.println("   Применяем линейную интерполяцию:");
                System.out.printf("   %s = %.2f + (%.2f - %.2f) · (%.1f - %.1f) / (%.1f - %.1f)%n",
                        paramName, table[i][1], table[i + 1][1], table[i][1], x, table[i][0], table[i + 1][0], table[i][0]);
                double result = table[i][1] + (table[i + 1][1] - table[i][1]) * (x - table[i][0]) / (table[i + 1][0] - table[i][0]);
                System.out.printf("   %s = %.2f + %.2f · %.2f = %.2f%n",
                        paramName, table[i][1], table[i + 1][1] - table[i][1], (x - table[i][0]) / (table[i + 1][0] - table[i][0]), result);
                return result;
            }
        }
        return table[0][1];
    }
}