package org.example;

import org.example.calculator.LoadsCalculator;
import org.example.calculator.MaterialCalculator;
import org.example.calculator.ShareCalculator;
import org.example.calculator.SlabCalculator;
import org.example.context.BridgeContext;
import org.example.model.TrackType;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BridgeContext ctx = new BridgeContext();

        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║  КАЛЬКУЛЯТОР ГРУЗОПОДЪЁМНОСТИ Ж/Б МОСТОВ (РЖД 249/р)     ");
            System.out.println("╚══════════════════════════════════════════════════════════╝");

            System.out.println("\n1. Рассчитать характеристики материалов (Раздел 5)");
            System.out.println("2. Рассчитать постоянные нагрузки (п. 6.1 - 6.3)");
            System.out.println("3. Рассчитать динамический коэффициент (п. 6.4)");
            System.out.println("4. Рассчитать доли временной нагрузки (п. 6.6 - 6.7)");
            System.out.println("5. Рассчитать плиту балластного корыта на прочность (п. 7.2)");
            System.out.println("0. Выход");
            System.out.print("\nВыберите пункт меню: ");

            int choice = sc.nextInt();

            if (choice == 0) {
                System.out.println("Завершение работы программы. До свидания!");
                break;
            }

            inputCommonData(sc, ctx);

            switch (choice) {
                case 1:
                    calculateMaterials(sc, ctx);
                    break;
                case 2:
                    calculatePermanentLoads(sc, ctx);
                    break;
                case 3:
                    calculateDynamicCoeff(sc, ctx);
                    break;
                case 4:
                    calculateShare(sc, ctx);
                    break;
                case 5:
                    calculateSlabStrength(sc, ctx);
                    break;
                default:
                    System.out.println("Неверный выбор.");
            }

            System.out.print("\nНажмите Enter для продолжения...");
            sc.nextLine();
            sc.nextLine();
        }

        sc.close();
    }

    private static void inputCommonData(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ВВОД ИСХОДНЫХ ДАННЫХ ---");

        System.out.print("Расчётный пролёт l (м) [например, 10.8]: ");
        ctx.spanLength = sc.nextDouble();

        System.out.print("Толщина балласта под шпалой hb (м) [например, 0.25]: ");
        ctx.ballastThickness = sc.nextDouble();

        System.out.println("Тип пути: 1 - звеньевой, 2 - бесстыковой");
        System.out.print("Выберите (1/2): ");
        ctx.trackType = (sc.nextInt() == 2) ? TrackType.CONTINUOUS : TrackType.LINKED;

        System.out.println("Тип шпал: 1 - ж/б, 2 - деревянные");
        System.out.print("Выберите (1/2): ");
        boolean woodenSleepers = sc.nextInt() == 2;
        LoadsCalculator.setBallastDensity(ctx, woodenSleepers);

        System.out.print("Фактическая прочность бетона R (МПа) [например, 23.0]: ");
        ctx.concreteStrengthR = sc.nextDouble();

        // Геометрия балок (нужна для пункта 4)
        System.out.print("Расстояние между осями главных балок c (м) [например, 1.8]: ");
        ctx.distanceBetweenBeams = sc.nextDouble();

        System.out.print("Смещение оси пути у левой опоры e1 (м) [например, 0.2]: ");
        ctx.trackOffsetLeft = sc.nextDouble();

        System.out.print("Смещение оси пути у правой опоры e2 (м) [например, 0.2]: ");
        ctx.trackOffsetRight = sc.nextDouble();
    }

    private static void calculateMaterials(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ХАРАКТЕРИСТИКИ АРМАТУРЫ ---");
        System.out.println("Тип рабочей арматуры: 1 - Гладкая (А240), 2 - Периодическая (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();
        System.out.println();
        MaterialCalculator.printMaterialsReport(ctx, rebarChoice);
    }

    private static void calculatePermanentLoads(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ГЕОМЕТРИЯ ДЛЯ СБОРА НАГРУЗОК ---");
        System.out.print("Толщина плиты h_slab (м) [например, 0.26]: ");
        double hSlab = sc.nextDouble();

        System.out.print("Объём ж/б пролётного строения V_concrete (м³) [например, 30.6]: ");
        double vConcrete = sc.nextDouble();

        System.out.print("Вес обустройств (лотки, перила) P_devices (кН) [если нет, 0]: ");
        double pDevices = sc.nextDouble();

        System.out.print("Площадь поперечного сечения балластной призмы S_ballast (м²) [например, 2.06]: ");
        double sBallast = sc.nextDouble();

        System.out.print("Количество главных балок m (обычно 2): ");
        int mBeams = sc.nextInt();
        System.out.println();

        LoadsCalculator.printLoadsReport(ctx, hSlab, vConcrete, pDevices, sBallast, mBeams);
    }

    private static void calculateDynamicCoeff(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЁТ ДИНАМИЧЕСКОГО КОЭФФИЦИЕНТА (1+μ) ---");

        // Главная балка
        double muBeam = LoadsCalculator.calculateDynamicCoeff(ctx, ctx.spanLength);
        ctx.dynamicCoeffBeam = muBeam;
        LoadsCalculator.printDynamicCoeffReport(ctx, ctx.spanLength, "ГЛАВНАЯ БАЛКА", false);

        // Плита
        System.out.println("\nДля ПЛИТЫ БАЛЛАСТНОГО КОРЫТА:");
        System.out.println("  1 - Максимальное значение (λ → 0)");
        System.out.println("  2 - Расчёт по формуле с вводом λ");
        System.out.print("Выберите (1/2): ");
        int slabChoice = sc.nextInt();

        double muSlab;
        if (slabChoice == 1) {
            muSlab = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0);
            LoadsCalculator.printDynamicCoeffReport(ctx, 0.0, "ПЛИТА БАЛЛАСТНОГО КОРЫТА", true);
        } else {
            System.out.print("Введите длину загружения λ для плиты (м) [например, 2.0]: ");
            double lambdaSlab = sc.nextDouble();
            if (lambdaSlab <= 0) lambdaSlab = 2.0;
            muSlab = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, false, lambdaSlab);
            LoadsCalculator.printDynamicCoeffReport(ctx, lambdaSlab, "ПЛИТА БАЛЛАСТНОГО КОРЫТА", false);
        }
        ctx.dynamicCoeffSlab = muSlab;
    }

    private static void calculateShare(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЁТ ДОЛЕЙ ВРЕМЕННОЙ НАГРУЗКИ ---");
        System.out.println("Тип пролетного строения:");
        System.out.println("  1 - Монолитное (п. 6.6)");
        System.out.println("  2 - Сборное (п. 6.7)");
        System.out.print("Выберите (1/2): ");
        int typeChoice = sc.nextInt();
        boolean isMonolithic = (typeChoice == 1);

        System.out.print("Введите относительную координату сечения x/l (от 0 до 1) [например, 0.5]: ");
        double xRatio = sc.nextDouble();

        if (isMonolithic) {
            ShareCalculator.calculateMonolithic(ctx, xRatio);
        } else {
            ShareCalculator.calculatePrecast(ctx, xRatio);
        }

        ShareCalculator.printReport(ctx, xRatio, isMonolithic);
    }

    private static void calculateSlabStrength(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ГЕОМЕТРИЯ И АРМАТУРА ПЛИТЫ ---");

        System.out.print("Высота плиты h (м) [например, 0.26]: ");
        ctx.slabHeight = sc.nextDouble();

        System.out.print("Расстояние до центра растянутой арматуры as (м) [например, 0.026]: ");
        ctx.as_tensile = sc.nextDouble();

        System.out.print("Расстояние до центра сжатой арматуры as' (м) [например, 0.026]: ");
        ctx.as_compressive = sc.nextDouble();

        System.out.print("Площадь растянутой арматуры As (м²) [например, 0.000905 для 8Ø12]: ");
        ctx.As_tensile = sc.nextDouble();

        System.out.print("Площадь сжатой арматуры As' (м²) [если нет, введите 0]: ");
        ctx.As_compressive = sc.nextDouble();

        System.out.println("\n--- ГЕОМЕТРИЯ ПОПЕРЕЧНОГО СЕЧЕНИЯ ---");
        System.out.print("Расстояние между внутренними гранями ребер lp (м) [например, 1.2]: ");
        ctx.lp = sc.nextDouble();

        System.out.print("Расстояние между наружными гранями ребер B (м) [например, 2.4]: ");
        ctx.B = sc.nextDouble();

        System.out.print("Длина шпалы ls (м) [например, 2.7]: ");
        ctx.ls = sc.nextDouble();

        System.out.print("Расстояние от ребра до левого борта l'b (м) [например, 1.05]: ");
        ctx.lb_prime = sc.nextDouble();

        System.out.print("Расстояние от ребра до правого борта l''b (м) [например, 1.05]: ");
        ctx.lb_doubleprime = sc.nextDouble();

        System.out.print("Толщина балласта под левым концом шпалы h'b (м) [например, 0.35]: ");
        ctx.hb_prime = sc.nextDouble();

        System.out.print("Толщина балласта под правым концом шпалы h''b (м) [например, 0.35]: ");
        ctx.hb_doubleprime = sc.nextDouble();

        System.out.println("\n--- МОМЕНТЫ ОТ ПОСТОЯННЫХ НАГРУЗОК ---");
        System.out.print("Момент в монолитном участке Mp (кН·м) [введите 0 для авто-расчета]: ");
        ctx.Mp_monolithic = sc.nextDouble();

        System.out.print("Момент во внешней консоли Mp (кН·м) [например, 12.5]: ");
        ctx.Mp_external_cantilever = sc.nextDouble();

        System.out.println();
        SlabCalculator.calculateAndPrintReport(ctx);
    }
}