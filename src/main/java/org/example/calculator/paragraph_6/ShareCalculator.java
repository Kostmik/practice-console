package org.example.calculator.paragraph_6;

import org.example.context.BridgeContext;

public class ShareCalculator {

    /**
     * Расчет долей временной нагрузки для МОНОЛИТНОГО пролетного строения (п. 6.6)
     * @param ctx контекст
     * @param xRatio отношение координаты сечения к пролету (x/l)
     */
    public static void calculateMonolithic(BridgeContext ctx, double xRatio) {
        double c = ctx.distanceBetweenBeams;
        double e1 = ctx.trackOffsetLeft;
        double e2 = ctx.trackOffsetRight;

        // Коэффициенты A и B из Таблицы 6.1
        double A1 = 0.3;
        double A2, B1, B2;

        // Определяем A2 в зависимости от x/l (по Таблице 6.1)
        if (xRatio <= 0.25) {
            A2 = -0.1;
        } else if (xRatio > 0.25 && xRatio <= 0.5) {
            A2 = 0.1; // В методичке для 0.25 < x <= 0.5
        } else {
            // Для x > 0.5 симметрия: A2 = -A2(для 1-x)
            // Но упростим: для главной балки в середине пролета (x=0.5) берем 0.1
            // Если нужно точно по таблице для x > 0.75 (там -0.1)
            if (xRatio >= 0.75) {
                A2 = -0.1;
            } else {
                A2 = 0.1; // Промежуточное значение для простоты, в идеале нужно интерполировать
            }
        }

        // Определяем B1, B2 (по Таблице 6.1 для поперечной силы Q)
        B1 = 0.6;
        if (xRatio < 0.5) {
            B2 = 0.15;
        } else {
            B2 = -0.15; // Для x > 0.5 знак меняется
        }

        // Формулы 6.3 и 6.4
        // Для Балки 1 (знак + перед скобкой с разностью)
        // Формула 6.3: 0.5 + 1/(2c) * [A1(e1+e2) + A2(e1-e2)]
        double termM = A1 * (e1 + e2) + A2 * (e1 - e2);
        ctx.epsilonM_Beam1 = 0.5 + termM / (2 * c);
        ctx.epsilonM_Beam2 = 0.5 - termM / (2 * c); // Для балки 2 знак перед A2(e1-e2) меняется

        // Формула 6.4: 0.5 + 1/(2c) * [B1(e1+e2) + B2(e1-e2)]
        double termQ = B1 * (e1 + e2) + B2 * (e1 - e2);
        ctx.epsilonQ_Beam1 = 0.5 + termQ / (2 * c);
        ctx.epsilonQ_Beam2 = 0.5 - termQ / (2 * c); // Для балки 2 знак меняется
    }

    /**
     * Расчет долей для СБОРНОГО пролетного строения (п. 6.7)
     */
    public static void calculatePrecast(BridgeContext ctx, double xRatio) {
        double c = ctx.distanceBetweenBeams;
        double e1 = ctx.trackOffsetLeft;
        double e2 = ctx.trackOffsetRight;

        // Формулы 6.6 - 6.8
        if (xRatio < 0.5) {
            // Формула 6.6 для M
            ctx.epsilonM_Beam1 = 0.5 + (e1 + e2) / (2 * c);
            ctx.epsilonM_Beam2 = 0.5 - (e1 + e2) / (2 * c);

            // Формула 6.7 для Q
            ctx.epsilonQ_Beam1 = 0.5 + (e1 + 2 * e2) / (3 * c); // Внимание: тут e1+2e2 для балки 1
            ctx.epsilonQ_Beam2 = 0.5 - (e1 + 2 * e2) / (3 * c);
        } else {
            // Формула 6.6 для M (та же)
            ctx.epsilonM_Beam1 = 0.5 + (e1 + e2) / (2 * c);
            ctx.epsilonM_Beam2 = 0.5 - (e1 + e2) / (2 * c);

            // Формула 6.8 для Q (знаки меняются)
            ctx.epsilonQ_Beam1 = 0.5 + (2 * e1 + e2) / (3 * c);
            ctx.epsilonQ_Beam2 = 0.5 - (2 * e1 + e2) / (3 * c);
        }
    }

    /**
     * Вывод подробного отчета
     */
    public static void printReport(BridgeContext ctx, double xRatio, boolean isMonolithic) {
        //System.out.println("============================================================");
        //System.out.println(" РАСЧЕТ ДОЛЕЙ ВРЕМЕННОЙ НАГРУЗКИ [п. 6." + (isMonolithic ? "6" : "7") + "]");
        System.out.printf(" Тип: %s пролетное строение%n", isMonolithic ? "Монолитное" : "Сборное");
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Расстояние между осями балок c = %.2f м%n", ctx.distanceBetweenBeams);
        System.out.printf("   Смещение пути у левой опоры e1 = %.2f м%n", ctx.trackOffsetLeft);
        System.out.printf("   Смещение пути у правой опоры e2 = %.2f м%n", ctx.trackOffsetRight);
        System.out.printf("   Относительная координата сечения x/l = %.2f%n", xRatio);

        System.out.println("\n[2. Коэффициенты (из Таблицы 6.1)]");
        System.out.printf("   Для момента (A1, A2): ");
        if (isMonolithic) {
            // Пересчитаем для вывода
            double A2 = (xRatio <= 0.25) ? -0.1 : ((xRatio >= 0.75) ? -0.1 : 0.1);
            System.out.printf("A1=0.3, A2=%.1f%n", A2);
        } else {
            System.out.println("Для сборных мостов коэффициенты не используются (формулы 6.6-6.8)");
        }

        System.out.printf("   Для поперечной силы (B1, B2): ");
        if (isMonolithic) {
            double B2 = (xRatio < 0.5) ? 0.15 : -0.15;
            System.out.printf("B1=0.6, B2=%.2f%n", B2);
        } else {
            System.out.println("Для сборных мостов коэффициенты не используются");
        }

        System.out.println("\n[3. Результаты расчета долей ε]");
        System.out.printf("   Балка 1 (левая): εM = %.3f, εQ = %.3f%n", ctx.epsilonM_Beam1, ctx.epsilonQ_Beam1);
        System.out.printf("   Балка 2 (правая): εM = %.3f, εQ = %.3f%n", ctx.epsilonM_Beam2, ctx.epsilonQ_Beam2);

    }
}