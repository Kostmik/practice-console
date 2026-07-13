package org.example;

import org.example.calculator.*;
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
        System.out.print("Высота продольного борта H_br (м): ");
        in.Hbr = sc.nextDouble();
        if (in.ls <= 0) {
            System.out.print("Длина шпалы l_s (м): ");
            in.ls = sc.nextDouble();
        }
        System.out.print("Толщина балласта под шпалой h_b (м): ");
        in.hb = sc.nextDouble();
        System.out.print("Плечо балластной призмы x (м): ");
        in.xShoulder = sc.nextDouble();

        // --- сечение и арматура ---
        System.out.print("Рабочая высота сечения h0 (м): ");
        in.h0 = sc.nextDouble();
        System.out.print("Площадь растянутой арматуры As (м²): ");
        in.As = sc.nextDouble();
        System.out.print("Площадь сжатой арматуры As' (м²): ");
        in.AsPrime = sc.nextDouble();
        System.out.print("Расстояние до центра сжатой арматуры a's (м): ");
        in.asPrime = sc.nextDouble();
        System.out.print("Длина проекции наклонного сечения c (м) [0 — принять = h0]: ");
        in.cShear = sc.nextDouble();
        System.out.print("Угол внутреннего трения балласта φ (град) [обычно 40]: ");
        in.phiFrictionDeg = sc.nextDouble();

        // --- выносливость (Раздел 10.3) ---
        System.out.print("Асимметрия цикла для бетона ρ_b: ");
        in.rhoB = sc.nextDouble();
        System.out.print("Асимметрия цикла для арматуры ρ: ");
        in.rhoS = sc.nextDouble();
        System.out.print("Коэффициент уменьшения динамики ε (Приложение 3): ");
        in.epsilonDyn = sc.nextDouble();

        // --- приведение к классу (необязательно) ---
        System.out.print("Эталонная нагрузка k_c (0 — класс не считать): ");
        in.kc = sc.nextDouble();
        if (in.kc > 0) {
            System.out.print("Динамический коэффициент (1+μ): ");
            in.dynamicCoeff = sc.nextDouble();
        }

        LongitudinalBoardCalculator.calculateAndReport(in);
    }

}