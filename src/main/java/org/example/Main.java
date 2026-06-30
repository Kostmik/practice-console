package org.example;

import org.example.calculator.LoadsCalculator;
import org.example.context.BridgeContext;
import org.example.model.TrackType;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        BridgeContext ctx = new BridgeContext();

        while (true) {
            System.out.println("\n╔══════════════════════════════════════════════════════════╗");
            System.out.println("║  КАЛЬКУЛЯТОР ГРУЗОПОДЪЁМНОСТИ Ж/Б МОСТОВ (РЖД 249/р)     ║");
            System.out.println("╚══════════════════════════════════════════════════════════╝");
            System.out.println("\n1. Рассчитать постоянные нагрузки (п. 6.1 - 6.3)");
            System.out.println("2. Рассчитать динамический коэффициент (п. 6.4)");
            System.out.println("0. Выход");
            System.out.print("\nВыберите пункт меню: ");

            int choice = sc.nextInt();

            if (choice == 0) {
                System.out.println("Завершение работы программы. До свидания!");
                break;
            }

            // Ввод общих исходных данных
            inputCommonData(sc, ctx);

            switch (choice) {
                case 1:
                    calculatePermanentLoads(sc, ctx);
                    break;
                case 2:
                    calculateDynamicCoeff(sc, ctx);
                    break;
                default:
                    System.out.println("Неверный выбор. Попробуйте снова.");
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

        System.out.println("Тип пути:");
        System.out.println("  1 - звеньевой");
        System.out.println("  2 - бесстыковой");
        System.out.print("Выберите (1/2): ");
        int trackChoice = sc.nextInt();
        ctx.trackType = (trackChoice == 2) ? TrackType.CONTINUOUS : TrackType.LINKED;

        System.out.print("Шпалы деревянные? (0 - нет, ж/б; 1 - да, деревянные): ");
        boolean woodenSleepers = sc.nextInt() == 1;
        LoadsCalculator.setBallastDensity(ctx, woodenSleepers);

        System.out.print("Фактическая прочность бетона R (МПа) [например, 23.0]: ");
        ctx.concreteStrengthR = sc.nextDouble();
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

        // Считаем для ГЛАВНОЙ БАЛКИ
        LoadsCalculator.printDynamicCoeffReport(ctx, ctx.spanLength, "ГЛАВНАЯ БАЛКА");

        // Считаем для ПЛИТЫ БАЛЛАСТНОГО КОРЫТА
        LoadsCalculator.printDynamicCoeffReport(ctx, 2.0, "ПЛИТА БАЛЛАСТНОГО КОРЫТА");
    }
}