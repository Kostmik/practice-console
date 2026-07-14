package org.example.calculator.paragraph_13;

/**
 * Расчёт усиления пролётного строения композиционными материалами на основе углеродного
 * волокна (Раздел 13 Руководства).
 *
 * <p>Реализованы формулы 13.1–13.29, сгруппированные по режимам {@link CarbonInput.Mode}:
 * <ul>
 *   <li>STRENGTH_MOMENT — прочность нормального сечения по моменту (13.1–13.9);</li>
 *   <li>STRENGTH_SHEAR — прочность наклонного сечения по поперечной силе (13.10–13.16);</li>
 *   <li>INCLINED_MOMENT — прочность наклонного сечения по моменту (13.17–13.18);</li>
 *   <li>FATIGUE — выносливость усиленного сечения (13.19–13.28);</li>
 *   <li>TECHNOLOGY — учёт технологии усиления (13.29).</li>
 * </ul>
 *
 * <p>Методы {@code calculate…} — «чистые» (только считают, ничего не печатают).
 * {@link #calculateAndReport(CarbonInput)} дополнительно печатает пошаговый отчёт.
 *
 * <p>ЕДИНИЦЫ: сопротивления/напряжения задаются в МПа. Для сил и моментов они переводятся в кПа
 * (×1000), тогда моменты — в кН·м, силы — в кН. В эмпирических формулах, где сопротивление входит
 * не как множитель к площади (ω = 0,85 − 0,008·R_b по ф. 7.13; φ_b = 1 − 0,01·R_b; σ_fu по ф. 13.2;
 * φ_w), используется числовое значение в МПа — так, как в Руководстве.
 *
 * <p>ПРИМЕЧАНИЯ по неоднозначностям выгрузки документа:
 * <ol>
 *   <li>ф. 13.6/13.8 (высота сжатой зоны) содержат σ_fu,2, которое само зависит от x (ф. 13.3),
 *       поэтому x и σ_fu,2 определяются совместно простой итерацией до сходимости.</li>
 *   <li>ф. 13.28 в тексте содержит x'_y в правой части (в отличие от аналогичной ф. 13.25, где стоит
 *       (h0 + a_s)). Реализовано буквально по тексту через итерацию по x'_y; если это опечатка
 *       выгрузки — достаточно убрать член «− x'_y» в {@link #fatigueCompressionHeight}.</li>
 * </ol>
 */
public final class CarbonCalculator {

    private static final double K = 1000.0; // МПа → кПа

    private CarbonCalculator() {
    }

    // =====================================================================
    // ДИСПЕТЧЕР
    // =====================================================================

    public static CarbonOutput calculate(CarbonInput in) {
        switch (in.mode) {
            case STRENGTH_MOMENT: return strengthMoment(in);
            case STRENGTH_SHEAR:  return strengthShear(in);
            case INCLINED_MOMENT: return inclinedMoment(in);
            case FATIGUE:         return fatigue(in);
            case TECHNOLOGY:      return technology(in);
            default: throw new IllegalArgumentException("Неизвестный режим: " + in.mode);
        }
    }

    // =====================================================================
    // 13.2 — ПРОЧНОСТЬ ПО ИЗГИБАЮЩЕМУ МОМЕНТУ (13.1–13.9)
    // =====================================================================

    /** Предельное напряжение в материале усиления σ_fu (ф. 13.2), МПа, с ограничением ≤ 0,9·R_f. */
    public static double limitFiberStress(CarbonInput in) {
        double ks = in.reinfType.ks();
        double sumTf = in.nLayers * in.tfLayerMm;                  // Σ t_f, мм
        double sigma = ks * Math.sqrt(in.Rb * in.Ef * in.bUnitMm / sumTf); // (13.2)
        return Math.min(sigma, 0.9 * in.Rf);
    }

    /** Характеристика сжатой зоны ω (ф. 7.13). */
    public static double omega(double Rb) {
        return 0.85 - 0.008 * Rb;
    }

    /** Граничная относительная высота сжатой зоны ξ_fy (ф. 13.4). */
    public static double xiFy(double omega, double sigmaFu, double epsBult, double Ef) {
        return omega / (1.0 + (sigmaFu / (epsBult * Ef)) * (1.0 - omega / 1.1));
    }

