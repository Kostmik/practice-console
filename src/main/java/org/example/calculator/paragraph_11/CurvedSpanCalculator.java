package org.example.calculator.paragraph_11;

/**
 * Расчёт доли временной вертикальной нагрузки, приходящейся на главную балку пролётного
 * строения, расположенного на кривой (Раздел 11 Руководства).
 *
 * <p>Согласно п. 11 расчёт на кривой аналогичен расчёту на прямом участке (раздел 6),
 * но доля нагрузки ε определяется с учётом смещения нагрузки от возвышения наружного рельса
 * и центробежной силы. Полученные ε_M и ε_Q далее подставляются в расчёт балки вместо долей
 * для прямого участка.
 *
 * <p>Реализованы формулы 11.1–11.14. Метод {@link #calculate(CurvedSpanInput)} — «чистый»
 * (без печати), {@link #printReport} печатает пошаговый отчёт в стиле проекта.
 *
 * <p>ВАЖНО о моментах инерции: в документе одно обозначение I_y′ отвечает двум разным
 * выражениям — 4-членному (ф. 11.7) и 6-членному (ф. 11.10, с добавками b·H·c²/2 и H·b³/6).
 * В данной реализации принято:
 * <ul>
 *   <li>I_y′ по ф. 11.10 (6 слагаемых) — в формуле 11.8 для d;</li>
 *   <li>I_y′ по ф. 11.7  (4 слагаемых) — в члене кручения формулы 11.2.</li>
 * </ul>
 * Если сверка с документом покажет иное распределение — достаточно поменять местами
 * ссылки на r.IyPrime10 и r.IyPrime7.
 */
public final class CurvedSpanCalculator {

    private CurvedSpanCalculator() {
    }

    public static CurvedSpanResult calculate(CurvedSpanInput in) {
        CurvedSpanResult r = new CurvedSpanResult();

        double n = in.Es / in.Eb; // условное отношение модулей (входит как Es/Eb)

        // --- 11.14: угол возвышения наружного рельса ---
        r.sinAlpha = in.cantElevation / in.b0;
        r.cosAlpha = Math.sqrt(1.0 - r.sinAlpha * r.sinAlpha);

        // --- 11.9: геометрия ---
        r.c1 = in.lk + 0.5 * in.b;
        r.H = in.h - 0.5 * in.h1 - in.as;

        double c = in.c, b = in.b, h1 = in.h1, h2 = in.h2, c1 = r.c1, H = r.H;
        double EsAs = in.Es * in.As; // произведение (для наглядности в формулах Es·As)

        // --- 11.10: момент инерции I_y′ (6 слагаемых) ---
        r.IyPrime10 = h1 * cube(c) / 12.0
                + h2 * cube(c1) / 6.0
                + c1 * h2 * sq(c + c1) / 2.0
                + n * in.As * sq(c) / 2.0
                + b * H * sq(c) / 2.0
                + H * cube(b) / 6.0;

        // --- 11.8: d (использует I_y′ по ф. 11.10) ---
        r.d = sq(c) * H / (4.0 * r.IyPrime10) * (H * b + 2.0 * n * in.As);

        // --- 11.6: ω ---
        r.omega = 0.5 * c * (H - r.d);

        // --- 11.13: ω_D, ω_κ, y ---
        r.omegaD = 0.5 * c * r.d;
        r.omegaK = (0.5 * c + c1) * r.d;
        r.y = r.omega * H / (r.omega + r.omegaD);

        // --- 11.5: I_к (кручение) ---
        r.Ik = 0.747 * (0.5 * c * cube(h1) + c1 * cube(h2) + H * cube(b));

        // --- 11.7: I_y′ (4 слагаемых) ---
        r.IyPrime7 = h1 * cube(c) / 12.0
                + h2 * cube(c1) / 6.0
                + c1 * h2 * sq(c + c1) / 2.0
                + n * in.As * sq(c) / 2.0;

        // --- 11.12: I_ω (секториальный) ---
        r.Iomega = (2.0 / 3.0) * (
                b * (H - r.y) * sq(r.omegaD)
                        + b * r.y * sq(r.omega)
                        + 3.0 * n * in.As * sq(r.omega)
                        + c * h1 * sq(r.omegaD) / 2.0
                        + c1 * h1 * (sq(r.omegaD) + r.omegaD * r.omegaK + sq(r.omegaK))
        );

        // --- 11.11: m ---
        r.m = Math.sqrt(0.42 * r.Ik / r.Iomega);

        // --- параметр g и коэффициент γ (таблица 11.1) ---
        r.g = 0.5 * r.m * in.l;
        r.gamma = CurvedSpanTables.gamma(r.g);

        // --- 11.2 / 11.3: смещение нагрузки, поезд движется ---
        double kCentr = 0.008 * sq(in.v) * r.cosAlpha / (in.R * in.Theta * (1.0 + in.mu0));
        double torsion = 0.0263 * c * sq(in.l) * r.Ik / (r.omega * r.IyPrime7 * (1.0 - 1.0 / r.gamma));

        double bracketM = (in.ht + in.hp + in.hs) * r.cosAlpha
                + 0.5 * in.ls * r.sinAlpha
                + in.hb + 0.5 * h1
                + torsion - r.d;
        r.dlM_moving = kCentr * bracketM - in.ht * r.sinAlpha;

        double bracketQ = (in.ht + in.hp + in.hs) * r.cosAlpha
                + 0.5 * in.ls * r.sinAlpha
                + in.hb + in.h;
        r.dlQ_moving = kCentr * bracketQ - in.ht * r.sinAlpha;

        // --- 11.4: поезд стоит ---
        r.dl_standing = -in.ht * r.sinAlpha;

        // --- 11.1: доли ε (балка 1 «+», балка 2 «−») ---
        // поезд движется
        r.epsM_beam1 = 0.5 + (in.lPrime + r.dlM_moving) / c;
        r.epsQ_beam1 = 0.5 + (in.lDoublePrime + r.dlQ_moving) / c;
        r.epsM_beam2 = 0.5 + (in.lPrime - r.dlM_moving) / c;
        r.epsQ_beam2 = 0.5 + (in.lDoublePrime - r.dlQ_moving) / c;
        // поезд стоит
        r.epsM_beam1_standing = 0.5 + (in.lPrime + r.dl_standing) / c;
        r.epsQ_beam1_standing = 0.5 + (in.lDoublePrime + r.dl_standing) / c;
        r.epsM_beam2_standing = 0.5 + (in.lPrime - r.dl_standing) / c;
        r.epsQ_beam2_standing = 0.5 + (in.lDoublePrime - r.dl_standing) / c;

        // --- определяющие доли на наиболее загруженную балку ---
        r.epsM_design = max4(r.epsM_beam1, r.epsM_beam2, r.epsM_beam1_standing, r.epsM_beam2_standing);
        r.epsQ_design = max4(r.epsQ_beam1, r.epsQ_beam2, r.epsQ_beam1_standing, r.epsQ_beam2_standing);

        return r;
    }

