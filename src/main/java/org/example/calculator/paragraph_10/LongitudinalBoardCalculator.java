package org.example.calculator.paragraph_10;

import org.example.calculator.common.FatigueResistance;
import org.example.calculator.common.RcSectionFormulas;

/**
 * Расчёт грузоподъёмности продольного борта балластного корыта (Раздел 10 Руководства).
 *
 * <p>Реализованы три группы проверок:
 * <ul>
 *   <li>10.1 — прочность по изгибающему моменту (ф. 10.1, 10.7, 10.8);</li>
 *   <li>10.2 — прочность по поперечной силе (ф. 10.9, 10.10);</li>
 *   <li>10.3 — выносливость бетона и арматуры (ф. 10.11, 10.12).</li>
 * </ul>
 *
 * <p>Метод {@link #calculate(BoardInput)} — «чистый» (возвращает результат, ничего не печатает),
 * его удобно вызывать из будущих разделов. Метод {@link #printReport(BoardInput, BoardResult)}
 * печатает пошаговый отчёт в стиле остальных калькуляторов проекта.
 *
 * <p>Функции F(a,z) и Ф(a,z) берутся из таблиц 10.2 и 10.4, которые уже включают коэффициент
 * Δ_br, поэтому отдельно он не вводится (примечание 3 к таблице 10.1).
 */
public final class LongitudinalBoardCalculator {

    private LongitudinalBoardCalculator() {
    }

    /** Полный расчёт борта без печати. */
    public static BoardResult calculate(BoardInput in) {
        BoardResult r = new BoardResult();

        // Перевод сопротивлений в кПа (размеры — в метрах, поэтому моменты выходят в кН·м)
        double RbkPa = in.Rb * 1000.0;
        double RbtKpa = in.Rbt * 1000.0;
        double RskPa = in.Rs * 1000.0;
        double RscKpa = in.Rsc * 1000.0;

        // === Общие постоянные воздействия ===
        r.sigmaBmax = sigmaBmax(in);                 // ф. 10.8
        r.Mp = permanentMoment(in, r.sigmaBmax, false); // ф. 10.7 (с коэф. надёжности)
        r.Qp = permanentShear(in, r.sigmaBmax);      // ф. 10.10

        // === 10.1 Прочность по изгибающему моменту ===
        r.Mlimit = RcSectionFormulas.limitMomentRect(
                RbkPa, RskPa, RscKpa, in.b, in.h0, in.As, in.AsPrime, in.asPrime); // ф. 7.11/7.14
        double F = BoardTables.F(in.hb, in.xShoulder);                             // табл. 10.2
        r.kMoment = F * (r.Mlimit - r.Mp);                                         // ф. 10.1

        // === 10.2 Прочность по поперечной силе ===
        r.Qlimit = RcSectionFormulas.limitShearForce(
                RskPa, RbtKpa, in.b, in.h0, safeC(in), in.sStirrup, in.sumAsiSinAlpha, in.Asw); // ф. 7.17
        double Phi = BoardTables.Phi(in.hb, in.xShoulder);                         // табл. 10.4
        r.kShear = Phi * (r.Qlimit - r.Qp);                                        // ф. 10.9

        // === 10.3 Выносливость ===
        r.Rbf = FatigueResistance.concrete(in.Rb, in.rhoB); // ф. 5.1, МПа
        r.Rsf = FatigueResistance.rebar(in.Rs, in.rhoS);    // ф. 5.2, МПа
        r.xFatigue = RcSectionFormulas.compressedZoneHeightFatigueRect(
                in.b, in.nPrime, in.As, in.AsPrime, in.h0, in.asPrime);            // ф. 7.39
        r.Ired = RcSectionFormulas.reducedInertiaRect(
                in.b, r.xFatigue, in.h0, in.nPrime, in.As, in.AsPrime, in.asPrime); // ф. 7.38

        double MpFat = permanentMoment(in, r.sigmaBmax, true); // ф. 10.7 при n_p = n'_p = 1
        // Функция F(a,z) в формулах 10.11/10.12 та же, но вместо n_k в знаменателе стоит
        // коэффициент уменьшения динамики ε (Приложение 3). Поскольку табличная F уже содержит
        // n_k, вводим множитель n_k/ε, чтобы заменить один коэффициент другим.
        double dynFactor = in.nk / in.epsilonDyn;
        double RbfKpa = r.Rbf * 1000.0;
        double RsfKpa = r.Rsf * 1000.0;

        double concreteCapacity = RbfKpa * r.Ired / r.xFatigue;                 // кН·м
        double rebarCapacity = RsfKpa * r.Ired / (in.nPrime * (in.h0 - r.xFatigue)); // кН·м
        r.kFatigueConcrete = F * dynFactor * (concreteCapacity - MpFat);        // ф. 10.11
        r.kFatigueRebar = F * dynFactor * (rebarCapacity - MpFat);             // ф. 10.12

        // === Итог ===
        r.kGoverning = r.minAllowable();
        r.governingCase = governingName(r);
        if (in.kc > 0) {
            r.classK = r.kGoverning / (in.kc * in.dynamicCoeff); // ф. 4.1
        }
        return r;
    }