    private static CarbonOutput strengthMoment(CarbonInput in) {
        CarbonOutput r = new CarbonOutput();
        r.sigmaFu = limitFiberStress(in);                         // (13.2)
        r.omega = omega(in.Rb);                                   // (7.13)
        r.xiFy = xiFy(r.omega, r.sigmaFu, in.epsBult, in.Ef);     // (13.4)

        double RbK = in.Rb * K, RsK = in.Rs * K, RscK = in.Rsc * K, sfuK = r.sigmaFu * K;

        // --- совместное определение x и σ_fu,2 (13.6/13.8 + 13.3) итерацией ---
        double sfu2 = r.sigmaFu;
        double x = 0.0;
        boolean web = false;
        for (int it = 0; it < 100; it++) {
            double sfu2K = sfu2 * K;
            // граница в плите (13.6), ширина b'_f
            x = (RsK * in.As + sfuK * (in.Af1 + in.Af2) - 0.5 * (sfuK - sfu2K) * in.Af2 - RscK * in.AsPrime)
                    / (RbK * in.bfPrime);
            web = false;
            if (x > in.hfPrime) {                                 // граница в ребре (13.8)
                x = (RsK * in.As + sfuK * (in.Af1 + in.Af2) - 0.5 * (sfuK - sfu2K) * in.Af2
                        - RscK * in.AsPrime - RbK * (in.bfPrime - in.b) * in.hfPrime) / (RbK * in.b);
                web = true;
            }
            double denom = in.h - x;
            double newSfu2 = (denom != 0.0) ? r.sigmaFu * (in.h - in.d - x) / denom : r.sigmaFu; // (13.3)
            if (Math.abs(newSfu2 - sfu2) < 1e-9) {
                sfu2 = newSfu2;
                break;
            }
            sfu2 = newSfu2;
        }

        // --- ограничение по ξ_fy (13.5) ---
        if (in.h0 > 0 && x / in.h0 > r.xiFy) {
            x = r.xiFy * in.h0;
            r.xiCapped = true;
        }
        r.x = x;
        r.boundaryInWeb = web;
        r.sigmaFu2 = sfu2;
        double sfu2K = sfu2 * K;

        // --- предельный момент усиленного сечения (13.7 в плите / 13.9 в ребре) ---
        double fiberTerms = sfuK * in.Af1 * in.h
                + sfu2K * in.Af2 * (in.h - in.d / 2.0)
                + 0.5 * (sfuK - sfu2K) * (in.h - in.d / 3.0) * in.Af2;
        if (!web) {
            r.My = RsK * in.As * in.h0 - 0.5 * RbK * in.bfPrime * x * x
                    - RscK * in.AsPrime * in.asPrime + fiberTerms;                        // (13.7)
        } else {
            r.My = RsK * in.As * in.h0 - 0.5 * RbK * in.bfPrime * in.hfPrime * in.hfPrime
                    - 0.5 * RbK * in.b * x * x - RscK * in.AsPrime * in.asPrime + fiberTerms; // (13.9)
        }

        // --- допускаемая нагрузка (13.1) ---
        r.kMoment = allowableByMoment(r.My, in.M, in.Mp, in.nk, in.epsM, in.Omega);
        r.governingK = r.kMoment;
        r.note = (web ? "Граница сжатой зоны в ребре (ф. 13.8/13.9)." : "Граница сжатой зоны в плите (ф. 13.6/13.7).")
                + (r.xiCapped ? " Сработало ограничение ξ > ξ_fy (x = ξ_fy·h0)." : "");
        return r;
    }

    /** Допускаемая временная нагрузка по моменту (ф. 13.1 и 13.17), кН/м. */
    public static double allowableByMoment(double My, double M, double Mp,
                                           double nk, double epsM, double omegaInflLine) {
        return (My * (M - Mp)) / (M * nk * epsM * omegaInflLine);
    }

    // =====================================================================
    // 13.2 — ПРОЧНОСТЬ ПО ПОПЕРЕЧНОЙ СИЛЕ (13.10–13.16)
    // =====================================================================

    /** Длина проекции наклонного сечения c (ф. 13.14), м. */
    public static double inclinedProjection(double RbtKpa, double b, double h0, double s,
                                            double RsKpa, double Asw) {
        return Math.sqrt(2.5 * RbtKpa * b * h0 * h0 * s / (RsKpa * Asw));
    }

    /** Поперечная сила, воспринимаемая бетоном Q_b (ф. 13.13), кН. */
    public static double shearConcrete(double RbtKpa, double b, double h0, double c) {
        return 2.0 * RbtKpa * b * h0 * h0 / c;
    }

