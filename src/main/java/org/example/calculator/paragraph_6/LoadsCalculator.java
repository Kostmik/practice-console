package org.example.calculator.paragraph_6;

import org.example.context.BridgeContext;
import org.example.model.TrackType;
import org.example.util.Interpolation;

public class LoadsCalculator {

    // =====================================================================
    // П. 6.1 — Удельный вес балласта
    // =====================================================================

    public static void setBallastDensity(BridgeContext ctx, boolean woodenSleepers) {
        ctx.gammaBallastWithTrack = woodenSleepers ? 17.17 : 19.6;
    }

    // =====================================================================
    // П. 6.1 - 6.3 — Сбор постоянных нагрузок
    // =====================================================================

    public static void calculateSlabLoads(BridgeContext ctx, double hSlab) {
        ctx.ppSlab = hSlab * ctx.gammaReinforcedConcrete;
        ctx.pbSlab = ctx.ballastThickness * ctx.gammaBallastWithTrack;
    }

    public static void calculateBeamLoads(BridgeContext ctx, double vConcrete,
                                          double pDevices, double sBallast, int mBeams) {
        double totalWeight = vConcrete * ctx.gammaReinforcedConcrete + pDevices;
        ctx.ppBeam = totalWeight / (mBeams * ctx.spanLength);
        ctx.pbBeam = (sBallast * ctx.gammaBallastWithTrack) / mBeams;
    }

    public static void printLoadsReport(BridgeContext ctx, double hSlab, double vConcrete,
                                        double pDevices, double sBallast, int mBeams) {
        calculateSlabLoads(ctx, hSlab);
        calculateBeamLoads(ctx, vConcrete, pDevices, sBallast, mBeams);

        System.out.println("============================================================");
        System.out.println(" СБОР ПОСТОЯННЫХ НАГРУЗОК [п. 6.1 - 6.3]");
        System.out.println("============================================================");

        System.out.println("\n[1. Удельные веса материалов (п. 6.1)]");
        System.out.printf("   Железобетон:              γ_rc = %.2f кН/м³%n", ctx.gammaReinforcedConcrete);
        System.out.printf("   Балласт с частями пути:   γ_b  = %.2f кН/м³%n", ctx.gammaBallastWithTrack);

        System.out.println("\n[2. Нагрузки на ПЛИТУ БАЛЛАСТНОГО КОРЫТА (на 1 м вдоль оси моста)]");
        System.out.println("   Нагрузка от веса плиты pp:");
        System.out.printf("      pp = h_slab · γ_rc = %.2f · %.2f = %.2f кН/м%n",
                hSlab, ctx.gammaReinforcedConcrete, ctx.ppSlab);
        System.out.println("   Нагрузка от веса балласта pb:");
        System.out.printf("      pb = hb · γ_b = %.2f · %.2f = %.2f кН/м%n",
                ctx.ballastThickness, ctx.gammaBallastWithTrack, ctx.pbSlab);

        System.out.println("\n[3. Нагрузки на ГЛАВНУЮ БАЛКУ (на 1 м пролёта)]");
        double totalWeight = vConcrete * ctx.gammaReinforcedConcrete + pDevices;
        System.out.println("   Нагрузка от веса ПС с обустройствами pp:");
        System.out.println("      pp = (V · γ_rc + P_dev) / (m · l)");
        System.out.printf("      pp = (%.2f · %.2f + %.2f) / (%d · %.2f) = %.2f кН/м%n",
                vConcrete, ctx.gammaReinforcedConcrete, pDevices, mBeams, ctx.spanLength, ctx.ppBeam);
        System.out.println("   Нагрузка от веса балласта pb:");
        System.out.println("      pb = (S_b · γ_b) / m");
        System.out.printf("      pb = (%.2f · %.2f) / %d = %.2f кН/м%n",
                sBallast, ctx.gammaBallastWithTrack, mBeams, ctx.pbBeam);

        System.out.println("\n[4. Коэффициенты надёжности по нагрузке (п. 6.3, 6.5)]");
        System.out.printf("   Для веса ж/б (np):           %.2f%n", ctx.np);
        System.out.printf("   Для веса балласта (np'):     %.2f%n", ctx.npPrime);
        System.out.printf("   Для временной нагрузки (nk): %.2f%n", ctx.nk);

    }

    // =====================================================================
    // П. 6.4 — Динамический коэффициент (1+μ)
    // =====================================================================