    /** ф. 10.8: σ_b,max = γ·z·tg²(45 − φ/2), кН/м². */
    public static double sigmaBmax(BoardInput in) {
        double phiRad = Math.toRadians(45.0 - in.phiFrictionDeg / 2.0);
        double t = Math.tan(phiRad);
        return in.gammaBallast * in.hb * t * t;
    }

    /**
     * ф. 10.7: M_p = (1/6)·n'_p·σ_b,max·h_b²·b + n_p·p_t·y_t·b.
     * При расчёте на выносливость (forFatigue = true) коэффициенты надёжности принимаются равными 1.
     */
    public static double permanentMoment(BoardInput in, double sigmaBmax, boolean forFatigue) {
        double np = forFatigue ? 1.0 : in.np;
        double npPrime = forFatigue ? 1.0 : in.npPrime;
        double Mb = (1.0 / 6.0) * npPrime * sigmaBmax * in.hb * in.hb * in.b; // от веса балласта
        double Mt = np * in.pt * in.yt * in.b;                                // от веса тротуара
        return Mb + Mt;
    }

    /** ф. 10.10: Q_p = n_p·[P_0 + p_t·(l_t − l_k)] + 0.5·n'_p·σ_b,max·h_b·b, кН. */
    public static double permanentShear(BoardInput in, double sigmaBmax) {
        return in.np * (in.P0 + in.pt * (in.lt - in.lk))
                + 0.5 * in.npPrime * sigmaBmax * in.hb * in.b;
    }

    /** ф. 10.2: интенсивность временной вертикальной нагрузки q = k / l_s, кН/м². */
    public static double verticalIntensity(double k, double ls) {
        return k / ls;
    }

    /** ф. 10.3: интенсивность горизонтальной центробежной нагрузки τ = 180·k/(R·l_s) ≤ 0,15·k/l_s. */
    public static double centrifugalIntensity(double k, double R, double ls) {
        if (R <= 0) return 0.0;
        double tau = 180.0 * k / (R * ls);
        double limit = 0.15 * k / ls;
        return Math.min(tau, limit);
    }

    private static double safeC(BoardInput in) {
        // Длина проекции наклонного сечения не должна быть нулевой (деление на c).
        // Если пользователь её не задал, принимаем c = h0 как разумное значение по умолчанию.
        return in.cShear > 0 ? in.cShear : in.h0;
    }

    private static String governingName(BoardResult r) {
        double m = r.kGoverning;
        if (m == r.kMoment) return "прочность по изгибающему моменту (10.1)";
        if (m == r.kShear) return "прочность по поперечной силе (10.9)";
        if (m == r.kFatigueConcrete) return "выносливость бетона (10.11)";
        return "выносливость арматуры (10.12)";
    }