    private static CarbonOutput strengthShear(CarbonInput in) {
        CarbonOutput r = new CarbonOutput();
        double RbK = in.Rb * K, RsK = in.Rs * K, RbtK = in.Rbt * K, sfuK = r_sigmaFuOrGiven(in) * K;
        double sinAlpha = 1.0; // отгибы задаются суммой A_si·sinα, поэтому здесь множитель уже учтён -> 1
        double sinPhi = Math.sin(Math.toRadians(in.phiFiberDeg));

        // c (13.14) — если не задано; но не более 2·h0 (примечание к 13.14)
        r.c = (in.cShear > 0) ? in.cShear
                : inclinedProjection(RbtK, in.b, in.h0, in.s, RsK, in.Asw);
        r.c = Math.min(r.c, 2.0 * in.h0);

        r.Qb = shearConcrete(RbtK, in.b, in.h0, r.c);              // (13.13)

        // Q неусиленного (13.12)
        r.Qunstr = (in.Q > 0) ? in.Q
                : 0.8 * RsK * in.sumAsiSinAlpha * sinAlpha + 0.8 * RsK * in.Asw * r.c / in.s + r.Qb;

        // Q^y усиленного: минимум из 13.15 и 13.16
        double mu = in.Asw / (in.b * in.s);
        double phiW = 1.0 + 5.0 * (in.Es * mu) / in.Eb;
        double phiB = 1.0 - 0.01 * in.Rb;
        r.QyConcrete = 0.3 * phiW * phiB * RbK * in.b * in.h0;     // (13.15)
        r.QyCrack = 0.8 * RsK * in.sumAsiSinAlpha * sinAlpha + 0.8 * RsK * in.Asw * r.c / in.s + r.Qb
                + sfuK * in.Afi * sinPhi + sfuK * in.Afw;          // (13.16)
        r.Qy = Math.min(r.QyConcrete, r.QyCrack);

        // Q_p (13.11)
        r.QpCalc = (in.Qp > 0) ? in.Qp
                : (in.np * in.pp + in.npPrime * in.pb) * in.OmegaP;

        // k (13.10)
        r.kShear = (r.Qy * (r.Qunstr - r.QpCalc)) / (r.Qunstr * in.nk * in.epsQ * in.OmegaK);
        r.governingK = r.kShear;
        r.note = "Q^y = min(по сжатому бетону 13.15, по наклонной трещине 13.16).";
        return r;
    }

    // σ_fu для сдвига (если материал усиления задан) либо 0
    private static double r_sigmaFuOrGiven(CarbonInput in) {
        if (in.Ef > 0 && in.Rf > 0 && in.tfLayerMm > 0) return limitFiberStress(in);
        return 0.0;
    }

    // =====================================================================
    // 13.2 — МОМЕНТ В НАКЛОННОМ СЕЧЕНИИ (13.17–13.18)
    // =====================================================================

    private static CarbonOutput inclinedMoment(CarbonInput in) {
        CarbonOutput r = new CarbonOutput();
        double RsK = in.Rs * K, RswK = in.Rsw * K, sfuK = r_sigmaFuOrGiven(in) * K;
        r.sigmaFu = sfuK / K;

        // (13.18): суммы задаются представительными произведениями A·z
        r.MyInclined = RsK * in.As * in.zS
                + RswK * in.Asw * in.zSw
                + RsK * in.sumAsi * in.zSi
                + sfuK * in.Af1 * in.zC1
                + sfuK * in.Af2 * in.zC2
                + sfuK * in.Afw * in.zCw
                + sfuK * in.Afi * in.zCi;

        // (13.17)
        r.kInclined = allowableByMoment(r.MyInclined, in.M, in.Mp, in.nk, in.epsM, in.Omega);
        r.governingK = r.kInclined;
        r.note = "M^y и k для наклонного сечения по изгибающему моменту (ф. 13.18, 13.17).";
        return r;
    }

    // =====================================================================
    // 13.3 — ВЫНОСЛИВОСТЬ (13.19–13.28)
    // =====================================================================