    // ----- вспомогательные -----
    private static double sq(double x) {
        return x * x;
    }

    private static double cube(double x) {
        return x * x * x;
    }

    private static double max4(double a, double b, double c, double d) {
        return Math.max(Math.max(a, b), Math.max(c, d));
    }

    public static void printReport(CurvedSpanInput in, CurvedSpanResult r) {
        line();
        System.out.println(" РАСЧЁТ ДОЛИ НАГРУЗКИ НА БАЛКУ, ПРОЛЁТ НА КРИВОЙ [Раздел 11]");
        line();

        System.out.println("\n[1. Угол возвышения наружного рельса (11.14)]");
        System.out.printf("   sinα = Δh/b0 = %.5f ;  cosα = %.5f%n", r.sinAlpha, r.cosAlpha);

        System.out.println("\n[2. Геометрия и моменты инерции сечения]");
        System.out.printf("   c1 = l_k + 0.5·b = %.4f м (11.9)%n", r.c1);
        System.out.printf("   H  = h − 0.5·h1 − a_s = %.4f м (11.9)%n", r.H);
        System.out.printf("   I_y′ = %.6f м⁴ (11.10, 6 слагаемых, для d)%n", r.IyPrime10);
        System.out.printf("   d    = %.6f м (11.8)%n", r.d);
        System.out.printf("   ω    = 0.5·c·(H−d) = %.6f м² (11.6)%n", r.omega);
        System.out.printf("   ω_D  = %.6f ; ω_κ = %.6f ; y = %.6f (11.13)%n", r.omegaD, r.omegaK, r.y);
        System.out.printf("   I_к  = %.6f м⁴ (11.5)%n", r.Ik);
        System.out.printf("   I_y′ = %.6f м⁴ (11.7, 4 слагаемых, для кручения в 11.2)%n", r.IyPrime7);
        System.out.printf("   I_ω  = %.6f (11.12)%n", r.Iomega);
        System.out.printf("   m    = √(0.42·I_к/I_ω) = %.5f (11.11)%n", r.m);
        System.out.printf("   g    = 0.5·m·l = %.4f  →  γ = %.4f (табл. 11.1)%n", r.g, r.gamma);

        System.out.println("\n[3. Смещение вертикальной нагрузки]");
        System.out.printf("   поезд движется: Δl_M = %.5f м (11.2) ; Δl_Q = %.5f м (11.3)%n",
                r.dlM_moving, r.dlQ_moving);
        System.out.printf("   поезд стоит:    Δl_M = Δl_Q = %.5f м (11.4)%n", r.dl_standing);

        System.out.println("\n[4. Доли нагрузки ε (11.1)]");
        System.out.println("   поезд движется:");
        System.out.printf("     балка 1 (+): ε_M = %.4f ; ε_Q = %.4f%n", r.epsM_beam1, r.epsQ_beam1);
        System.out.printf("     балка 2 (−): ε_M = %.4f ; ε_Q = %.4f%n", r.epsM_beam2, r.epsQ_beam2);
        System.out.println("   поезд стоит:");
        System.out.printf("     балка 1 (+): ε_M = %.4f ; ε_Q = %.4f%n",
                r.epsM_beam1_standing, r.epsQ_beam1_standing);
        System.out.printf("     балка 2 (−): ε_M = %.4f ; ε_Q = %.4f%n",
                r.epsM_beam2_standing, r.epsQ_beam2_standing);

        line();
        System.out.printf("   >>> ОПРЕДЕЛЯЮЩИЕ ДОЛИ: ε_M = %.4f ; ε_Q = %.4f%n", r.epsM_design, r.epsQ_design);
        System.out.println("   (эти доли подставляются в расчёт балки вместо долей для прямого участка)");
        line();
    }

    public static CurvedSpanResult calculateAndReport(CurvedSpanInput in) {
        CurvedSpanResult r = calculate(in);
        printReport(in, r);
        return r;
    }

    private static void line() {
        System.out.println("============================================================");
    }
}