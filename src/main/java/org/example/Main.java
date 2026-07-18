package org.example;

import org.example.calculator.*;
import org.example.calculator.paragraph_7.*;
import org.example.calculator.paragraph_8.*;
import org.example.calculator.paragraph_9.*;
import org.example.calculator.paragraph_10.*;
import org.example.calculator.paragraph_11.*;
import org.example.calculator.paragraph_12.*;
import org.example.calculator.paragraph_13.*;
import org.example.calculator.paragraph_14.*;
import org.example.calculator.paragraph_15.*;
import org.example.context.BridgeContext;
import org.example.model.RebarType;
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
            System.out.println("6. Рассчитать плиту балластного корыта по поперечной силе (п. 7.2.4)");
            System.out.println("7. Рассчитать главную балку по моменту (п. 7.2.5-7.2.6)");
            System.out.println("8. Рассчитать главную балку по поперечной силе (п. 7.2.7-7.2.8)");
            System.out.println("9. Рассчитать плиту балластного корыта на выносливость (п. 7.3.1)");
            System.out.println("10. Рассчитать главную балку на выносливость (п. 7.3.2)");
            System.out.println("11. Рассчитать монолитный участок плиты (формула 8.2 - 8.3)");
            System.out.println("12. Рассчитать внешнюю консоль плиты (формула 8.1)");
            System.out.println("13. Рассчитать главную балку (формула 8.4)");
            System.out.println("14. Рассчитать главную балку с напрягаемой арматурой (Раздел 9)");
            System.out.println("15. Рассчитать продольный борт (Раздел 10)");
            System.out.println("16. Рассчитать долю нагрузки на балку на кривой (Раздел 11)");
            System.out.println("17. Учесть влияние дефектов пролётного строения (Раздел 12)");
            System.out.println("18. Рассчитать усиление углеволокном (Раздел 13)");
            System.out.println("19. Получить рекомендации по усилению углеволокном (Раздел 14)");
            System.out.println("20. Обследование и испытание пролётных строений (Раздел 15)");
            System.out.println("0. Выход");

            int choice = readInt(sc, "\nВыберите пункт меню: ");

            if (choice == 0) {
                System.out.println("Завершение работы программы. До свидания!");
                break;
            }

            inputCommonData(sc, ctx);

            switch (choice) {
                case 1: calculateMaterials(sc, ctx); break;
                case 2: calculatePermanentLoads(sc, ctx); break;
                case 3: calculateDynamicCoeff(sc, ctx); break;
                case 4: calculateShare(sc, ctx); break;
                case 5: calculateSlabStrength(sc, ctx); break;
                case 6: calculateSlabShear(sc, ctx); break;
                case 7: calculateBeamMoment(sc, ctx); break;
                case 8: calculateBeamShear(sc, ctx); break;
                case 9: calculateSlabFatigue(sc, ctx); break;
                case 10: calculateBeamFatigue(sc, ctx); break;
                case 11: calculateSlabMonolithic(sc, ctx); break;
                case 12: calculateSlabCantilever(sc, ctx); break;
                case 13: calculateBeam(sc, ctx); break;
                case 14: calculatePrestressedBeam(sc, ctx); break;
                case 15: calculateBoard(sc, ctx); break;
                case 16: calculateCurvedSpan(sc, ctx); break;
                case 17: calculateDefects(sc, ctx); break;
                case 18: calculateCarbon(sc, ctx); break;
                case 19: calculateCarbonRecommendations(sc, ctx); break;
                case 20: calculateInspection(sc, ctx); break;
                default: System.out.println("Неверный выбор.");
            }

            System.out.print("\nНажмите Enter для продолжения...");
            sc.nextLine();
            sc.nextLine();
        }

        sc.close();
    }

    // =====================================================================
    // БЕЗОПАСНЫЙ ВВОД ДАННЫХ (ТОЛЬКО ЗАПЯТАЯ ДЛЯ ДРОБЕЙ)
    // =====================================================================

    private static double readDouble(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.next().trim();
            if (input.contains(".")) {
                System.out.println("   ⚠ Ошибка: используйте запятую (,) в качестве десятичного разделителя, а не точку (.). Попробуйте снова.");
                continue;
            }
            try {
                return Double.parseDouble(input.replace(',', '.'));
            } catch (NumberFormatException e) {
                System.out.println("   ⚠ Ошибка: введено некорректное число. Попробуйте снова.");
            }
        }
    }

    private static int readInt(Scanner sc, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = sc.next().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("   ⚠ Ошибка: введено некорректное целое число. Попробуйте снова.");
            }
        }
    }

    // =====================================================================
    // МЕТОДЫ МЕНЮ
    // =====================================================================

    private static void inputCommonData(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ВВОД ИСХОДНЫХ ДАННЫХ ---");

        ctx.spanLength = readDouble(sc, "Расчётный пролёт l (м) [например, 10,8]: ");
        ctx.ballastThickness = readDouble(sc, "Толщина балласта под шпалой hb (м) [например, 0,25]: ");

        int trackChoice = readInt(sc, "Тип пути: 1 - звеньевой, 2 - бесстыковой. Выберите (1/2): ");
        ctx.trackType = (trackChoice == 2) ? TrackType.CONTINUOUS : TrackType.LINKED;

        int sleeperChoice = readInt(sc, "Тип шпал: 1 - ж/б, 2 - деревянные. Выберите (1/2): ");
        boolean woodenSleepers = (sleeperChoice == 2);
        LoadsCalculator.setBallastDensity(ctx, woodenSleepers);

        ctx.concreteStrengthR = readDouble(sc, "Фактическая прочность бетона R (МПа) [например, 23,0]: ");
        ctx.distanceBetweenBeams = readDouble(sc, "Расстояние между осями главных балок c (м) [например, 1,8]: ");
        ctx.trackOffsetLeft = readDouble(sc, "Смещение оси пути у левой опоры e1 (м) [например, 0,2]: ");
        ctx.trackOffsetRight = readDouble(sc, "Смещение оси пути у правой опоры e2 (м) [например, 0,2]: ");
    }

    private static void calculateMaterials(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ХАРАКТЕРИСТИКИ АРМАТУРЫ ---");
        int rebarChoice = readInt(sc, "Тип рабочей арматуры: 1 - Гладкая (А240), 2 - Периодическая (А400). Выберите (1/2): ");
        System.out.println();
        MaterialCalculator.printMaterialsReport(ctx, rebarChoice);
    }

    private static void calculatePermanentLoads(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ГЕОМЕТРИЯ ДЛЯ СБОРА НАГРУЗОК ---");
        double hSlab = readDouble(sc, "Толщина плиты h_slab (м) [например, 0,26]: ");
        double vConcrete = readDouble(sc, "Объём ж/б пролётного строения V_concrete (м³) [например, 30,6]: ");
        double pDevices = readDouble(sc, "Вес обустройств (лотки, перила) P_devices (кН) [если нет, 0]: ");
        double sBallast = readDouble(sc, "Площадь поперечного сечения балластной призмы S_ballast (м²) [например, 2,06]: ");
        int mBeams = readInt(sc, "Количество главных балок m (обычно 2): ");
        System.out.println();

        LoadsCalculator.printLoadsReport(ctx, hSlab, vConcrete, pDevices, sBallast, mBeams);
    }

    private static void calculateDynamicCoeff(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЁТ ДИНАМИЧЕСКОГО КОЭФФИЦИЕНТА (1+μ) ---");

        double muBeam = LoadsCalculator.calculateDynamicCoeff(ctx, ctx.spanLength);
        ctx.dynamicCoeffBeam = muBeam;
        LoadsCalculator.printDynamicCoeffReport(ctx, ctx.spanLength, "ГЛАВНАЯ БАЛКА", false);

        System.out.println("\nДля ПЛИТЫ БАЛЛАСТНОГО КОРЫТА:");
        System.out.println("  1 - Максимальное значение (λ → 0)");
        System.out.println("  2 - Расчёт по формуле с вводом λ");
        int slabChoice = readInt(sc, "Выберите (1/2): ");

        double muSlab;
        if (slabChoice == 1) {
            muSlab = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0);
            LoadsCalculator.printDynamicCoeffReport(ctx, 0.0, "ПЛИТА БАЛЛАСТНОГО КОРЫТА", true);
        } else {
            double lambdaSlab = readDouble(sc, "Введите длину загружения λ для плиты (м) [например, 2,0]: ");
            if (lambdaSlab <= 0) lambdaSlab = 2.0;
            muSlab = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, false, lambdaSlab);
            LoadsCalculator.printDynamicCoeffReport(ctx, lambdaSlab, "ПЛИТА БАЛЛАСТНОГО КОРЫТА", false);
        }
        ctx.dynamicCoeffSlab = muSlab;
    }

    private static void calculateShare(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЁТ ДОЛЕЙ ВРЕМЕННОЙ НАГРУЗКИ ---");
        int typeChoice = readInt(sc, "Тип пролетного строения: 1 - Монолитное (п. 6.6), 2 - Сборное (п. 6.7). Выберите (1/2): ");
        boolean isMonolithic = (typeChoice == 1);

        double xRatio = readDouble(sc, "Введите относительную координату сечения x/l (от 0 до 1) [например, 0,5]: ");

        if (isMonolithic) {
            ShareCalculator.calculateMonolithic(ctx, xRatio);
        } else {
            ShareCalculator.calculatePrecast(ctx, xRatio);
        }

        ShareCalculator.printReport(ctx, xRatio, isMonolithic);
    }

    private static void calculateSlabStrength(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ГЕОМЕТРИЯ И АРМАТУРА ПЛИТЫ ---");

        ctx.slabHeight = readDouble(sc, "Высота плиты h (м) [например, 0,26]: ");
        ctx.as_tensile = readDouble(sc, "Расстояние до центра растянутой арматуры as (м) [например, 0,026]: ");
        ctx.as_compressive = readDouble(sc, "Расстояние до центра сжатой арматуры as' (м) [например, 0,026]: ");
        ctx.As_tensile = readDouble(sc, "Площадь растянутой арматуры As (м²) [например, 0,000905 для 8Ø12]: ");
        ctx.As_compressive = readDouble(sc, "Площадь сжатой арматуры As' (м²) [если нет, введите 0]: ");

        System.out.println("\n--- ГЕОМЕТРИЯ ПОПЕРЕЧНОГО СЕЧЕНИЯ ---");
        ctx.lp = readDouble(sc, "Расстояние между внутренними гранями ребер lp (м) [например, 1,2]: ");
        ctx.B = readDouble(sc, "Расстояние между наружными гранями ребер B (м) [например, 2,4]: ");
        ctx.ls = readDouble(sc, "Длина шпалы ls (м) [например, 2,7]: ");
        ctx.lb_prime = readDouble(sc, "Расстояние от ребра до левого борта l'b (м) [например, 1,05]: ");
        ctx.lb_doubleprime = readDouble(sc, "Расстояние от ребра до правого борта l''b (м) [например, 1,05]: ");
        ctx.hb_prime = readDouble(sc, "Толщина балласта под левым концом шпалы h'b (м) [например, 0,35]: ");
        ctx.hb_doubleprime = readDouble(sc, "Толщина балласта под правым концом шпалы h''b (м) [например, 0,35]: ");

        System.out.println("\n--- МОМЕНТЫ ОТ ПОСТОЯННЫХ НАГРУЗОК ---");
        ctx.Mp_monolithic = readDouble(sc, "Момент в монолитном участке Mp (кН·м) [введите 0 для авто-расчета]: ");
        ctx.Mp_external_cantilever = readDouble(sc, "Момент во внешней консоли Mp (кН·м) [например, 12,5]: ");

        System.out.println();
        SlabCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateSlabShear(Scanner sc, BridgeContext ctx) {
        if (ctx.Rbt == 0.0 || ctx.ppSlab == null) {
            System.out.println("\n⚠️ ОШИБКА: Недостаточно данных для расчета!");
            System.out.println("Необходимо сначала выполнить пункты 1 и 2.");
            return;
        }

        System.out.println("\n--- ДОПОЛНИТЕЛЬНЫЕ ПАРАМЕТРЫ ДЛЯ РАСЧЕТА ПО ПОПЕРЕЧНОЙ СИЛЕ ---");

        ctx.lt = readDouble(sc, "Длина внешней консоли с учетом тротуара lt (м) [например, 1,25]: ");
        ctx.lk = readDouble(sc, "Длина внешней консоли плиты lk (м) [например, 1,05]: ");
        ctx.P0 = readDouble(sc, "Нагрузка от веса перил P0 (кН/м) [если нет, 0]: ");
        ctx.pt = readDouble(sc, "Нагрузка от веса тротуара pt (кН/м) [если нет, 0]: ");

        System.out.println();
        SlabShearCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateBeamShear(Scanner sc, BridgeContext ctx) {
        if (ctx.Rbt == 0.0 || ctx.ppBeam == null || ctx.epsilonQ_Beam1 == null) {
            System.out.println("\n⚠️ ОШИБКА: Недостаточно данных для расчета!");
            System.out.println("Необходимо сначала выполнить пункты 1, 2 и 4.");
            return;
        }

        System.out.println("\n--- АРМИРОВАНИЕ НА СДВИГ И ЛИНИИ ВЛИЯНИЯ ---");

        ctx.Asw = readDouble(sc, "Площадь поперечного сечения одной ветви хомутов Asw (м²) [например, 0.000201 для 2Ø8]: ");
        ctx.s_stirrups = readDouble(sc, "Шаг хомутов s (м) [например, 0.20]: ");
        ctx.sum_Asi = readDouble(sc, "Сумма площадей отогнутых стержней ΣAsi (м²) [если нет, 0]: ");
        ctx.alpha_bent = readDouble(sc, "Угол наклона отогнутых стержней α (град) [если нет, 0]: ");

        System.out.println("\n--- ПЛОЩАДИ ЛИНИЙ ВЛИЯНИЯ ПОПЕРЕЧНОЙ СИЛЫ ---");
        ctx.Omega_k = readDouble(sc, "Площадь линии влияния поперечной силы Ωk (временная) (м²) [например, 2.97]: ");
        ctx.Omega_p = readDouble(sc, "Площадь линии влияния поперечной силы Ωp (постоянная) (м²) [например, 2.61]: ");

        System.out.println();
        BeamShearCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateSlabFatigue(Scanner sc, BridgeContext ctx) {
        if (ctx.Rb == 0.0 || ctx.ppSlab == null || ctx.k_monolithic == 0.0) {
            System.out.println("\n️ ОШИБКА: Недостаточно данных для расчета!");
            System.out.println("Необходимо сначала выполнить пункты 1, 2 и 5 (расчет плиты на прочность).");
            return;
        }

        System.out.println("\n--- РАСЧЕТ ПЛИТЫ НА ВЫНОСЛИВОСТЬ ---");
        System.out.println("Данные берутся из предыдущих расчетов.");
        System.out.println();

        SlabFatigueCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateBeamFatigue(Scanner sc, BridgeContext ctx) {
        if (ctx.Rb == 0.0 || ctx.ppBeam == null || ctx.k_beam_moment == 0.0) {
            System.out.println("\n⚠️ ОШИБКА: Недостаточно данных для расчета!");
            System.out.println("Необходимо сначала выполнить пункты 1, 2, 4 и 7 (расчет балки на прочность).");
            return;
        }

        System.out.println("\n--- РАСЧЕТ ГЛАВНОЙ БАЛКИ НА ВЫНОСЛИВОСТЬ ---");
        System.out.println("Данные берутся из предыдущих расчетов.");
        System.out.println();

        BeamFatigueCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateBeamMoment(Scanner sc, BridgeContext ctx) {
        if (ctx.Rb == 0.0 || ctx.ppBeam == null || ctx.epsilonM_Beam1 == null) {
            System.out.println("\n⚠️ ОШИБКА: Недостаточно данных для расчета!");
            System.out.println("Необходимо сначала выполнить пункты 1, 2 и 4.");
            return;
        }

        System.out.println("\n--- ГЕОМЕТРИЯ И АРМАТУРА ГЛАВНОЙ БАЛКИ ---");

        ctx.beamHeight = readDouble(sc, "Высота балки h (м) [например, 1,34]: ");
        ctx.beamWidth = readDouble(sc, "Ширина ребра балки b (м) [например, 0,60]: ");
        ctx.bf = readDouble(sc, "Расчетная ширина плиты bf (м) [например, 2,45]: ");
        ctx.hf = readDouble(sc, "Приведенная толщина плиты hf (м) [например, 0,238]: ");
        ctx.As_beam_tensile = readDouble(sc, "Площадь растянутой арматуры As (м²) [например, 0,01367 для 17Ø32]: ");
        ctx.As_beam_compressive = readDouble(sc, "Площадь сжатой арматуры As' (м²) [например, 0,003216 для 4Ø32]: ");
        ctx.as_beam_tensile = readDouble(sc, "Расстояние до центра растянутой арматуры as (м) [например, 0,106]: ");
        ctx.as_beam_compressive = readDouble(sc, "Расстояние до центра сжатой арматуры as' (м) [например, 0,038]: ");

        System.out.println();
        BeamMomentCalculator.calculateAndPrintReport(ctx);
    }

    private static void calculateSlabMonolithic(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ МОНОЛИТНОГО УЧАСТКА ПЛИТЫ (ФОРМУЛА 8.2) ---");
        int designYear = readInt(sc, "Год выпуска норм проектирования (например, 1931): ");
        int rebarChoice = readInt(sc, "Тип арматуры: 1 - Гладкая (А240), 2 - Периодического профиля (А400). Выберите (1/2): ");
        RebarType rebarType = (rebarChoice == 1) ? RebarType.SMOOTH : RebarType.RIBBED;

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        double alpha = readDouble(sc, "Положение вершины линии влияния α (0,0 или 0,5): ");
        double j = readDouble(sc, "Относительное изменение площади арматуры j (1,0 - без дефектов): ");
        double l0 = readDouble(sc, "Длина распределения временной нагрузки l0 (м) [например, 3,4]: ");
        double etaM = readDouble(sc, "Коэффициент надежности по назначению ηM (например, 1,1): ");

        if (ctx.slabHeight <= 0) {
            ctx.slabHeight = readDouble(sc, "Толщина плиты h_slab (м) [например, 0,26]: ");
        }
        System.out.println();

        double k = SlabMonolithicCalculator.calculate(ctx, designYear, l0, etaM, rebarType, j, loadType, alpha);
        SlabMonolithicCalculator.printReport(ctx, designYear, l0, etaM, rebarType, j, loadType, alpha, k);
    }

    private static void calculateSlabCantilever(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ ВНЕШНЕЙ КОНСОЛИ ПЛИТЫ (ФОРМУЛА 8.1) ---");
        int designYear = readInt(sc, "Год выпуска норм проектирования (например, 1931): ");
        int rebarChoice = readInt(sc, "Тип арматуры: 1 - Гладкая (А240), 2 - Периодического профиля (А400). Выберите (1/2): ");
        RebarType rebarType = (rebarChoice == 1) ? RebarType.SMOOTH : RebarType.RIBBED;

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        double alpha = readDouble(sc, "Положение вершины линии влияния α (0,0 или 0,5): ");
        double j = readDouble(sc, "Относительное изменение площади арматуры j (1,0 - без дефектов): ");
        double l0 = readDouble(sc, "Длина распределения временной нагрузки l0 (м) [например, 3,4]: ");
        double etaM = readDouble(sc, "Коэффициент надежности по назначению ηM (например, 1,1): ");
        double delta = readDouble(sc, "Расстояние от ребра до точки приложения нагрузки Δ (м): ");
        double Z = readDouble(sc, "Расстояние от ребра до расчетного сечения Z (м): ");
        double lk = readDouble(sc, "Длина внешней консоли lk (м): ");
        double P0 = readDouble(sc, "Нагрузка от веса перил P0 (кН): ");
        double lt = readDouble(sc, "Расстояние до центра тяжести перил lt (м): ");
        double Mp = readDouble(sc, "Изгибающий момент от постоянных нагрузок Mp (кН·м): ");

        if (ctx.slabHeight <= 0) ctx.slabHeight = readDouble(sc, "Толщина плиты h_slab (м) [например, 0,26]: ");
        if (ctx.B <= 0) ctx.B = readDouble(sc, "Расстояние между наружными гранями ребер B (м) [например, 2,4]: ");
        if (ctx.ls <= 0) ctx.ls = readDouble(sc, "Длина шпалы ls (м) [например, 2,7]: ");
        System.out.println();

        double k = SlabCantileverCalculator.calculate(ctx, designYear, l0, etaM, rebarType, j, loadType, alpha, delta, Z, lk, P0, lt, Mp);
        SlabCantileverCalculator.printReport(ctx, designYear, l0, etaM, rebarType, j, loadType, alpha, delta, Z, lk, P0, lt, Mp, k);
    }

    private static void calculateBeam(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- РАСЧЕТ ГЛАВНОЙ БАЛКИ (ФОРМУЛА 8.4) ---");
        int designYear = readInt(sc, "Год выпуска норм проектирования (например, 1931): ");
        int rebarChoice = readInt(sc, "Тип арматуры: 1 - Гладкая (А240), 2 - Периодического профиля (А400). Выберите (1/2): ");
        RebarType rebarType = (rebarChoice == 1) ? RebarType.SMOOTH : RebarType.RIBBED;

        System.out.print("Тип нагрузки (Н7 или Н8): ");
        String loadType = sc.next();

        double alpha = readDouble(sc, "Положение вершины линии влияния α (0,0 или 0,5): ");
        double j = readDouble(sc, "Относительное изменение площади арматуры j (1,0 - без дефектов): ");
        double epsilon = readDouble(sc, "Доля временной нагрузки на балку εM (из Раздела 6) [например, 0,565]: ");
        int m = readInt(sc, "Количество главных балок m [например, 2]: ");
        double pp = readDouble(sc, "Вес пролетного строения на балку pp (кН/м) [например, 34,0]: ");
        double pb = readDouble(sc, "Вес балласта на балку pb (кН/м) [например, 20,6]: ");
        System.out.println();

        double k = BeamCalculator.calculate(ctx, designYear, rebarType, j, loadType, alpha, epsilon, m, pp, pb);
        BeamCalculator.printReport(ctx, designYear, rebarType, j, loadType, alpha, epsilon, m, pp, pb, k);
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
        BoardInput in = new BoardInput();

        in.Rb = ctx.Rb; in.Rbt = ctx.Rbt; in.Rs = ctx.Rs; in.Rsc = ctx.Rs;
        in.nPrime = ctx.nPrime; in.np = ctx.np; in.npPrime = ctx.npPrime; in.nk = ctx.nk;
        in.gammaBallast = ctx.gammaBallastWithTrack; in.ls = ctx.ls;

        in.Hbr = readDouble(sc, "Высота продольного борта H_br (м) [например, 0,5]: ");
        if (in.ls <= 0) in.ls = readDouble(sc, "Длина шпалы l_s (м) [например, 2,7]: ");
        in.hb = readDouble(sc, "Толщина балласта под шпалой h_b (м) [например, 0,25]: ");
        in.xShoulder = readDouble(sc, "Плечо балластной призмы x (м) [например, 0,4]: ");
        in.h0 = readDouble(sc, "Рабочая высота сечения h0 (м) [например, 0,45]: ");
        in.As = readDouble(sc, "Площадь растянутой арматуры As (м²) [например, 0,0012]: ");
        in.AsPrime = readDouble(sc, "Площадь сжатой арматуры As' (м²) [например, 0,0004]: ");
        in.asPrime = readDouble(sc, "Расстояние до центра сжатой арматуры a's (м) [например, 0,04]: ");
        in.cShear = readDouble(sc, "Длина проекции наклонного сечения c (м) [0 — принять = h0; например, 0]: ");
        in.phiFrictionDeg = readDouble(sc, "Угол внутреннего трения балласта φ (град) [например, 40]: ");
        in.rhoB = readDouble(sc, "Асимметрия цикла для бетона ρ_b [например, 0,3]: ");
        in.rhoS = readDouble(sc, "Асимметрия цикла для арматуры ρ [например, 0,3]: ");
        in.epsilonDyn = readDouble(sc, "Коэффициент уменьшения динамики ε (Приложение 3) [например, 0,8]: ");
        in.kc = readDouble(sc, "Эталонная нагрузка k_c (0 — класс не считать) [например, 0]: ");
        if (in.kc > 0) in.dynamicCoeff = readDouble(sc, "Динамический коэффициент (1+μ) [например, 1,3]: ");

        LongitudinalBoardCalculator.calculateAndReport(in);
    }

    private static void calculateCurvedSpan(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- ПРОЛЁТ НА КРИВОЙ (Раздел 11) ---");
        CurvedSpanInput in = new CurvedSpanInput();

        in.l = ctx.spanLength; in.c = ctx.distanceBetweenBeams; in.Eb = ctx.Eb; in.Es = ctx.Es;
        in.As = ctx.As_tensile; in.hb = ctx.ballastThickness; in.ls = ctx.ls; in.as = ctx.as_tensile;

        if (in.l <= 0) in.l = readDouble(sc, "Расчётный пролёт l (м) [например, 16,5]: ");
        if (in.c <= 0) in.c = readDouble(sc, "Расстояние между осями балок c (м) [например, 1,9]: ");
        if (in.Eb <= 0) in.Eb = readDouble(sc, "Модуль упругости бетона Eb (МПа) [например, 30000]: ");
        if (in.As <= 0) in.As = readDouble(sc, "Площадь растянутой арматуры As (м²) [например, 0,006]: ");

        in.b = readDouble(sc, "Ширина ребра (стенки) балки b (м) [например, 0,2]: ");
        in.lk = readDouble(sc, "Параметр l_k для c1 = l_k + 0,5b (м) [например, 0,5]: ");
        in.h = readDouble(sc, "Полная высота балки h (м) [например, 1,3]: ");
        in.h1 = readDouble(sc, "Средняя толщина плиты между рёбрами h1 (м) [например, 0,18]: ");
        in.h2 = readDouble(sc, "Средняя толщина плиты консоли h2 (м) [например, 0,15]: ");
        in.hp = readDouble(sc, "Высота рельса hp (м) [например, 0,18]: ");
        in.hs = readDouble(sc, "Высота шпалы hs (м) [например, 0,2]: ");
        in.ht = readDouble(sc, "Высота приложения нагрузки ht (м) [например, 2,2]: ");
        in.R = readDouble(sc, "Радиус кривой R (м) [например, 600]: ");
        in.v = readDouble(sc, "Наибольшая скорость v (км/ч) [например, 80]: ");
        in.cantElevation = readDouble(sc, "Возвышение наружного рельса Δh (м) [например, 0,1]: ");
        in.b0 = readDouble(sc, "Расстояние между осями головок рельсов b0 (м) [например, 1,6]: ");
        in.lPrime = readDouble(sc, "Смещение оси пути l' (участок 0,25l…0,75l, м) [например, 0,05]: ");
        in.lDoublePrime = readDouble(sc, "Смещение оси пути l'' (участки у опор, м) [например, 0,05]: ");
        in.Theta = readDouble(sc, "Коэффициент Θ (приложение) [например, 1,0]: ");
        in.mu0 = readDouble(sc, "Динамический коэффициент μ₀ [например, 0,3]: ");

        CurvedSpanCalculator.calculateAndReport(in);
    }

    private static void calculateDefects(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- УЧЁТ ВЛИЯНИЯ ДЕФЕКТОВ (Раздел 12) ---");
        int kind = readInt(sc, "Выберите вид дефекта: 1 - коррозия, 2 - трещина в сжатой зоне, 3 - раковины (прямоуг.), 4 - раковины (тавр), 5 - напрягаемая арматура: ");

        DefectInput in = new DefectInput();
        in.Rb = ctx.Rb; in.Rs = ctx.Rs; in.Rsc = ctx.Rs;

        if (kind == 1) {
            in.type = DefectInput.DefectType.CORROSION;
            in.nBars = readInt(sc, "Общее число стержней рабочей арматуры n [например, 20]: ");
            in.faOne = readDouble(sc, "Площадь сечения одного стержня f_a (см²) [например, 4,91]: ");
            int n1 = readInt(sc, "Число повреждённых коррозией стержней n_1 [например, 2]: ");
            in.corrodedLoss = new double[Math.max(n1, 0)];
            for (int i = 0; i < n1; i++) {
                in.corrodedLoss[i] = readDouble(sc, String.format("Площадь ослабления коррозией f_%d (см²) [например, 0,6]: ", i + 1));
            }
            in.nDisconnected = readInt(sc, "Число выключенных из работы стержней n_2 [например, 1]: ");
            DefectCalculator.calculateAndReport(in);
            return;
        }

        if (kind == 2) {
            in.type = DefectInput.DefectType.CRACK_IN_COMPRESSION;
            readMaterialsIfEmpty(sc, in, false);
            in.b = readDouble(sc, "Ширина сечения b (м) [например, 0,2]: ");
            in.h0 = readDouble(sc, "Рабочая высота сечения h0 (м) [например, 1,25]: ");
            in.As = readDouble(sc, "Площадь растянутой арматуры A_s (м²) [например, 0,006]: ");
            in.AsPrime = readDouble(sc, "Площадь сжатой арматуры A'_s (м²) [например, 0,0006]: ");
            in.asPrime = readDouble(sc, "Расстояние до центра сжатой арматуры a'_s (м) [например, 0,05]: ");
            in.xBarPhi = readDouble(sc, "Высота сжатой зоны по эпюре трещины x̄_φ (м) [например, 0,05]: ");
            in.Mbar = readDouble(sc, "Момент от испытательной нагрузки M̄ (кН·м) [например, 900]: ");
            in.Mult = readDouble(sc, "Предельный момент M по разделу 7 (кН·м) [0 — рассчитать; например, 1834]: ");
            DefectCalculator.calculateAndReport(in);
            return;
        }

        in.b = readDouble(sc, "Ширина сечения (ребра) b (м) [например, 0,2]: ");
        in.h0 = readDouble(sc, "Рабочая высота сечения h0 (м) [например, 1,25]: ");
        readMaterialsIfEmpty(sc, in, kind == 5);
        in.As = readDouble(sc, "Площадь растянутой арматуры A_s (м²) [например, 0,006]: ");
        in.AsPrime = readDouble(sc, "Площадь сжатой арматуры A'_s (м²) [например, 0,0006]: ");
        in.asPrime = readDouble(sc, "Расстояние до центра сжатой арматуры a'_s (м) [например, 0,05]: ");
        in.A0 = readDouble(sc, "Площадь ослабления раковиной/сколом A_0 (м²) [например, 0,003]: ");
        in.a0 = readDouble(sc, "Расстояние от растянутой арматуры до ц.т. ослабления a_0 (м) [например, 1,15]: ");

        if (kind == 3) in.type = DefectInput.DefectType.VOID_RECTANGULAR;
        else if (kind == 4) {
            in.type = DefectInput.DefectType.VOID_TSECTION;
            in.bfPrime = readDouble(sc, "Расчётная ширина полки b'_f (м) [например, 1,6]: ");
            in.hfPrime = readDouble(sc, "Приведённая толщина полки h'_f (м) [например, 0,18]: ");
        } else if (kind == 5) {
            in.type = DefectInput.DefectType.VOID_PRESTRESSED;
            in.bfPrime = readDouble(sc, "Расчётная ширина полки b'_f (м) [например, 1,6]: ");
            in.hfPrime = readDouble(sc, "Приведённая толщина полки h'_f (м) [например, 0,18]: ");
            in.Rp = readDouble(sc, "Сопротивление напрягаемой арматуры R_p (МПа) [например, 800]: ");
            in.sigmaPc = readDouble(sc, "Напряжение в напрягаемой арматуре сжатой зоны σ'_pc (МПа) [например, 300]: ");
            in.Ap = readDouble(sc, "Площадь растянутой напрягаемой арматуры A_p (м²) [например, 0,003]: ");
            in.ApPrime = readDouble(sc, "Площадь напрягаемой арматуры сжатой зоны A'_p (м²) [например, 0,0005]: ");
            in.apPrime = readDouble(sc, "Расстояние до центра A'_p — a'_p (м) [например, 0,05]: ");
        } else {
            System.out.println("Неверный выбор вида дефекта.");
            return;
        }
        DefectCalculator.calculateAndReport(in);
    }

    private static void readMaterialsIfEmpty(Scanner sc, DefectInput in, boolean prestressed) {
        if (in.Rb <= 0) in.Rb = readDouble(sc, "Сопротивление бетона сжатию R_b (МПа) [например, 17,0]: ");
        if (in.Rs <= 0) in.Rs = readDouble(sc, "Сопротивление арматуры растяжению R_s (МПа) [например, 250]: ");
        if (in.Rsc <= 0) in.Rsc = in.Rs;
    }

    private static void calculateCarbon(Scanner sc, BridgeContext ctx) {
        System.out.println("\n--- УСИЛЕНИЕ УГЛЕВОЛОКНОМ (Раздел 13) ---");
        int mode = readInt(sc, "Выберите проверку: 1 - момент, 2 - поперечная сила, 3 - наклонный момент, 4 - выносливость, 5 - технология: ");

        CarbonInput in = new CarbonInput();
        in.Rb = ctx.Rb; in.Rbt = ctx.Rbt; in.Rs = ctx.Rs; in.Rsc = ctx.Rs;
        in.Eb = ctx.Eb; in.Es = ctx.Es; in.nPrimeSteel = ctx.nPrime;

        if (mode == 5) {
            in.mode = CarbonInput.Mode.TECHNOLOGY;
            in.M = readDouble(sc, "Предельный момент неусиленного сечения M (кН·м) [например, 1834]: ");
            in.My = readDouble(sc, "Предельный момент усиленного сечения M^y (кН·м) [например, 2226]: ");
            in.Mp = readDouble(sc, "Момент от постоянных нагрузок M_p (кН·м) [например, 650]: ");
            in.Mk = readDouble(sc, "Момент от временной нагрузки при усилении M_k (кН·м) [например, 400]: ");
            in.MyK = readDouble(sc, "M^y_k (кН·м) [например, 200]: ");
            CarbonCalculator.calculateAndReport(in);
            return;
        }

        if (in.Rb <= 0) in.Rb = readDouble(sc, "R_b (МПа) [например, 17,0]: ");
        if (in.Rs <= 0) in.Rs = readDouble(sc, "R_s (МПа) [например, 250]: ");
        in.Rsc = in.Rs;

        in.Ef = readDouble(sc, "Модуль упругости углеволокна E_f (МПа) [например, 230000]: ");
        in.Rf = readDouble(sc, "Сопротивление углеволокна R_f (МПа) [например, 2400]: ");
        in.tfLayerMm = readDouble(sc, "Толщина одного слоя t_f (мм) [например, 0,13]: ");
        in.nLayers = readInt(sc, "Количество слоёв n [например, 2]: ");
        int reinfChoice = readInt(sc, "Тип усиления (табл. 13.4): 1-холст низ без закр., 2-холст низ с закр., 3-U-обойма без закр., 4-U-обойма с закр., 5-пластины с закр.: ");
        in.reinfType = reinforcementType(reinfChoice);

        if (mode == 1) {
            in.mode = CarbonInput.Mode.STRENGTH_MOMENT;
            in.b = readDouble(sc, "Ширина ребра b (м) [например, 0,2]: ");
            in.bfPrime = readDouble(sc, "Ширина полки b'_f (м) [например, 1,6]: ");
            in.hfPrime = readDouble(sc, "Толщина полки h'_f (м) [например, 0,18]: ");
            in.h = readDouble(sc, "Высота балки h (м) [например, 1,3]: ");
            in.h0 = readDouble(sc, "Рабочая высота h0 (м) [например, 1,25]: ");
            in.asPrime = readDouble(sc, "Расстояние до сжатой арматуры a'_s (м) [например, 0,05]: ");
            in.As = readDouble(sc, "Площадь растянутой арматуры A_s (м²) [например, 0,006]: ");
            in.AsPrime = readDouble(sc, "Площадь сжатой арматуры A'_s (м²) [например, 0,0006]: ");
            in.Af1 = readDouble(sc, "Площадь усиления на нижней грани A_f1 (м²) [например, 0,00013]: ");
            in.Af2 = readDouble(sc, "Площадь усиления на боковых гранях A_f2 (м², 0 — только низ) [например, 0,00006]: ");
            in.d = readDouble(sc, "Высота полок U-обоймы d (м) [например, 0,3]: ");
            in.epsBult = readDouble(sc, "Предельная деформация бетона ε_b,ult [например, 0,0033]: ");
            readMomentLoads(sc, in);
        } else if (mode == 2) {
            in.mode = CarbonInput.Mode.STRENGTH_SHEAR;
            if (in.Rbt <= 0) in.Rbt = readDouble(sc, "R_bt (МПа) [например, 1,2]: ");
            if (in.Eb <= 0) in.Eb = readDouble(sc, "E_b (МПа) [например, 30000]: ");
            in.b = readDouble(sc, "Ширина ребра b (м) [например, 0,2]: ");
            in.h0 = readDouble(sc, "Рабочая высота h0 (м) [например, 1,25]: ");
            in.Asw = readDouble(sc, "Площадь одной ветви хомутов A_sw (м²) [например, 0,0001]: ");
            in.s = readDouble(sc, "Шаг хомутов s (м) [например, 0,15]: ");
            in.sumAsiSinAlpha = readDouble(sc, "Σ A_si·sinα по отгибам (м², 0 — нет) [например, 0]: ");
            in.cShear = readDouble(sc, "Длина проекции c (м) [0 — вычислить по 13.14; например, 0]: ");
            in.Afi = readDouble(sc, "Σ площадей наклонных холстов A_fi (м²) [например, 0,00006]: ");
            in.phiFiberDeg = readDouble(sc, "Угол наклона холстов φ (град) [например, 90]: ");
            in.Afw = readDouble(sc, "Σ площадей вертикальных холстов A_fw (м²) [например, 0,00006]: ");
            in.Q = readDouble(sc, "Предельная поперечная сила Q (кН) [0 — вычислить; например, 0]: ");
            in.Qp = readDouble(sc, "Q_p (кН) [0 — вычислить по 13.11; например, 0]: ");
            if (in.Qp <= 0) {
                in.pp = readDouble(sc, "  p_p (кН/м) [например, 15]: ");
                in.pb = readDouble(sc, "  p_b (кН/м) [например, 20]: ");
                in.OmegaP = readDouble(sc, "  Ω_p (м) [например, 8,0]: ");
            }
            in.epsQ = readDouble(sc, "Доля нагрузки ε_Q [например, 0,55]: ");
            in.OmegaK = readDouble(sc, "Площадь линии влияния Ω_к (м) [например, 8,0]: ");
        } else if (mode == 3) {
            in.mode = CarbonInput.Mode.INCLINED_MOMENT;
            in.Rsw = readDouble(sc, "Сопротивление хомутов R_sw (МПа) [например, 250]: ");
            in.As = readDouble(sc, "Площадь растянутой арматуры A_s (м²) [например, 0,006]: ");
            in.zS = readDouble(sc, "плечо z_s (м) [например, 1,2]: ");
            in.Asw = readDouble(sc, "A_sw (м²) [например, 0,0001]: "); in.zSw = readDouble(sc, "z_sw (м) [например, 0,6]: ");
            in.sumAsi = readDouble(sc, "Σ A_si (м²) [например, 0]: "); in.zSi = readDouble(sc, "z_si (м) [например, 0]: ");
            in.Af1 = readDouble(sc, "A_f1 (м²) [например, 0,00013]: "); in.zC1 = readDouble(sc, "z_c1 (м) [например, 1,25]: ");
            in.Af2 = readDouble(sc, "A_f2 (м²) [например, 0,00006]: "); in.zC2 = readDouble(sc, "z_c2 (м) [например, 0,6]: ");
            in.Afw = readDouble(sc, "A_fw (м²) [например, 0,00006]: "); in.zCw = readDouble(sc, "z_cw (м) [например, 0,6]: ");
            in.Afi = readDouble(sc, "A_fi (м²) [например, 0,00006]: "); in.zCi = readDouble(sc, "z_ci (м) [например, 0,6]: ");
            readMomentLoads(sc, in);
        } else if (mode == 4) {
            in.mode = CarbonInput.Mode.FATIGUE;
            in.Rbf = readDouble(sc, "R_bf (МПа) [например, 8,5]: ");
            in.Rsf = readDouble(sc, "R_sf (МПа) [например, 120]: ");
            in.b = readDouble(sc, "Ширина ребра b (м) [например, 0,2]: ");
            in.bfPrime = readDouble(sc, "Ширина полки b'_f (м) [например, 1,6]: ");
            in.hfPrime = readDouble(sc, "Толщина полки h'_f (м) [например, 0,18]: ");
            in.h = readDouble(sc, "Высота балки h (м) [например, 1,3]: ");
            in.h0 = readDouble(sc, "Рабочая высота h0 (м) [например, 1,25]: ");
            in.asPrime = readDouble(sc, "a'_s (м) [например, 0,05]: ");
            in.as = readDouble(sc, "a_s (м) [например, 0,05]: ");
            in.au = readDouble(sc, "a_u (м) [например, 0,05]: ");
            in.As = readDouble(sc, "A_s (м²) [например, 0,006]: ");
            in.AsPrime = readDouble(sc, "A'_s (м²) [например, 0,0006]: ");
            in.Af1 = readDouble(sc, "A_f1 (м²) [например, 0,00013]: ");
            in.Af2 = readDouble(sc, "A_f2 (м²) [например, 0,00006]: ");
            in.AfPrime = readDouble(sc, "A'_f (м²) [например, 0,00013]: ");
            if (in.nPrimeSteel <= 0) in.nPrimeSteel = readDouble(sc, "n' (сталь/бетон) [например, 6,9]: ");
            in.nPrimeFiber = readDouble(sc, "n'_f (углеволокно/бетон) [например, 7,7]: ");
            in.Theta = readDouble(sc, "Коэффициент Θ [например, 1,0]: ");
            in.M = readDouble(sc, "Предельный момент M (кН·м) [например, 1800]: ");
            in.Mp = readDouble(sc, "Момент от постоянных M_p (кН·м) [например, 650]: ");
            in.epsM = readDouble(sc, "Доля нагрузки ε_M [например, 0,55]: ");
            in.Omega = readDouble(sc, "Площадь линии влияния Ω (м) [например, 32,0]: ");
        } else {
            System.out.println("Неверный выбор проверки.");
            return;
        }
        CarbonCalculator.calculateAndReport(in);
    }

    private static void readMomentLoads(Scanner sc, CarbonInput in) {
        in.M = readDouble(sc, "Предельный момент неусиленного сечения M (кН·м) [например, 1800]: ");
        in.Mp = readDouble(sc, "Момент от постоянных нагрузок M_p (кН·м) [например, 650]: ");
        in.epsM = readDouble(sc, "Доля временной нагрузки ε_M [например, 0,55]: ");
        in.Omega = readDouble(sc, "Площадь линии влияния Ω (м) [например, 32,0]: ");
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
        int action = readInt(sc, "Выберите действие: 1 - Все схемы, 2 - Информация о схеме, 3 - Подбор по %, 4 - Сравнение схем: ");

        switch (action) {
            case 1: CarbonRecommendationTables.printAllSchemes(); break;
            case 2:
                int schemeNum = readInt(sc, "Введите номер схемы (1-7): ");
                try {
                    CarbonRecommendationTables.StrengtheningScheme scheme = CarbonRecommendationTables.getScheme(schemeNum);
                    CarbonRecommendationCalculator.printSchemeDetails(scheme);
                } catch (IllegalArgumentException e) {
                    System.out.println("Ошибка: " + e.getMessage());
                }
                break;
            case 3:
                double requiredPercent = readDouble(sc, "Введите требуемое увеличение несущей способности (%): ");
                CarbonRecommendationTables.StrengtheningScheme recommended = CarbonRecommendationCalculator.recommendScheme(requiredPercent);
                CarbonRecommendationCalculator.printSchemeDetails(recommended);
                System.out.printf("\nРекомендованная схема обеспечивает увеличение на %.0f%%%n", recommended.getIncreasePercent());
                break;
            case 4:
                int count = readInt(sc, "Введите количество схем для сравнения (2-7): ");
                if (count < 2 || count > 7) { System.out.println("Неверное количество схем"); return; }
                CarbonRecommendationTables.StrengtheningScheme[] schemesToCompare = new CarbonRecommendationTables.StrengtheningScheme[count];
                System.out.println("Введите номера схем для сравнения:");
                for (int i = 0; i < count; i++) {
                    int num = readInt(sc, String.format("  Схема %d: ", i + 1));
                    schemesToCompare[i] = CarbonRecommendationTables.getScheme(num);
                }
                CarbonRecommendationCalculator.compareSchemes(schemesToCompare);
                break;
            default: System.out.println("Неверный выбор");
        }
    }

    private static void calculateInspection(Scanner sc, BridgeContext ctx) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   ОБСЛЕДОВАНИЕ И ИСПЫТАНИЕ ПРОЛЁТНЫХ СТРОЕНИЙ [Раздел 15]    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");

        int choice = readInt(sc, "Выберите расчёт: 1 - Смещение оси пути, 2 - Прочность бетона, 3 - Доля нагрузки по испытаниям, 4 - Полный расчёт: ");
        InspectionInput in = new InspectionInput();

        if (choice == 1 || choice == 4) {
            System.out.println("\n--- 15.3. СМЕЩЕНИЕ ОСИ ПУТИ (ф. 15.1) ---");
            in.aPrime = readDouble(sc, "a' — расстояние между внутр. гранью головки рельса и отвесом (м) [например, 0,85]: ");
            in.bPrime = readDouble(sc, "b' — расстояние от оси пролётного строения до отвеса (м) [например, 0,1]: ");
            in.b0Prime = readDouble(sc, "b'₀ — ширина колеи по внутр. граням головок рельсов (м) [например, 1,52]: ");
        }

        if (choice == 2 || choice == 4) {
            System.out.println("\n--- 15.4. ПРОЧНОСТЬ БЕТОНА (ф. 15.2) ---");
            int n = readInt(sc, "Число измерений n [например, 3]: ");
            in.concreteStrengths = new double[n];
            for (int i = 0; i < n; i++) {
                in.concreteStrengths[i] = readDouble(sc, String.format("  R%d (МПа) [например, 23]: ", i + 1));
            }
        }

        if (choice == 3 || choice == 4) {
            System.out.println("\n--- 15.5. ИСПЫТАНИЕ ПРОЛЁТНЫХ СТРОЕНИЙ (ф. 15.3) ---");
            int m = readInt(sc, "Число балок m [например, 2]: ");
            in.deflections = new double[m];
            in.inertias = new double[m];
            for (int i = 0; i < m; i++) {
                System.out.printf("\n  Балка %d:%n", i + 1);
                in.deflections[i] = readDouble(sc, String.format("    Прогиб f%d (мм, с учётом осадки опор) [например, 12]: ", i + 1));
                in.inertias[i] = readDouble(sc, String.format("    Момент инерции I%d (м⁴, без арматуры) [например, 0,045]: ", i + 1));
            }
            in.targetBeamIndex = readInt(sc, String.format("\nИндекс балки для расчёта ε_M (1..%d): ", m)) - 1;
        }

        InspectionOutput out = InspectionCalculator.calculateAndReport(in);

        if (choice == 1 || choice == 4) {
            ctx.trackOffsetLeft = out.trackOffset;
            ctx.trackOffsetRight = out.trackOffset;
            System.out.printf("\n[!] Смещение оси пути l = %.4f м сохранено в контекст.%n", out.trackOffset);
        }
        if (choice == 2 || choice == 4) {
            ctx.concreteStrengthR = out.avgConcreteStrength;
            System.out.printf("[!] Средняя прочность бетона R = %.2f МПа сохранена в контекст.%n", out.avgConcreteStrength);
            System.out.println("[!] Рекомендуется пересчитать характеристики материалов (пункт 1 меню).");
        }
        if (choice == 3 || choice == 4) {
            ctx.epsilonM_Beam1 = out.epsilonM;
            System.out.printf("[!] Уточнённая доля нагрузки ε_M = %.4f сохранена в контекст.%n", out.epsilonM);
            System.out.println("[!] Рекомендуется пересчитать главную балку (пункт 7 или 10 меню).");
        }
    }
}