    /**
     * Высота сжатой зоны усиленного сечения для выносливости x'_y (ф. 13.23).
     * Сначала пробуется тавровый случай (13.24–13.25); если x'_y ≤ h'_f — прямоугольный (13.27–13.28).
     * Формула 13.28 содержит x'_y в правой части, поэтому решается итерацией.
     */
    public static double[] fatigueCompressionHeight(CarbonInput in) {
        double Af = in.Af1 + in.Af2;
        // тавр (13.24, 13.25)
        double s = ((in.bfPrime - in.b) * in.hfPrime + in.nPrimeSteel * (in.As + in.AsPrime)
                + in.nPrimeFiber * Af) / in.b;
        double rr = ((in.bfPrime - in.b) * in.hfPrime * in.hfPrime
                + 2.0 * in.nPrimeSteel * (in.As * in.h0 + in.AsPrime * in.asPrime)
                + 2.0 * in.nPrimeFiber * Af * (in.h0 + in.as)) / in.b;
        double xy = -s + Math.sqrt(s * s + rr);                   // (13.23)
        if (xy > in.hfPrime) {
            return new double[]{xy, 1.0}; // тавровое (флаг 1)
        }
        // прямоугольное (13.27, 13.28) — 13.28 неявна по x'_y, итерация
        s = (in.nPrimeSteel * (in.As + in.AsPrime) + in.nPrimeFiber * Af) / in.bfPrime;
        xy = in.hfPrime;
        for (int it = 0; it < 100; it++) {
            rr = (2.0 * in.nPrimeSteel * (in.As * in.h0 + in.AsPrime * in.asPrime)
                    + 2.0 * in.nPrimeFiber * Af * (in.h0 - xy + in.as)) / in.bfPrime;
            double next = -s + Math.sqrt(s * s + rr);
            if (Math.abs(next - xy) < 1e-12) {
                xy = next;
                break;
            }
            xy = next;
        }
        return new double[]{xy, 0.0}; // прямоугольное (флаг 0)
    }

    /** Приведённый момент инерции усиленного сечения I^y_red (ф. 13.22 — тавр / 13.26 — прямоуг.), м⁴. */
    public static double reducedInertia(CarbonInput in, double xy, boolean tee) {
        double base = in.bfPrime * xy * xy * xy / 3.0;
        double steel = in.nPrimeSteel * in.As * sq(in.h0 - xy)
                + in.nPrimeSteel * in.AsPrime * sq(xy - in.asPrime);
        double fiber = in.nPrimeFiber * in.AfPrime * sq(in.h0 - xy + in.asPrime);
        if (tee) {
            base -= (in.bfPrime - in.b) * Math.pow(xy - in.hfPrime, 3) / 3.0; // (13.22)
        }
        return base + steel + fiber;                                          // (13.22 / 13.26)
    }

    private static CarbonOutput fatigue(CarbonInput in) {
        CarbonOutput r = new CarbonOutput();
        double[] xyRes = fatigueCompressionHeight(in);
        r.xyFatigue = xyRes[0];
        r.fatigueTee = xyRes[1] == 1.0;
        r.IredFatigue = reducedInertia(in, r.xyFatigue, r.fatigueTee);        // (13.22 / 13.26)

        double RbfK = in.Rbf * K, RsfK = in.Rsf * K;
        double Af = in.Af1 + in.Af2;
        r.es = CarbonTables.es(in.As > 0 ? Af / in.As : 0.0);                 // таблица 13.5

        r.MyConcrete = RbfK * r.IredFatigue / r.xyFatigue;                    // (13.20)
        r.MyRebar = RsfK * r.IredFatigue
                / (r.es * in.nPrimeSteel * (in.h - r.xyFatigue - in.au));     // (13.21)

        // допускаемые нагрузки по выносливости (13.19) — для бетона и для арматуры
        r.kFatigueConcrete = (r.MyConcrete * (in.M - in.Mp)) / (in.M * in.Theta * in.epsM * in.Omega);
        r.kFatigueRebar = (r.MyRebar * (in.M - in.Mp)) / (in.M * in.Theta * in.epsM * in.Omega);
        r.governingK = Math.min(r.kFatigueConcrete, r.kFatigueRebar);
        r.note = (r.fatigueTee ? "Тавровое сечение (ф. 13.22)." : "Прямоугольное сечение (ф. 13.26).")
                + " Определяющая нагрузка — минимум из бетона и арматуры.";
        return r;
    }

    // =====================================================================
    // 13.4 — УЧЁТ ТЕХНОЛОГИИ УСИЛЕНИЯ (13.29)
    // =====================================================================

    /** Предельный момент усиленного сечения с учётом технологии M^уб (ф. 13.29), кН·м. */
    public static double momentWithTechnology(double M, double My, double Mp, double Mk, double MyK) {
        return M + (My - M) * (Mk - MyK) / (Mp + Mk);
    }

    private static CarbonOutput technology(CarbonInput in) {
        CarbonOutput r = new CarbonOutput();
        r.My = in.My;
        r.Mub = momentWithTechnology(in.M, in.My, in.Mp, in.Mk, in.MyK); // (13.29)
        r.note = "M^уб — предельный момент усиленного сечения с учётом технологии усиления (ф. 13.29).";
        return r;
    }

    // =====================================================================
    // ОТЧЁТ
    // =====================================================================