    public static double calculateDynamicCoeff(BridgeContext ctx, double lambda) {
        double hb = ctx.ballastThickness;

        if (hb >= 1.0) {
            return 1.0;
        }

        double[] hbPoints = {0.25, 0.40, 0.55, 0.70, 0.85, 1.00};
        double[] mu0Points = (ctx.trackType == TrackType.LINKED)
                ? new double[]{10.0, 8.0, 6.0, 4.0, 2.0, 0.0}
                : new double[]{5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

        double mu0 = Interpolation.linear(hbPoints, mu0Points, hb);
        return 1.0 + mu0 / (20.0 + lambda);
    }

    public static double calculateDynamicCoeffForSlab(BridgeContext ctx, boolean useMaxCoefficient, double lambda) {
        if (useMaxCoefficient) {
            double hb = ctx.ballastThickness;
            if (hb >= 1.0) {
                return 1.0;
            }

            double[] hbPoints = {0.25, 0.40, 0.55, 0.70, 0.85, 1.00};
            double[] mu0Points = (ctx.trackType == TrackType.LINKED)
                    ? new double[]{10.0, 8.0, 6.0, 4.0, 2.0, 0.0}
                    : new double[]{5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

            double mu0 = Interpolation.linear(hbPoints, mu0Points, hb);
            return 1.0 + mu0 / 20.0;
        } else {
            return calculateDynamicCoeff(ctx, lambda);
        }
    }

    public static void printDynamicCoeffReport(BridgeContext ctx, double lambda, String elementName) {
        printDynamicCoeffReport(ctx, lambda, elementName, false);
    }

    public static void printDynamicCoeffReport(BridgeContext ctx, double lambda, String elementName, boolean useMaxCoefficient) {
        double hb = ctx.ballastThickness;

        System.out.println("============================================================");
        System.out.printf(" РАСЧЁТ ДИНАМИЧЕСКОГО КОЭФФИЦИЕНТА (1+μ) [п. 6.4]%n");
        System.out.printf(" Элемент: %s%n", elementName);
        System.out.println("============================================================");

        System.out.println("\n[1. Исходные данные]");
        System.out.printf("   Толщина балласта hb = %.2f м%n", hb);

        if (useMaxCoefficient) {
            System.out.println("   Режим: МАКСИМАЛЬНОЕ ЗНАЧЕНИЕ (как в Приложении 4)");
            System.out.println("   Длина загружения λ не используется (принято λ → 0)");
        } else {
            System.out.printf("   Длина загружения λ  = %.2f м%n", lambda);
        }

        System.out.printf("   Тип пути            = %s%n", ctx.trackType);

        if (hb >= 1.0) {
            System.out.println("\n[2. Анализ условий]");
            System.out.println("   Так как hb >= 1.0 м, согласно п. 6.4 коэффициент = 1.0");
            System.out.printf("%n>>> РЕЗУЛЬТАТ: 1+μ = 1.000%n");
            System.out.println("============================================================\n");
            return;
        }

        double[] hbPoints = {0.25, 0.40, 0.55, 0.70, 0.85, 1.00};
        double[] mu0Points = (ctx.trackType == TrackType.LINKED)
                ? new double[]{10.0, 8.0, 6.0, 4.0, 2.0, 0.0}
                : new double[]{5.0, 4.0, 3.0, 2.0, 1.0, 0.0};

        System.out.println("\n[2. Определение базового коэффициента μ0 (Таблица п. 6.4)]");

        double mu0 = 0;
        boolean isExactMatch = false;

        // Проверяем точное совпадение
        for (int i = 0; i < hbPoints.length; i++) {
            if (Math.abs(hb - hbPoints[i]) < 0.001) {
                System.out.printf("   Так как hb = %.2f м точно совпадает с табличным значением:%n", hb);
                System.out.printf("   μ0 = %.1f%n", mu0Points[i]);
                mu0 = mu0Points[i];
                isExactMatch = true;
                break;
            }
        }

        // Если нет точного совпадения, показываем интерполяцию
        if (!isExactMatch) {
            int lowerIdx = -1;
            for (int i = 0; i < hbPoints.length - 1; i++) {
                if (hb > hbPoints[i] && hb < hbPoints[i + 1]) {
                    lowerIdx = i;
                    break;
                }
            }

            double hb1 = hbPoints[lowerIdx];
            double hb2 = hbPoints[lowerIdx + 1];
            double mu0_1 = mu0Points[lowerIdx];
            double mu0_2 = mu0Points[lowerIdx + 1];

            System.out.printf("   Так как hb = %.2f м находится между табличными %.2f и %.2f м:%n", hb, hb1, hb2);
            System.out.printf("   μ0(%.2f) = %.1f, μ0(%.2f) = %.1f%n", hb1, mu0_1, hb2, mu0_2);
            System.out.println("   Применяем линейную интерполяцию:");
            System.out.printf("   μ0 = μ0(%.2f) + (μ0(%.2f) - μ0(%.2f)) · (hb - %.2f) / (%.2f - %.2f)%n",
                    hb1, hb2, hb1, hb1, hb2, hb1);
            System.out.printf("   μ0 = %.1f + (%.1f - %.1f) · (%.2f - %.2f) / (%.2f - %.2f)%n",
                    mu0_1, mu0_2, mu0_1, hb, hb1, hb2, hb1);

            mu0 = Interpolation.linear(hbPoints, mu0Points, hb);
            System.out.printf("   μ0 = %.3f%n", mu0);
        }

        System.out.printf("%n[3. Расчёт по формуле]%n");

        if (useMaxCoefficient) {
            System.out.println("   Формула: 1+μ = 1 + μ0 / 20 (при λ → 0)");
            System.out.printf("   Подстановка: 1+μ = 1 + %.3f / 20%n", mu0);
            System.out.printf("   1+μ = 1 + %.3f = %.3f%n", mu0 / 20.0, (1.0 + mu0 / 20.0));
        } else {
            System.out.println("   Формула: 1+μ = 1 + μ0 / (20 + λ)");
            System.out.printf("   Подстановка: 1+μ = 1 + %.3f / (20 + %.2f)%n", mu0, lambda);
            System.out.printf("   1+μ = 1 + %.3f / %.2f = 1 + %.3f%n",
                    mu0, (20 + lambda), mu0 / (20 + lambda));
        }

        double result = useMaxCoefficient ? (1.0 + mu0 / 20.0) : (1.0 + mu0 / (20.0 + lambda));
        System.out.printf("%n>>> ИТОГОВЫЙ КОЭФФИЦИЕНТ: 1+μ = %.3f%n", result);
        System.out.println("============================================================\n");
    }
}