    /** Пошаговый отчёт по расчёту борта. */
    public static void printReport(BoardInput in, BoardResult r) {
        line();
        System.out.println(" РАСЧЁТ ГРУЗОПОДЪЁМНОСТИ ПРОДОЛЬНОГО БОРТА [Раздел 10]");
        line();

        System.out.println("\n[1. Постоянные воздействия]");
        System.out.printf("   σ_b,max = γ·z·tg²(45−φ/2) = %.1f·%.2f·tg²(45−%.0f/2) = %.3f кН/м² (ф. 10.8)%n",
                in.gammaBallast, in.hb, in.phiFrictionDeg, r.sigmaBmax);
        System.out.printf("   M_p = 1/6·n'_p·σ·h_b²·b + n_p·p_t·y_t·b = %.4f кН·м (ф. 10.7)%n", r.Mp);
        System.out.printf("   Q_p = n_p·[P₀+p_t(l_t−l_k)] + 0.5·n'_p·σ·h_b·b = %.3f кН (ф. 10.10)%n", r.Qp);

        System.out.println("\n[2. Прочность по изгибающему моменту (10.1)]");
        System.out.printf("   Предельный момент M = %.4f кН·м (ф. 7.11/7.14)%n", r.Mlimit);
        System.out.printf("   F(a,z) при h_br=%.2f м, x=%.2f м: %.3f 1/м² (табл. 10.2)%n",
                in.hb, in.xShoulder, BoardTables.F(in.hb, in.xShoulder));
        System.out.printf("   k = F·(M − M_p) = %.2f кН/м%n", r.kMoment);

        System.out.println("\n[3. Прочность по поперечной силе (10.2)]");
        System.out.printf("   Предельная поперечная сила Q = %.3f кН (ф. 7.17)%n", r.Qlimit);
        System.out.printf("   Ф(a,z) при h_br=%.2f м, x=%.2f м: %.3f 1/м (табл. 10.4)%n",
                in.hb, in.xShoulder, BoardTables.Phi(in.hb, in.xShoulder));
        System.out.printf("   k = Ф·(Q − Q_p) = %.2f кН/м%n", r.kShear);

        System.out.println("\n[4. Выносливость (10.3)]");
        System.out.printf("   R_bf = 0.6·β_b·R_b = %.2f МПа (ф. 5.1, ρ_b=%.2f)%n", r.Rbf, in.rhoB);
        System.out.printf("   R_sf = ε_ρ·R_s = %.2f МПа (ф. 5.2, ρ=%.2f)%n", r.Rsf, in.rhoS);
        System.out.printf("   x' = %.4f м (ф. 7.39),  I_red = %.6f м⁴ (ф. 7.38)%n", r.xFatigue, r.Ired);
        System.out.printf("   ε (Приложение 3) = %.3f%n", in.epsilonDyn);
        System.out.printf("   k по выносливости бетона   = %.2f кН/м (ф. 10.11)%n", r.kFatigueConcrete);
        System.out.printf("   k по выносливости арматуры = %.2f кН/м (ф. 10.12)%n", r.kFatigueRebar);

        line();
        System.out.println(" ИТОГ ПО ПРОДОЛЬНОМУ БОРТУ");
        line();
        System.out.printf("   k(момент)          = %8.2f кН/м%n", r.kMoment);
        System.out.printf("   k(поперечная сила) = %8.2f кН/м%n", r.kShear);
        System.out.printf("   k(вынос. бетон)    = %8.2f кН/м%n", r.kFatigueConcrete);
        System.out.printf("   k(вынос. арматура) = %8.2f кН/м%n", r.kFatigueRebar);
        System.out.printf("   >>> ОПРЕДЕЛЯЮЩАЯ ДОПУСКАЕМАЯ НАГРУЗКА k = %.2f кН/м%n", r.kGoverning);
        System.out.printf("       (определяет %s)%n", r.governingCase);
        if (r.classK != null) {
            System.out.printf("   >>> КЛАСС БОРТА K = k / (k_c·(1+μ)) = %.2f (ф. 4.1)%n", r.classK);
        }
        // Справочно — интенсивности временной нагрузки при определяющем k
        System.out.printf("   Справочно: q = k/l_s = %.3f кН/м²; τ = %.3f кН/м² (кривая R=%.0f м)%n",
                verticalIntensity(r.kGoverning, in.ls),
                centrifugalIntensity(r.kGoverning, in.R, in.ls), in.R);
        line();
    }

    /** Расчёт с печатью отчёта. */
    public static BoardResult calculateAndReport(BoardInput in) {
        BoardResult r = calculate(in);
        printReport(in, r);
        return r;
    }

    private static void line() {
        System.out.println("============================================================");
    }
}