    public static void printReport(CarbonInput in, CarbonOutput r) {
        line();
        System.out.println(" УСИЛЕНИЕ КОМПОЗИЦИОННЫМИ МАТЕРИАЛАМИ (УГЛЕВОЛОКНО) [Раздел 13]");
        line();

        switch (in.mode) {
            case STRENGTH_MOMENT:
                System.out.println("\n[13.2. Прочность нормального сечения по изгибающему моменту]");
                System.out.printf("   Тип усиления: %s (k_s = %.2f, табл. 13.4)%n",
                        in.reinfType.title(), in.reinfType.ks());
                System.out.printf("   σ_fu = %.1f МПа (ф. 13.2, ≤ 0,9·R_f = %.1f)%n", r.sigmaFu, 0.9 * in.Rf);
                System.out.printf("   σ_fu,2 = %.1f МПа (ф. 13.3)%n", r.sigmaFu2);
                System.out.printf("   ω = %.4f (ф. 7.13) ; ξ_fy = %.4f (ф. 13.4)%n", r.omega, r.xiFy);
                System.out.printf("   x = %.4f м ; %s%n", r.x, r.note);
                System.out.printf("   M^y = %.2f кН·м (ф. %s)%n", r.My, r.boundaryInWeb ? "13.9" : "13.7");
                System.out.printf("   >>> k = M^y·(M − M_p)/(M·n_k·ε_M·Ω) = %.2f кН/м (ф. 13.1)%n", r.kMoment);
                break;

            case STRENGTH_SHEAR:
                System.out.println("\n[13.2. Прочность наклонного сечения по поперечной силе]");
                System.out.printf("   c = %.4f м (ф. 13.14) ; Q_b = %.2f кН (ф. 13.13)%n", r.c, r.Qb);
                System.out.printf("   Q (неусиленное) = %.2f кН (ф. 13.12)%n", r.Qunstr);
                System.out.printf("   Q^y по бетону = %.2f кН (ф. 13.15) ; Q^y по трещине = %.2f кН (ф. 13.16)%n",
                        r.QyConcrete, r.QyCrack);
                System.out.printf("   Q^y = %.2f кН ; Q_p = %.2f кН (ф. 13.11)%n", r.Qy, r.QpCalc);
                System.out.printf("   >>> k = Q^y·(Q − Q_p)/(Q·n_k·ε_Q·Ω_к) = %.2f кН/м (ф. 13.10)%n", r.kShear);
                break;

            case INCLINED_MOMENT:
                System.out.println("\n[13.2. Прочность наклонного сечения по изгибающему моменту]");
                System.out.printf("   M^y = %.2f кН·м (ф. 13.18)%n", r.MyInclined);
                System.out.printf("   >>> k = M^y·(M − M_p)/(M·n_k·ε_M·Ω) = %.2f кН/м (ф. 13.17)%n", r.kInclined);
                break;

            case FATIGUE:
                System.out.println("\n[13.3. Выносливость усиленного сечения]");
                System.out.printf("   x'_y = %.4f м (ф. 13.23) ; %s%n", r.xyFatigue,
                        r.fatigueTee ? "тавровое" : "прямоугольное");
                System.out.printf("   I^y_red = %.6f м⁴ (ф. %s)%n", r.IredFatigue, r.fatigueTee ? "13.22" : "13.26");
                System.out.printf("   e_s = %.3f (табл. 13.5)%n", r.es);
                System.out.printf("   M^y (бетон) = %.2f кН·м (ф. 13.20) ; M^y (арматура) = %.2f кН·м (ф. 13.21)%n",
                        r.MyConcrete, r.MyRebar);
                System.out.printf("   k (бетон) = %.2f ; k (арматура) = %.2f кН/м (ф. 13.19)%n",
                        r.kFatigueConcrete, r.kFatigueRebar);
                System.out.printf("   >>> Определяющая k = %.2f кН/м%n", r.governingK);
                break;

            case TECHNOLOGY:
                System.out.println("\n[13.4. Учёт технологии усиления]");
                System.out.printf("   M = %.2f ; M^y = %.2f ; M_p = %.2f ; M_k = %.2f ; M^y_k = %.2f (кН·м)%n",
                        in.M, in.My, in.Mp, in.Mk, in.MyK);
                System.out.printf("   >>> M^уб = %.2f кН·м (ф. 13.29)%n", r.Mub);
                break;
        }
        line();
    }

    public static CarbonOutput calculateAndReport(CarbonInput in) {
        CarbonOutput r = calculate(in);
        printReport(in, r);
        return r;
    }

    // ----- вспомогательные -----
    private static double sq(double x) {
        return x * x;
    }

    private static void line() {
        System.out.println("============================================================");
    }
}