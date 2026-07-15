package org.example;

import org.example.calculator.*;
import org.example.calculator.paragraph_15.InspectionCalculator;
import org.example.calculator.paragraph_15.InspectionInput;
import org.example.calculator.paragraph_15.InspectionOutput;
import org.example.calculator.paragraph_8.BeamCalculator;
import org.example.calculator.paragraph_8.BetaCalculator;
import org.example.calculator.paragraph_8.SlabCantileverCalculator;
import org.example.calculator.paragraph_8.SlabMonolithicCalculator;
import org.example.calculator.paragraph_9.PrestressedBeamCalculator;
import org.example.context.BridgeContext;
import org.example.model.RebarType;
import org.example.model.TrackType;
import org.example.calculator.paragraph_10.BoardInput;
import org.example.calculator.paragraph_10.LongitudinalBoardCalculator;
import org.example.calculator.paragraph_11.CurvedSpanInput;
import org.example.calculator.paragraph_11.CurvedSpanCalculator;
import org.example.calculator.paragraph_12.DefectInput;
import org.example.calculator.paragraph_12.DefectCalculator;
import org.example.calculator.paragraph_13.CarbonInput;
import org.example.calculator.paragraph_13.CarbonTables;
import org.example.calculator.paragraph_13.CarbonCalculator;
import org.example.calculator.paragraph_14.CarbonRecommendationCalculator;
import org.example.calculator.paragraph_14.CarbonRecommendationTables;

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
            System.out.println("6. Рассчитать монолитный участок плиты (формула 8.2 - 8.3)");
            System.out.println("7. Рассчитать внешнюю консоль плиты (формула 8.1)");
            System.out.println("8. Рассчитать главную балку (формула 8.4)");
            System.out.println("9. Рассчитать главную балку с напрягаемой арматурой (Раздел 9)");
            System.out.println("10. Рассчитать продольный борт (Раздел 10)");
            System.out.println("11. Рассчитать долю нагрузки на балку на кривой (Раздел 11)");
            System.out.println("12. Учесть влияние дефектов пролётного строения (Раздел 12)");
            System.out.println("13. Рассчитать усиление углеволокном (Раздел 13)");
            System.out.println("14. Получить рекомендации по усилению углеволокном (Раздел 14)");
            System.out.println("15. Обследование и испытание пролётных строений (Раздел 15)");
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
                case 6:
                    calculateSlabMonolithic(sc, ctx);
                    break;
                case 7:
                    calculateSlabCantilever(sc, ctx);
                    break;
                case 8:
                    calculateBeam(sc, ctx);
                    break;
                case 9:
                    calculatePrestressedBeam(sc, ctx);
                    break;
                case 10:
                    calculateBoard(sc, ctx);
                    break;
                case 11:
                    calculateCurvedSpan(sc, ctx);
                    break;
                case 12:
                    calculateDefects(sc, ctx);
                    break;
                case 13:
                    calculateCarbon(sc, ctx);
                    break;
                case 14:
                    calculateCarbonRecommendations(sc, ctx);
                    break;
                case 15:
                    calculateInspection(sc, ctx);
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

    private static void calculateBetaOnly(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ КОЭФФИЦИЕНТА β (ФОРМУЛА 8.3) ---");

        System.out.println("\n[1. Исходные данные]");
        System.out.print("Год выпуска норм проектирования (например, 1931): ");
        ctx.designYear = sc.nextInt();

        System.out.println("Тип арматуры:");
        System.out.println("  1 - Гладкая (А240)");
        System.out.println("  2 - Периодического профиля (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();

        System.out.print("Рассчетную ширину плиту b (м) [например, 1.0]: ");
        ctx.b = sc.nextDouble();

        System.out.print("Длина распределения временной нагрузки (м) [например, 3.4]: ");
        ctx.l_o = sc.nextDouble();

        System.out.print("Коэффициент надежности по назначению [например, 1.1]: ");
        ctx.l_o = sc.nextDouble();

        RebarType rebarType;
        if (rebarChoice == 1) {
            rebarType = RebarType.SMOOTH;
        } else {
            rebarType = RebarType.RIBBED;
        }

        System.out.print("Относительное изменение площади арматуры j (1.0 - без дефектов, 0.904 - с дефектами): ");
        ctx.j = sc.nextDouble();

        // Расчет β
        ctx.beta = BetaCalculator.calculateBeta(ctx.designYear, rebarType, ctx.j);

        // Вывод отчета
        BetaCalculator.printBetaReport(ctx.designYear, rebarType, ctx.j);
    }

    private static void calculateSlabMonolithic(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ МОНОЛИТНОГО УЧАСТКА ПЛИТЫ (ФОРМУЛА 8.2) ---");

        System.out.println("\n[1. Исходные данные]");
        System.out.print("Год выпуска норм проектирования (например, 1931): ");
        int designYear = sc.nextInt();

        System.out.println("Тип арматуры:");
        System.out.println("  1 - Гладкая (А240)");
        System.out.println("  2 - Периодического профиля (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();

        RebarType rebarType;
        if (rebarChoice == 1) {
            rebarType = RebarType.SMOOTH;
        } else {
            rebarType = RebarType.RIBBED;
        }

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        System.out.print("Положение вершины линии влияния α (0.0 или 0.5): ");
        double alpha = sc.nextDouble();

        System.out.print("Относительное изменение площади арматуры j (1.0 - без дефектов): ");
        double j = sc.nextDouble();

        System.out.println("\n[2. Параметры, вводимые пользователем]");
        System.out.print("Длина распределения временной нагрузки l0 (м) [например, 3.4]: ");
        double l0 = sc.nextDouble();

        System.out.print("Коэффициент надежности по назначению ηM (например, 1.1): ");
        double etaM = sc.nextDouble();

        // Проверка: есть ли толщина плиты в контексте
        if (ctx.slabHeight <= 0) {
            System.out.print("Толщина плиты h_slab (м) [например, 0.26]: ");
            ctx.slabHeight = sc.nextDouble();
        }

        System.out.println();

        // Расчет
        double k = SlabMonolithicCalculator.calculate(
            ctx, designYear, l0, etaM, rebarType, j, loadType, alpha
        );

        // Вывод отчета
        SlabMonolithicCalculator.printReport(
            ctx, designYear, l0, etaM, rebarType, j, loadType, alpha, k
        );
    }

    private static void calculateSlabCantilever(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ ВНЕШНЕЙ КОНСОЛИ ПЛИТЫ (ФОРМУЛА 8.1) ---");

        System.out.println("\n[1. Исходные данные]");
        System.out.print("Год выпуска норм проектирования (например, 1931): ");
        int designYear = sc.nextInt();

        System.out.println("Тип арматуры:");
        System.out.println("  1 - Гладкая (А240)");
        System.out.println("  2 - Периодического профиля (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();

        RebarType rebarType;
        if (rebarChoice == 1) {
            rebarType = RebarType.SMOOTH;
        } else {
            rebarType = RebarType.RIBBED;
        }

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        System.out.print("Положение вершины линии влияния α (0.0 или 0.5): ");
        double alpha = sc.nextDouble();

        System.out.print("Относительное изменение площади арматуры j (1.0 - без дефектов): ");
        double j = sc.nextDouble();

        System.out.println("\n[2. Параметры, вводимые пользователем]");
        System.out.print("Длина распределения временной нагрузки l0 (м) [например, 3.4]: ");
        double l0 = sc.nextDouble();

        System.out.print("Коэффициент надежности по назначению ηM (например, 1.1): ");
        double etaM = sc.nextDouble();

        System.out.print("Расстояние от ребра до точки приложения нагрузки Δ (м): ");
        double delta = sc.nextDouble();

        System.out.print("Расстояние от ребра до расчетного сечения Z (м): ");
        double Z = sc.nextDouble();

        System.out.print("Длина внешней консоли lk (м): ");
        double lk = sc.nextDouble();

        System.out.print("Нагрузка от веса перил P0 (кН): ");
        double P0 = sc.nextDouble();

        System.out.print("Расстояние до центра тяжести перил lt (м): ");
        double lt = sc.nextDouble();

        System.out.print("Изгибающий момент от постоянных нагрузок Mp (кН·м): ");
        double Mp = sc.nextDouble();

        // Проверка: есть ли толщина плиты в контексте
        if (ctx.slabHeight <= 0) {
            System.out.print("Толщина плиты h_slab (м) [например, 0.26]: ");
            ctx.slabHeight = sc.nextDouble();
        }
        if (ctx.B <= 0) {
            System.out.print("Расстояние между наружными гранями ребер B (м) [например, 2.4]: ");
            ctx.B = sc.nextDouble();
        }
        if (ctx.ls <= 0) {
            System.out.print("Длина шпалы ls (м) [например, 2.7]: ");
            ctx.ls = sc.nextDouble();
        }

        System.out.println();

        // Расчет
        double k = SlabCantileverCalculator.calculate(
            ctx, designYear, l0, etaM, rebarType, j,
            loadType, alpha, delta, Z, lk, P0, lt, Mp
        );

        // Вывод отчета
        SlabCantileverCalculator.printReport(
            ctx, designYear, l0, etaM, rebarType, j,
            loadType, alpha, delta, Z, lk, P0, lt, Mp, k
        );
    }

    /**
     * Метод для расчета главной балки по формуле 8.4
     */
    private static void calculateBeam(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ ГЛАВНОЙ БАЛКИ (ФОРМУЛА 8.4) ---");

        System.out.println("\n[1. Исходные данные]");
        System.out.print("Год выпуска норм проектирования (например, 1931): ");
        int designYear = sc.nextInt();

        System.out.println("Тип арматуры:");
        System.out.println("  1 - Гладкая (А240)");
        System.out.println("  2 - Периодического профиля (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();

        RebarType rebarType;
        if (rebarChoice == 1) {
            rebarType = RebarType.SMOOTH;
        } else {
            rebarType = RebarType.RIBBED;
        }

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        System.out.print("Положение вершины линии влияния α (0.0 или 0.5): ");
        double alpha = sc.nextDouble();

        System.out.print("Относительное изменение площади арматуры j (1.0 - без дефектов): ");
        double j = sc.nextDouble();

        System.out.println("\n[2. Параметры для балки]");
        System.out.print("Доля временной нагрузки на балку εM (из Раздела 6) [например, 0.565]: ");
        double epsilon = sc.nextDouble();

        System.out.print("Количество главных балок m [например, 2]: ");
        int m = sc.nextInt();

        System.out.print("Вес пролетного строения на балку pp (кН/м) [например, 34.0]: ");
        double pp = sc.nextDouble();

        System.out.print("Вес балласта на балку pb (кН/м) [например, 20.6]: ");
        double pb = sc.nextDouble();

        System.out.println();

        // Расчет
        double k = BeamCalculator.calculate(
            ctx, designYear, rebarType, j, loadType, alpha,
            epsilon, m, pp, pb
        );

        // Вывод отчета
        BeamCalculator.printReport(
            ctx, designYear, rebarType, j, loadType, alpha,
            epsilon, m, pp, pb, k
        );
    }

    private static void calculatePrestressedBeam(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ ГЛАВНОЙ БАЛКИ С НАПРЯГАЕМОЙ АРМАТУРОЙ (РАЗДЕЛ 9) ---");
        System.out.println("\n[1. Исходные данные и материалы]");
        System.out.println("Тип арматуры: 1 - Гладкая (А240), 2 - Периодического профиля (А400)");
        System.out.print("Выберите (1/2): ");
        int rebarChoice = sc.nextInt();
        RebarType rebarType = (rebarChoice == 1) ? RebarType.SMOOTH : RebarType.RIBBED;

        System.out.print("Расчетное сопротивление бетона Rb (МПа) [например, 16.5]: ");
        double Rb = sc.nextDouble();
        System.out.print("Расчетное сопротивление напрягаемой арматуры Rp (МПа) [например, 1060]: ");
        double Rp = sc.nextDouble();

        System.out.print("Предварительное напряжение в растянутой зоне σ_p2 (МПа) [например, 800]: ");
        double sigmaP2 = sc.nextDouble();
        System.out.print("Предварительное напряжение в сжатой зоне σ'_p2 (МПа) [например, 600]: ");
        double sigmaP2s = sc.nextDouble();

        System.out.println("\n[2. Геометрия сечения]");
        System.out.print("Ширина ребра b (м) [например, 0.26]: ");
        double b = sc.nextDouble();
        System.out.print("Ширина плиты bf (м) [например, 2.45]: ");
        double bf = sc.nextDouble();
        System.out.print("Толщина плиты hf (м) [например, 0.185]: ");
        double hf = sc.nextDouble();
        System.out.print("Рабочая высота сечения h0 (м) [например, 1.42]: ");
        double h0 = sc.nextDouble();

        System.out.println("\n[3. Армирование]");
        System.out.print("Площадь ненапрягаемой растянутой арматуры As (м²) [например, 0.001]: ");
        double As = sc.nextDouble();
        System.out.print("Площадь напрягаемой растянутой арматуры Ap (м²) [например, 0.00658]: ");
        double Ap = sc.nextDouble();
        System.out.print("Площадь сжатой ненапрягаемой арматуры As' (м²) [если нет, 0]: ");
        double As_s = sc.nextDouble();
        System.out.print("Площадь сжатой напрягаемой арматуры Ap' (м²) [если нет, 0]: ");
        double Ap_s = sc.nextDouble();

        System.out.print("Расстояние до центра тяжести сжатой арматуры as' (м) [например, 0.038]: ");
        double as = sc.nextDouble();
        System.out.print("Расстояние до центра тяжести сжатой напрягаемой арматуры ap' (м) [например, 0.08]: ");
        double ap = sc.nextDouble();

        System.out.println("\n[4. Нагрузки и линии влияния (для изгиба)]");
        System.out.print("Доля временной нагрузки на балку εM (из Раздела 6) [например, 0.5]: ");
        double epsilonM = sc.nextDouble();
        System.out.print("Площадь линии влияния изгибающего момента Ω (м²) [например, 40.5]: ");
        double Omega = sc.nextDouble();
        System.out.print("Вес пролетного строения на балку pp (кН/м) [например, 31.6]: ");
        double pp = sc.nextDouble();
        System.out.print("Вес балласта на балку pb (кН/м) [например, 19.6]: ");
        double pb = sc.nextDouble();

        System.out.println("\n[5. Нагрузки и линии влияния (для поперечной силы)]");
        System.out.print("Доля временной нагрузки на балку εQ (из Раздела 6) [например, 0.5]: ");
        double epsilonQ = sc.nextDouble();
        System.out.print("Площадь линии влияния поперечной силы Ωk (м²) [например, 6.93]: ");
        double OmegaK = sc.nextDouble();
        System.out.print("Поперечная сила от постоянных нагрузок Qp (кН) [например, 396.4]: ");
        double Qp = sc.nextDouble();

        System.out.println("\n[6. Параметры для поперечной силы (формула 9.7)]");
        System.out.print("Сумма площадей отогнутых пучков ΣA_pi (м²) [если нет, 0]: ");
        double sumApi = sc.nextDouble();
        System.out.print("Синус угла наклона отогнутых пучков sin α [если нет, 0]: ");
        double sinAlpha = sc.nextDouble();
        System.out.print("Площадь поперечного сечения хомутов A_sw (м²) [например, 0.000308]: ");
        double Asw = sc.nextDouble();
        System.out.print("Длина проекции наклонного сечения c (м) [например, 2.0]: ");
        double c = sc.nextDouble();
        System.out.print("Шаг хомутов s (м) [например, 0.1]: ");
        double s = sc.nextDouble();
        System.out.print("Поперечная сила, воспринимаемая бетоном Q_b (кН) [например, 1182.2]: ");
        double Qb = sc.nextDouble();

        // ====================================================================
        // НОВЫЙ БЛОК: ВВОД ДАННЫХ ДЛЯ РАСЧЕТА НА ВЫНОСЛИВОСТЬ (Раздел 9.3)
        // ====================================================================
        System.out.println("\n[7. Параметры для расчета на выносливость (Раздел 9.3)]");
        System.out.print("Высота балки h (м) [например, 1.55]: ");
        double h = sc.nextDouble();
        System.out.print("Расстояние от нейтральной оси до растянутой грани hc (м) [например, 0.883]: ");
        double hc = sc.nextDouble();
        System.out.print("Приведенный момент инерции I_red (м⁴) [например, 0.320]: ");
        double Ired = sc.nextDouble();
        System.out.print("Отношение модулей упругости n' = Ep/Eb [например, 5.14]: ");
        double nPrime = sc.nextDouble();
        System.out.print("Расстояние до центра тяжести растянутой арматуры au (м) [например, 0.08]: ");
        double au = sc.nextDouble();
        System.out.print("Асимметрия цикла напряжений для бетона ρ_b [например, 0.623]: ");
        double rhoB = sc.nextDouble();
        System.out.print("Асимметрия цикла напряжений для арматуры ρ_p [например, 0.928]: ");
        double rhoP = sc.nextDouble();
        System.out.print("Коэффициент уменьшения динамики Θ [например, 0.931]: ");
        double ThetaFatigue = sc.nextDouble();
        System.out.print("Динамический коэффициент для выносливости (1+μ_0) [например, 1.175]: ");
        double mu0Fatigue = sc.nextDouble();

        // ===== ХАРАКТЕРИСТИКИ МАТЕРИАЛОВ =====
        double Rs = rebarType.getRs();
        double Rsc = Rs;
        double Rpc = 500.0; // По п. 9.1.1 (формула 9.4) Рпс всегда принимается равным 500 МПа

        // ===== 1. ГРАНИЧНАЯ ВЫСОТА СЖАТОЙ ЗОНЫ (ф. 9.1) =====
        double xiY = PrestressedBeamCalculator.calculateXiY(Rb, Rp, sigmaP2, Rpc);

        // ===== 2. ОПРЕДЕЛЕНИЕ ВЫСОТЫ СЖАТОЙ ЗОНЫ x =====
        double xWeb = PrestressedBeamCalculator.calculateXWithWeb(
            Rs, As, Rp, Ap, Rsc, As_s, sigmaP2s, Ap_s, Rb, bf, b, hf);
        double xFlange = PrestressedBeamCalculator.calculateXWithFlange(
            Rs, As, Rp, Ap, Rsc, As_s, sigmaP2s, Ap_s, Rb, bf);

        double xCalc;
        boolean isInWeb;

        if (xFlange <= hf) {
            xCalc = xFlange;
            isInWeb = false;
        } else {
            xCalc = xWeb;
            isInWeb = true;
        }

        if (xCalc > xiY * h0) {
            System.out.println("\n[!] ВНИМАНИЕ: x > ξ_y * h0. Принимаем x = ξ_y * h0 (разрушение по сжатому бетону).");
            xCalc = xiY * h0;
        }

        // ===== 3. ПРЕДЕЛЬНЫЙ ИЗГИБАЮЩИЙ МОМЕНТ (ф. 9.2 или 9.5) =====
        double sigmaPc = PrestressedBeamCalculator.calculateSigmaPc(Rpc, sigmaP2s);
        double M;
        if (isInWeb) {
            M = PrestressedBeamCalculator.calculateMomentWithWeb(
                Rb, b, xCalc, h0, bf, hf, Rsc, As_s, as, sigmaPc, Ap_s, ap);
        } else {
            M = PrestressedBeamCalculator.calculateMomentWithFlange(
                Rb, bf, xCalc, h0, Rsc, As_s, as, sigmaPc, Ap_s, ap);
        }

        // ===== 4. РАСЧЕТ ПО ИЗГИБУ (ф. 7.20) =====
        double Mp = (1.1 * pp + 1.2 * pb) * Omega;
        double k_M = (M - Mp) / (1.15 * epsilonM * Omega);

        // ===== 5. РАСЧЕТ ПО ПОПЕРЕЧНОЙ СИЛЕ (ф. 9.7 и 7.25) =====
        double Q = PrestressedBeamCalculator.calculateShearForce(
            Rp, sumApi, sinAlpha, Rs, Asw, c, s, Qb);
        double k_Q = (Q - Qp) / (1.15 * epsilonQ * OmegaK);

        // ===== 6. ГРУЗОПОДЪЕМНОСТЬ ПО ПРОЧНОСТИ =====
        double kStrength = Math.min(k_M, k_Q);
        double kc = PrestressedBeamCalculator.getStandardLoad(ctx.spanLength, 0.5);
        double mu = LoadsCalculator.calculateDynamicCoeff(ctx, ctx.spanLength);
        double K_M = k_M / (kc * mu);
        double K_Q = k_Q / (kc * mu);
        double KStrength = Math.min(K_M, K_Q);

        // ====================================================================
        // 7. РАСЧЕТ ПО ВЫНОСЛИВОСТИ (Раздел 9.3, формулы 7.40 и 7.41)
        // ====================================================================

        // Высота сжатой зоны с учетом растянутой зоны бетона (ф. 9.14)
        double xPrime = h - hc;

        // Определение коэффициентов ε_ρ по таблицам 5.1 и 5.3
        double epsRhoB;
        if (rhoB <= 0.1) epsRhoB = 1.00;
        else if (rhoB <= 0.2) epsRhoB = 1.06;
        else if (rhoB <= 0.3) epsRhoB = 1.10;
        else if (rhoB <= 0.4) epsRhoB = 1.15;
        else if (rhoB <= 0.5) epsRhoB = 1.20;
        else epsRhoB = 1.24; // 0.6 и более

        double epsRhoP;
        if (rhoP <= 0.75) epsRhoP = 0.78;
        else if (rhoP <= 0.8) epsRhoP = 0.82;
        else if (rhoP <= 0.85) epsRhoP = 0.87;
        else if (rhoP <= 0.9) epsRhoP = 0.91;
        else if (rhoP <= 1.0) epsRhoP = 0.91 + (rhoP - 0.9) / 0.1 * 0.09; // интерполяция
        else epsRhoP = 1.0;

        // Расчетные сопротивления на выносливость
        double Rbf = 0.6 * epsRhoB * Rb; // ф. 5.1
        double Rpf = epsRhoP * Rp;       // ф. 9.8

        // Момент от постоянных нагрузок для выносливости (коэф. надежности n_p = 1.0)
        double MpFatigue = (1.0 * pp + 1.0 * pb) * Omega;

        // Допускаемая нагрузка по выносливости бетона (адаптация ф. 7.40)
        double kFatB = (1.0 / (ThetaFatigue * epsilonM * Omega)) *
            ((Rbf * 1000 * Ired) / xPrime - MpFatigue);

        // Допускаемая нагрузка по выносливости арматуры (адаптация ф. 7.41)
        double leverArmP = h - au - xPrime; // Расстояние от центра тяжести сжатой зоны до арматуры
        double kFatP = (1.0 / (ThetaFatigue * epsilonM * Omega)) *
            ((Rpf * 1000 * Ired) / (nPrime * leverArmP) - MpFatigue);

        // Итоговые показатели по выносливости
        double kFatigue = Math.min(kFatB, kFatP);
        double KFatigue = kFatigue / (kc * mu0Fatigue);

        // ====================================================================
        // ИТОГОВЫЙ РАСЧЕТ И ВЫВОД ОТЧЕТА
        // ====================================================================
        double kFinal = Math.min(kStrength, kFatigue);
        double KFinal = Math.min(KStrength, KFatigue);

        System.out.println("\n============================================================");
        System.out.println(" РЕЗУЛЬТАТЫ РАСЧЕТА (РАЗДЕЛ 9)");
        System.out.println("============================================================");
        System.out.println("\n[А. Расчет по прочности]");
        System.out.printf("Граничная относительная высота ξ_y: %.3f%n", xiY);
        System.out.printf("Высота сжатой зоны x: %.4f м (Граница в %s)%n", xCalc, isInWeb ? "ребре" : "плите");
        System.out.printf("Предельный момент M: %.2f кН·м%n", M);
        System.out.printf("Допускаемая нагрузка по изгибу k_M: %.2f кН/м (Класс K_M = %.2f)%n", k_M, K_M);
        System.out.printf("Предельная поперечная сила Q: %.2f кН%n", Q);
        System.out.printf("Допускаемая нагрузка по срезу k_Q: %.2f кН/м (Класс K_Q = %.2f)%n", k_Q, K_Q);
        System.out.printf(">>> k по прочности: %.2f кН/м (Класс K = %.2f) <<<%n", kStrength, KStrength);

        System.out.println("\n------------------------------------------------------------");
        System.out.println("[Б. Расчет по выносливости (Раздел 9.3)]");
        System.out.printf("Высота сжатой зоны с учетом растянутой зоны x': %.4f м%n", xPrime);
        System.out.printf("Коэффициент ε_ρb (бетон): %.2f | ε_ρp (арматура): %.3f%n", epsRhoB, epsRhoP);
        System.out.printf("R_bf (бетон на выносливость): %.2f МПа%n", Rbf);
        System.out.printf("R_pf (арматура на выносливость): %.2f МПа%n", Rpf);
        System.out.printf("Mp (момент от пост. нагрузок для выносливости): %.2f кН·м%n", MpFatigue);
        System.out.printf("Допускаемая нагрузка по выносливости бетона k_fat_b: %.2f кН/м%n", kFatB);
        System.out.printf("Допускаемая нагрузка по выносливости арматуры k_fat_p: %.2f кН/м%n", kFatP);
        System.out.printf(">>> k по выносливости: %.2f кН/м (Класс K_fat = %.2f) <<<%n", kFatigue, KFatigue);

        System.out.println("\n============================================================");
        System.out.printf(" >>> ИТОГОВАЯ ДОПУСКАЕМАЯ НАГРУЗКА k = %.2f кН/м <<<%n", kFinal);
        System.out.printf(" >>> ИТОГОВЫЙ КЛАСС ГЛАВНОЙ БАЛКИ: K = %.2f <<<%n", KFinal);
        System.out.println("============================================================\n");
    }

    private static void calculateBoard(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЁТ ПРОДОЛЬНОГО БОРТА (Раздел 10) ---");
        System.out.println("(материалы берутся из Раздела 5 — сначала выполните пункт 1)");

        BoardInput in = new BoardInput();

        // --- материалы и коэффициенты из BridgeContext ---
        in.Rb = ctx.Rb;
        in.Rbt = ctx.Rbt;
        in.Rs = ctx.Rs;
        in.Rsc = ctx.Rs;                       // для ненапрягаемой арматуры R_sc = R_s
        in.nPrime = ctx.nPrime;
        in.np = ctx.np;
        in.npPrime = ctx.npPrime;
        in.nk = ctx.nk;
        in.gammaBallast = ctx.gammaBallastWithTrack;
        in.ls = ctx.ls;                        // длина шпалы (если введена в Разделе 6)

        // --- геометрия борта ---
        System.out.print("Высота продольного борта H_br (м) [например, 0.5]: ");
        in.Hbr = sc.nextDouble();
        if (in.ls <= 0) {
            System.out.print("Длина шпалы l_s (м) [например, 2.7]: ");
            in.ls = sc.nextDouble();
        }
        System.out.print("Толщина балласта под шпалой h_b (м) [например, 0.25]: ");
        in.hb = sc.nextDouble();
        System.out.print("Плечо балластной призмы x (м) [например, 0.4]: ");
        in.xShoulder = sc.nextDouble();

        // --- сечение и арматура ---
        System.out.print("Рабочая высота сечения h0 (м) [например, 0.45]: ");
        in.h0 = sc.nextDouble();
        System.out.print("Площадь растянутой арматуры As (м²) [например, 0.0012]: ");
        in.As = sc.nextDouble();
        System.out.print("Площадь сжатой арматуры As' (м²) [например, 0.0004]: ");
        in.AsPrime = sc.nextDouble();
        System.out.print("Расстояние до центра сжатой арматуры a's (м) [например, 0.04]: ");
        in.asPrime = sc.nextDouble();
        System.out.print("Длина проекции наклонного сечения c (м) [0 — принять = h0; например, 0]: ");
        in.cShear = sc.nextDouble();
        System.out.print("Угол внутреннего трения балласта φ (град) [например, 40]: ");
        in.phiFrictionDeg = sc.nextDouble();

        // --- выносливость (Раздел 10.3) ---
        System.out.print("Асимметрия цикла для бетона ρ_b [например, 0.3]: ");
        in.rhoB = sc.nextDouble();
        System.out.print("Асимметрия цикла для арматуры ρ [например, 0.3]: ");
        in.rhoS = sc.nextDouble();
        System.out.print("Коэффициент уменьшения динамики ε (Приложение 3) [например, 0.8]: ");
        in.epsilonDyn = sc.nextDouble();

        // --- приведение к классу (необязательно) ---
        System.out.print("Эталонная нагрузка k_c (0 — класс не считать) [например, 0]: ");
        in.kc = sc.nextDouble();
        if (in.kc > 0) {
            System.out.print("Динамический коэффициент (1+μ) [например, 1.3]: ");
            in.dynamicCoeff = sc.nextDouble();
        }

        LongitudinalBoardCalculator.calculateAndReport(in);
    }

    private static void calculateCurvedSpan(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ПРОЛЁТ НА КРИВОЙ (Раздел 11) ---");
        System.out.println("(пролёт, c, Es/Eb, As, балласт, шпала берутся из ранее введённых данных)");

        CurvedSpanInput in = new CurvedSpanInput();

        // --- из BridgeContext ---
        in.l  = ctx.spanLength;
        in.c  = ctx.distanceBetweenBeams;
        in.Eb = ctx.Eb;
        in.Es = ctx.Es;
        in.As = ctx.As_tensile;
        in.hb = ctx.ballastThickness;
        in.ls = ctx.ls;
        in.as = ctx.as_tensile;
        if (in.l  <= 0) { System.out.print("Расчётный пролёт l (м) [например, 16.5]: "); in.l  = sc.nextDouble(); }
        if (in.c  <= 0) { System.out.print("Расстояние между осями балок c (м) [например, 1.9]: "); in.c = sc.nextDouble(); }
        if (in.Eb <= 0) { System.out.print("Модуль упругости бетона Eb (МПа) [например, 30000]: "); in.Eb = sc.nextDouble(); }
        if (in.As <= 0) { System.out.print("Площадь растянутой арматуры As (м²) [например, 0.006]: "); in.As = sc.nextDouble(); }

        // --- геометрия сечения ---
        System.out.print("Ширина ребра (стенки) балки b (м) [например, 0.2]: ");        in.b  = sc.nextDouble();
        System.out.print("Параметр l_k для c1 = l_k + 0,5b (м) [например, 0.5]: ");      in.lk = sc.nextDouble();
        System.out.print("Полная высота балки h (м) [например, 1.3]: ");                 in.h  = sc.nextDouble();
        System.out.print("Средняя толщина плиты между рёбрами h1 (м) [например, 0.18]: ");in.h1 = sc.nextDouble();
        System.out.print("Средняя толщина плиты консоли h2 (м) [например, 0.15]: ");      in.h2 = sc.nextDouble();
        System.out.print("Высота рельса hp (м) [например, 0.18]: ");                      in.hp = sc.nextDouble();
        System.out.print("Высота шпалы hs (м) [например, 0.2]: ");                       in.hs = sc.nextDouble();
        System.out.print("Высота приложения нагрузки ht (м) [например, 2.2]: "); in.ht = sc.nextDouble();

        // --- кривая и путь ---
        System.out.print("Радиус кривой R (м) [например, 600]: ");                       in.R  = sc.nextDouble();
        System.out.print("Наибольшая скорость v (км/ч) [например, 80]: ");              in.v  = sc.nextDouble();
        System.out.print("Возвышение наружного рельса Δh (м) [например, 0.1]: ");        in.cantElevation = sc.nextDouble();
        System.out.print("Расстояние между осями головок рельсов b0 (м) [например, 1.6]: "); in.b0 = sc.nextDouble();
        System.out.print("Смещение оси пути l' (участок 0,25l…0,75l, м) [например, 0.05]: ");  in.lPrime = sc.nextDouble();
        System.out.print("Смещение оси пути l'' (участки у опор, м) [например, 0.05]: ");      in.lDoublePrime = sc.nextDouble();
        System.out.print("Коэффициент Θ (приложение) [например, 1.0]: ");                in.Theta = sc.nextDouble();
        System.out.print("Динамический коэффициент μ₀ [например, 0.3]: ");               in.mu0 = sc.nextDouble();

        CurvedSpanCalculator.calculateAndReport(in);
    }

    private static void calculateDefects(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- УЧЁТ ВЛИЯНИЯ ДЕФЕКТОВ (Раздел 12) ---");
        System.out.println("(материалы можно взять из Раздела 5 — сначала выполните пункт 1)");
        System.out.println("Выберите вид дефекта:");
        System.out.println("  1 - ослабление арматуры коррозией / выключенные стержни (12.2, ф. 12.1)");
        System.out.println("  2 - трещина в сжатой зоне бетона (12.3, ф. 12.2-12.3)");
        System.out.println("  3 - раковины/сколы бетона, прямоугольное сечение (12.4, ф. 12.4-12.5)");
        System.out.println("  4 - раковины/сколы бетона, тавровое сечение (12.4, ф. 12.6, 12.5)");
        System.out.println("  5 - дефекты в пролёте с напрягаемой арматурой (12.5, ф. 12.7, 12.5)");
        System.out.print("Ваш выбор [например, 1]: ");
        int kind = sc.nextInt();

        DefectInput in = new DefectInput();

        // материалы из BridgeContext (если рассчитаны в п.1); при нуле — спросим
        in.Rb  = ctx.Rb;
        in.Rs  = ctx.Rs;
        in.Rsc = ctx.Rs;                       // для ненапрягаемой арматуры R_sc = R_s

        if (kind == 1) {
            in.type = DefectInput.DefectType.CORROSION;
            System.out.print("Общее число стержней рабочей арматуры n [например, 20]: ");
            in.nBars = sc.nextInt();
            System.out.print("Площадь сечения одного стержня f_a (см²/мм²/м² — любые ед.) [например, 4.91]: ");
            in.faOne = sc.nextDouble();
            System.out.print("Число повреждённых коррозией стержней n_1 [например, 2]: ");
            int n1 = sc.nextInt();
            in.corrodedLoss = new double[Math.max(n1, 0)];
            for (int i = 0; i < n1; i++) {
                System.out.printf("  Площадь ослабления коррозией f_%d (в тех же ед.) [например, 0.6]: ", i + 1);
                in.corrodedLoss[i] = sc.nextDouble();
            }
            System.out.print("Число выключенных из работы стержней n_2 [например, 1]: ");
            in.nDisconnected = sc.nextInt();
            DefectCalculator.calculateAndReport(in);
            return;
        }

        if (kind == 2) {
            in.type = DefectInput.DefectType.CRACK_IN_COMPRESSION;
            readMaterialsIfEmpty(sc, in, false);
            System.out.print("Ширина сечения b (м) [например, 0.2]: ");                    in.b  = sc.nextDouble();
            System.out.print("Рабочая высота сечения h0 (м) [например, 1.25]: ");           in.h0 = sc.nextDouble();
            System.out.print("Площадь растянутой арматуры A_s (м²) [например, 0.006]: ");     in.As = sc.nextDouble();
            System.out.print("Площадь сжатой арматуры A'_s (м²) [например, 0.0006]: ");        in.AsPrime = sc.nextDouble();
            System.out.print("Расстояние до центра сжатой арматуры a'_s (м) [например, 0.05]: "); in.asPrime = sc.nextDouble();
            System.out.print("Высота сжатой зоны по эпюре трещины x̄_φ (м) [например, 0.05]: ");   in.xBarPhi = sc.nextDouble();
            System.out.print("Момент от испытательной нагрузки M̄ (кН·м) [например, 900]: ");     in.Mbar = sc.nextDouble();
            System.out.print("Предельный момент M по разделу 7 (кН·м) [0 — рассчитать; например, 1834]: "); in.Mult = sc.nextDouble();
            DefectCalculator.calculateAndReport(in);
            return;
        }

        // kind 3/4/5 — раковины/сколы
        System.out.print("Ширина сечения (ребра) b (м) [например, 0.2]: ");                in.b  = sc.nextDouble();
        System.out.print("Рабочая высота сечения h0 (м) [например, 1.25]: ");               in.h0 = sc.nextDouble();
        readMaterialsIfEmpty(sc, in, kind == 5);
        System.out.print("Площадь растянутой арматуры A_s (м²) [например, 0.006]: ");         in.As = sc.nextDouble();
        System.out.print("Площадь сжатой арматуры A'_s (м²) [например, 0.0006]: ");            in.AsPrime = sc.nextDouble();
        System.out.print("Расстояние до центра сжатой арматуры a'_s (м) [например, 0.05]: ");in.asPrime = sc.nextDouble();
        System.out.print("Площадь ослабления раковиной/сколом A_0 (м²) [например, 0.003]: "); in.A0 = sc.nextDouble();
        System.out.print("Расстояние от растянутой арматуры до ц.т. ослабления a_0 (м) [например, 1.15]: "); in.a0 = sc.nextDouble();

        if (kind == 3) {
            in.type = DefectInput.DefectType.VOID_RECTANGULAR;
        } else if (kind == 4) {
            in.type = DefectInput.DefectType.VOID_TSECTION;
            System.out.print("Расчётная ширина полки b'_f (м) [например, 1.6]: ");          in.bfPrime = sc.nextDouble();
            System.out.print("Приведённая толщина полки h'_f (м) [например, 0.18]: ");       in.hfPrime = sc.nextDouble();
        } else if (kind == 5) {
            in.type = DefectInput.DefectType.VOID_PRESTRESSED;
            System.out.print("Расчётная ширина полки b'_f (м) [например, 1.6]: ");          in.bfPrime = sc.nextDouble();
            System.out.print("Приведённая толщина полки h'_f (м) [например, 0.18]: ");       in.hfPrime = sc.nextDouble();
            System.out.print("Сопротивление напрягаемой арматуры R_p (МПа) [например, 800]: ");     in.Rp = sc.nextDouble();
            System.out.print("Напряжение в напрягаемой арматуре сжатой зоны σ'_pc (МПа) [например, 300]: "); in.sigmaPc = sc.nextDouble();
            System.out.print("Площадь растянутой напрягаемой арматуры A_p (м²) [например, 0.003]: ");  in.Ap = sc.nextDouble();
            System.out.print("Площадь напрягаемой арматуры сжатой зоны A'_p (м²) [например, 0.0005]: ");in.ApPrime = sc.nextDouble();
            System.out.print("Расстояние до центра A'_p — a'_p (м) [например, 0.05]: ");              in.apPrime = sc.nextDouble();
        } else {
            System.out.println("Неверный выбор вида дефекта.");
            return;
        }
        DefectCalculator.calculateAndReport(in);
    }

    /** Запрашивает R_b/R_s, если они не заданы в контексте. */
    private static void readMaterialsIfEmpty(Scanner sc, DefectInput in, boolean prestressed) {
        if (in.Rb <= 0) { System.out.print("Сопротивление бетона сжатию R_b (МПа) [например, 17.0]: "); in.Rb = sc.nextDouble(); }
        if (in.Rs <= 0) { System.out.print("Сопротивление арматуры растяжению R_s (МПа) [например, 250]: "); in.Rs = sc.nextDouble(); }
        if (in.Rsc <= 0) { in.Rsc = in.Rs; }
    }

    private static void calculateCarbon(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- УСИЛЕНИЕ УГЛЕВОЛОКНОМ (Раздел 13) ---");
        System.out.println("(характеристики бетона и арматуры — по разделу 5, сначала выполните пункт 1)");
        System.out.println("Выберите проверку:");
        System.out.println("  1 - прочность нормального сечения по моменту (13.1-13.9)");
        System.out.println("  2 - прочность наклонного сечения по поперечной силе (13.10-13.16)");
        System.out.println("  3 - прочность наклонного сечения по моменту (13.17-13.18)");
        System.out.println("  4 - выносливость усиленного сечения (13.19-13.28)");
        System.out.println("  5 - учёт технологии усиления (13.29)");
        System.out.print("Ваш выбор [например, 1]: ");
        int mode = sc.nextInt();

        CarbonInput in = new CarbonInput();
        in.Rb = ctx.Rb; in.Rbt = ctx.Rbt; in.Rs = ctx.Rs; in.Rsc = ctx.Rs;
        in.Eb = ctx.Eb; in.Es = ctx.Es; in.nPrimeSteel = ctx.nPrime;

        if (mode == 5) {
            in.mode = CarbonInput.Mode.TECHNOLOGY;
            System.out.print("Предельный момент неусиленного сечения M (кН·м) [например, 1834]: ");   in.M  = sc.nextDouble();
            System.out.print("Предельный момент усиленного сечения M^y (кН·м) [например, 2226]: ");    in.My = sc.nextDouble();
            System.out.print("Момент от постоянных нагрузок M_p (кН·м) [например, 650]: ");           in.Mp = sc.nextDouble();
            System.out.print("Момент от временной нагрузки при усилении M_k (кН·м) [например, 400]: ");in.Mk = sc.nextDouble();
            System.out.print("M^y_k (кН·м) [например, 200]: ");                                       in.MyK = sc.nextDouble();
            CarbonCalculator.calculateAndReport(in);
            return;
        }

        if (in.Rb <= 0) { System.out.print("R_b (МПа) [например, 17.0]: "); in.Rb = sc.nextDouble(); }
        if (in.Rs <= 0) { System.out.print("R_s (МПа) [например, 250]: "); in.Rs = sc.nextDouble(); }
        in.Rsc = in.Rs;

        System.out.print("Модуль упругости углеволокна E_f (МПа) [например, 230000]: ");        in.Ef = sc.nextDouble();
        System.out.print("Сопротивление углеволокна R_f (МПа) [например, 2400]: ");           in.Rf = sc.nextDouble();
        System.out.print("Толщина одного слоя t_f (мм) [например, 0.13]: ");                  in.tfLayerMm = sc.nextDouble();
        System.out.print("Количество слоёв n [например, 2]: ");                            in.nLayers = sc.nextInt();
        System.out.println("Тип усиления (табл. 13.4): 1-холст низ без закр., 2-холст низ с закр.,");
        System.out.println("  3-U-обойма без закр., 4-U-обойма с закр., 5-пластины с закр.");
        System.out.print("Ваш выбор [например, 3]: ");
        in.reinfType = reinforcementType(sc.nextInt());

        if (mode == 1) {
            in.mode = CarbonInput.Mode.STRENGTH_MOMENT;
            System.out.print("Ширина ребра b (м) [например, 0.2]: ");                    in.b = sc.nextDouble();
            System.out.print("Ширина полки b'_f (м) [например, 1.6]: ");                 in.bfPrime = sc.nextDouble();
            System.out.print("Толщина полки h'_f (м) [например, 0.18]: ");                in.hfPrime = sc.nextDouble();
            System.out.print("Высота балки h (м) [например, 1.3]: ");                    in.h = sc.nextDouble();
            System.out.print("Рабочая высота h0 (м) [например, 1.25]: ");                 in.h0 = sc.nextDouble();
            System.out.print("Расстояние до сжатой арматуры a'_s (м) [например, 0.05]: ");in.asPrime = sc.nextDouble();
            System.out.print("Площадь растянутой арматуры A_s (м²) [например, 0.006]: ");  in.As = sc.nextDouble();
            System.out.print("Площадь сжатой арматуры A'_s (м²) [например, 0.0006]: ");     in.AsPrime = sc.nextDouble();
            System.out.print("Площадь усиления на нижней грани A_f1 (м²) [например, 0.00013]: ");   in.Af1 = sc.nextDouble();
            System.out.print("Площадь усиления на боковых гранях A_f2 (м², 0 — только низ) [например, 0.00006]: "); in.Af2 = sc.nextDouble();
            System.out.print("Высота полок U-обоймы d (м) [например, 0.3]: ");           in.d = sc.nextDouble();
            System.out.print("Предельная деформация бетона ε_b,ult [например, 0.0033]: "); in.epsBult = sc.nextDouble();
            readMomentLoads(sc, in);
        } else if (mode == 2) {
            in.mode = CarbonInput.Mode.STRENGTH_SHEAR;
            if (in.Rbt <= 0) { System.out.print("R_bt (МПа) [например, 1.2]: "); in.Rbt = sc.nextDouble(); }
            if (in.Eb <= 0)  { System.out.print("E_b (МПа) [например, 30000]: "); in.Eb = sc.nextDouble(); }
            System.out.print("Ширина ребра b (м) [например, 0.2]: ");                    in.b = sc.nextDouble();
            System.out.print("Рабочая высота h0 (м) [например, 1.25]: ");                 in.h0 = sc.nextDouble();
            System.out.print("Площадь одной ветви хомутов A_sw (м²) [например, 0.0001]: "); in.Asw = sc.nextDouble();
            System.out.print("Шаг хомутов s (м) [например, 0.15]: ");                     in.s = sc.nextDouble();
            System.out.print("Σ A_si·sinα по отгибам (м², 0 — нет) [например, 0]: ");  in.sumAsiSinAlpha = sc.nextDouble();
            System.out.print("Длина проекции c (м) [0 — вычислить по 13.14; например, 0]: "); in.cShear = sc.nextDouble();
            System.out.print("Σ площадей наклонных холстов A_fi (м²) [например, 0.00006]: ");in.Afi = sc.nextDouble();
            System.out.print("Угол наклона холстов φ (град) [например, 90]: "); in.phiFiberDeg = sc.nextDouble();
            System.out.print("Σ площадей вертикальных холстов A_fw (м²) [например, 0.00006]: "); in.Afw = sc.nextDouble();
            System.out.print("Предельная поперечная сила Q (кН) [0 — вычислить; например, 0]: "); in.Q = sc.nextDouble();
            System.out.print("Q_p (кН) [0 — вычислить по 13.11; например, 0]: ");      in.Qp = sc.nextDouble();
            if (in.Qp <= 0) {
                System.out.print("  p_p (кН/м) [например, 15]: "); in.pp = sc.nextDouble();
                System.out.print("  p_b (кН/м) [например, 20]: "); in.pb = sc.nextDouble();
                System.out.print("  Ω_p (м) [например, 8.0]: ");    in.OmegaP = sc.nextDouble();
            }
            System.out.print("Доля нагрузки ε_Q [например, 0.55]: ");                     in.epsQ = sc.nextDouble();
            System.out.print("Площадь линии влияния Ω_к (м) [например, 8.0]: ");         in.OmegaK = sc.nextDouble();
        } else if (mode == 3) {
            in.mode = CarbonInput.Mode.INCLINED_MOMENT;
            System.out.print("Сопротивление хомутов R_sw (МПа) [например, 250]: ");      in.Rsw = sc.nextDouble();
            System.out.print("Площадь растянутой арматуры A_s (м²) [например, 0.006]: ");  in.As = sc.nextDouble();
            System.out.print("плечо z_s (м) [например, 1.2]: ");                         in.zS = sc.nextDouble();
            System.out.print("A_sw (м²) и z_sw (м) [например, 0.0001 и 0.6]: ");   in.Asw = sc.nextDouble(); in.zSw = sc.nextDouble();
            System.out.print("Σ A_si (м²) и z_si (м) [например, 0 и 0]: "); in.sumAsi = sc.nextDouble(); in.zSi = sc.nextDouble();
            System.out.print("A_f1 (м²) и z_c1 (м) [например, 0.00013 и 1.25]: ");   in.Af1 = sc.nextDouble(); in.zC1 = sc.nextDouble();
            System.out.print("A_f2 (м²) и z_c2 (м) [например, 0.00006 и 0.6]: ");   in.Af2 = sc.nextDouble(); in.zC2 = sc.nextDouble();
            System.out.print("A_fw (м²) и z_cw (м) [например, 0.00006 и 0.6]: ");   in.Afw = sc.nextDouble(); in.zCw = sc.nextDouble();
            System.out.print("A_fi (м²) и z_ci (м) [например, 0.00006 и 0.6]: ");   in.Afi = sc.nextDouble(); in.zCi = sc.nextDouble();
            readMomentLoads(sc, in);
        } else if (mode == 4) {
            in.mode = CarbonInput.Mode.FATIGUE;
            System.out.print("R_bf (МПа) [например, 8.5]: "); in.Rbf = sc.nextDouble();
            System.out.print("R_sf (МПа) [например, 120]: "); in.Rsf = sc.nextDouble();
            System.out.print("Ширина ребра b (м) [например, 0.2]: ");                    in.b = sc.nextDouble();
            System.out.print("Ширина полки b'_f (м) [например, 1.6]: ");                 in.bfPrime = sc.nextDouble();
            System.out.print("Толщина полки h'_f (м) [например, 0.18]: ");                in.hfPrime = sc.nextDouble();
            System.out.print("Высота балки h (м) [например, 1.3]: ");                    in.h = sc.nextDouble();
            System.out.print("Рабочая высота h0 (м) [например, 1.25]: ");                 in.h0 = sc.nextDouble();
            System.out.print("a'_s (м) [например, 0.05]: ");                              in.asPrime = sc.nextDouble();
            System.out.print("a_s (м) [например, 0.05]: ");                               in.as = sc.nextDouble();
            System.out.print("a_u (м) [например, 0.05]: ");                               in.au = sc.nextDouble();
            System.out.print("A_s (м²) [например, 0.006]: ");                              in.As = sc.nextDouble();
            System.out.print("A'_s (м²) [например, 0.0006]: ");                             in.AsPrime = sc.nextDouble();
            System.out.print("A_f1 (м²) [например, 0.00013]: ");                             in.Af1 = sc.nextDouble();
            System.out.print("A_f2 (м²) [например, 0.00006]: ");                             in.Af2 = sc.nextDouble();
            System.out.print("A'_f (м²) [например, 0.00013]: ");                             in.AfPrime = sc.nextDouble();
            if (in.nPrimeSteel <= 0) { System.out.print("n' (сталь/бетон) [например, 6.9]: "); in.nPrimeSteel = sc.nextDouble(); }
            System.out.print("n'_f (углеволокно/бетон) [например, 7.7]: ");              in.nPrimeFiber = sc.nextDouble();
            System.out.print("Коэффициент Θ [например, 1.0]: ");                         in.Theta = sc.nextDouble();
            System.out.print("Предельный момент M (кН·м) [например, 1800]: ");            in.M = sc.nextDouble();
            System.out.print("Момент от постоянных M_p (кН·м) [например, 650]: ");       in.Mp = sc.nextDouble();
            System.out.print("Доля нагрузки ε_M [например, 0.55]: ");                     in.epsM = sc.nextDouble();
            System.out.print("Площадь линии влияния Ω (м) [например, 32.0]: ");           in.Omega = sc.nextDouble();
        } else {
            System.out.println("Неверный выбор проверки.");
            return;
        }
        CarbonCalculator.calculateAndReport(in);
    }

    private static void readMomentLoads(Scanner sc, CarbonInput in) {
        System.out.print("Предельный момент неусиленного сечения M (кН·м) [например, 1800]: "); in.M = sc.nextDouble();
        System.out.print("Момент от постоянных нагрузок M_p (кН·м) [например, 650]: ");        in.Mp = sc.nextDouble();
        System.out.print("Доля временной нагрузки ε_M [например, 0.55]: ");                     in.epsM = sc.nextDouble();
        System.out.print("Площадь линии влияния Ω (м) [например, 32.0]: ");                     in.Omega = sc.nextDouble();
    }

    private static CarbonTables.ReinforcementType reinforcementType(int n) {
        switch (n) {
            case 1: return CarbonTables.ReinforcementType.SHEET_BOTTOM_FREE;
            case 2: return CarbonTables.ReinforcementType.SHEET_BOTTOM_ANCHORED;
            case 3: return CarbonTables.ReinforcementType.U_JACKET_FREE;
            case 4: return CarbonTables.ReinforcementType.U_JACKET_ANCHORED;
            case 5: return CarbonTables.ReinforcementType.PLATE_ANCHORED;
            default: return CarbonTables.ReinforcementType.U_JACKET_FREE;
        }
    }

    private static void calculateCarbonRecommendations(Scanner sc, BridgeContext ctx) {
        System.out.println("\n=== РЕКОМЕНДАЦИИ ПО УСИЛЕНИЮ (Раздел 14) ===");

        System.out.println("\n1. Показать все схемы усиления (Таблица 14.1)");
        System.out.println("2. Получить информацию о конкретной схеме");
        System.out.println("3. Подобрать схему по требуемому увеличению несущей способности");
        System.out.println("4. Сравнить несколько схем");
        System.out.print("Выберите действие (1-4): ");

        int action = sc.nextInt();

        switch (action) {
            case 1:
                CarbonRecommendationTables.printAllSchemes();
                break;

            case 2:
                System.out.print("Введите номер схемы (1-7): ");
                int schemeNum = sc.nextInt();
                try {
                    CarbonRecommendationTables.StrengtheningScheme scheme = CarbonRecommendationTables.getScheme(schemeNum);
                    CarbonRecommendationCalculator.printSchemeDetails(scheme);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
                break;

            case 3:
                System.out.print("Введите требуемое увеличение несущей способности (%): ");
                double requiredPercent = sc.nextDouble();
                CarbonRecommendationTables.StrengtheningScheme recommended = CarbonRecommendationCalculator.recommendScheme(requiredPercent);
                CarbonRecommendationCalculator.printSchemeDetails(recommended);
                System.out.printf("\nРекомендованная схема обеспечивает увеличение на %.0f%%%n",
                    recommended.getIncreasePercent());
                break;

            case 4:
                System.out.print("Введите количество схем для сравнения (2-7): ");
                int count = sc.nextInt();
                if (count < 2 || count > 7) {
                    System.out.println("Неверное количество схем");
                    return;
                }

                CarbonRecommendationTables.StrengtheningScheme[] schemesToCompare = new CarbonRecommendationTables.StrengtheningScheme[count];
                System.out.println("Введите номера схем для сравнения:");
                for (int i = 0; i < count; i++) {
                    System.out.printf("  Схема %d: ", i + 1);
                    int num = sc.nextInt();
                    schemesToCompare[i] = CarbonRecommendationTables.getScheme(num);
                }

                CarbonRecommendationCalculator.compareSchemes(schemesToCompare);
                break;

            default:
                System.out.println("Неверный выбор");
        }
    }

    private static void calculateInspection(Scanner sc, BridgeContext ctx) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   ОБСЛЕДОВАНИЕ И ИСПЫТАНИЕ ПРОЛЁТНЫХ СТРОЕНИЙ [Раздел 15]    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        System.out.println("\nВыберите расчёт:");
        System.out.println("  1 - Смещение оси пути (15.3, ф. 15.1)");
        System.out.println("  2 - Средняя прочность бетона (15.4, ф. 15.2)");
        System.out.println("  3 - Доля нагрузки по испытаниям (15.5, ф. 15.3)");
        System.out.println("  4 - Полный расчёт (все три пункта)");
        System.out.print("Ваш выбор: ");
        int choice = sc.nextInt();

        InspectionInput in = new InspectionInput();

        if (choice == 1 || choice == 4) {
            System.out.println("\n--- 15.3. СМЕЩЕНИЕ ОСИ ПУТИ (ф. 15.1) ---");
            System.out.print("a' — расстояние между внутр. гранью головки рельса и отвесом (м) [например: 0,85]: ");
            in.aPrime = sc.nextDouble();
            System.out.print("b' — расстояние от оси пролётного строения до отвеса (м) [например: 0,1]: ");
            in.bPrime = sc.nextDouble();
            System.out.print("b'₀ — ширина колеи по внутр. граням головок рельсов (м) [например: 1.52]: ");
            in.b0Prime = sc.nextDouble();
        }

        if (choice == 2 || choice == 4) {
            System.out.println("\n--- 15.4. ПРОЧНОСТЬ БЕТОНА (ф. 15.2) ---");
            System.out.print("Число измерений n [например: 3]: ");
            int n = sc.nextInt();
            in.concreteStrengths = new double[n];
            for (int i = 0; i < n; i++) {
                System.out.printf("  R%d (МПа) [например: +-23]: ", i + 1);
                in.concreteStrengths[i] = sc.nextDouble();
            }
        }

        if (choice == 3 || choice == 4) {
            System.out.println("\n--- 15.5. ИСПЫТАНИЕ ПРОЛЁТНЫХ СТРОЕНИЙ (ф. 15.3) ---");
            System.out.print("Число балок m [например: 2]: ");
            int m = sc.nextInt();
            in.deflections = new double[m];
            in.inertias = new double[m];
            for (int i = 0; i < m; i++) {
                System.out.printf("\n  Балка %d:%n", i + 1);
                System.out.printf("    Прогиб f%d (мм, с учётом осадки опор) [например: +-12]: ", i + 1);
                in.deflections[i] = sc.nextDouble();
                System.out.printf("    Момент инерции I%d (м⁴, без арматуры) [например: 0,045]: ", i + 1);
                in.inertias[i] = sc.nextDouble();
            }
            System.out.print("\nИндекс балки для расчёта ε_M (1.." + m + "): ");
            in.targetBeamIndex = sc.nextInt() - 1;
        }

        // Расчёт и вывод
        InspectionOutput out = InspectionCalculator.calculateAndReport(in);

        // Сохранение результатов в контекст для использования в других разделах
        if (choice == 1 || choice == 4) {
            // Обновляем смещение оси пути в контексте (используется в разделах 6, 7, 8)
            ctx.trackOffsetLeft = out.trackOffset;
            ctx.trackOffsetRight = out.trackOffset;
            System.out.printf("\n[!] Смещение оси пути l = %.4f м сохранено в контекст.%n", out.trackOffset);
        }
        if (choice == 2 || choice == 4) {
            // Обновляем фактическую прочность бетона (используется в разделе 5 и далее)
            ctx.concreteStrengthR = out.avgConcreteStrength;
            System.out.printf("[!] Средняя прочность бетона R = %.2f МПа сохранена в контекст.%n",
                out.avgConcreteStrength);
            System.out.println("[!] Рекомендуется пересчитать характеристики материалов (пункт 1 меню).");
        }
        if (choice == 3 || choice == 4) {
            // Сохраняем уточнённую долю нагрузки (используется в разделах 7, 8, 9)
            ctx.epsilonM_Beam1 = out.epsilonM;
            System.out.printf("[!] Уточнённая доля нагрузки ε_M = %.4f сохранена в контекст.%n",
                out.epsilonM);
            System.out.println("[!] Рекомендуется пересчитать главную балку (пункт 8 меню).");
        }
    }
}