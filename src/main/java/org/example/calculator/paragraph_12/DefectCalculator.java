package org.example.calculator.paragraph_12;

/**
 * Учёт влияния дефектов пролётного строения (Раздел 12 Руководства).
 *
 * <p>Реализованы формулы 12.1–12.7:
 * <ul>
 *   <li>12.2 — ослабление арматуры коррозией и выключенные из работы стержни (ф. 12.1);</li>
 *   <li>12.3 — трещины, заходящие в сжатую зону бетона (условие 12.2 и ф. 12.3);</li>
 *   <li>12.4 — раковины/сколы в сжатой зоне, прямоугольное сечение (ф. 12.4 + 12.5);</li>
 *   <li>12.4 — то же, тавровое сечение (ф. 12.6, момент по 7.22 + ф. 12.5);</li>
 *   <li>12.5 — дефекты в пролётных строениях с напрягаемой арматурой (ф. 12.7, момент по 9.2 + ф. 12.5).</li>
 * </ul>
 *
 * <p>Раздел 12 корректирует величины расчётов на прочность разделов 7–9, поэтому предельный
 * изгибающий момент здесь вычисляется теми же выражениями, что и в этих разделах, но с заменой
 * высоты сжатой зоны {@code x} на её значение с учётом дефекта ({@code x_φ} или {@code x_0}).
 *
 * <p>Методы {@code calculate…} — «чистые» (только считают и возвращают число / DefectOutput,
 * ничего не печатают). {@link #calculateAndReport(DefectInput)} дополнительно печатает
 * пошаговый отчёт в стиле проекта.
 *
 * <p>ЕДИНИЦЫ: сопротивления передаются в МПа и переводятся в кПа внутри класса; линейные
 * размеры — в метрах, площади — в м². Тогда моменты — в кН·м, высота сжатой зоны — в метрах.
 *
 * <p>ПРИМЕЧАНИЕ по п. 12.3: в Руководстве символы сравнения x_φ с расчётной высотой сжатой зоны
 * в тексте отсутствуют (утеряны при выгрузке). Принята физически обоснованная трактовка:
 * если стабильная высота сжатой зоны x_φ ≥ расчётной x (по разделу 7) — трещина несущую
 * способность не снижает и момент определяют по разделу 7; если x_φ &lt; x — предельный момент
 * определяют по формулам раздела 7 с заменой x на x_φ. При необходимости знак легко изменить
 * в методе {@link #calculateCrack(DefectInput)}.
 */
public final class DefectCalculator {

    private static final double MPA_TO_KPA = 1000.0;

    private DefectCalculator() {
    }

    // =====================================================================
    // 12.2 — ОСЛАБЛЕНИЕ АРМАТУРЫ КОРРОЗИЕЙ И ВЫКЛЮЧЕННЫЕ СТЕРЖНИ (ф. 12.1)
    // =====================================================================

    /**
     * Относительное изменение площади арматуры (ф. 12.1):
     * <pre>  j = A_si / A_s = (n·f_a − Σ f_i − n_2·f_a) / (n·f_a)  </pre>
     *
     * @param nBars        n — общее число стержней рабочей арматуры
     * @param faOne        f_a — площадь сечения одного неповреждённого стержня
     * @param corrodedLoss f_i — площади ослабления коррозией по каждому повреждённому стержню
     * @param nDisconnected n_2 — число выключенных из работы стержней
     * @return коэффициент j (безразмерный)
     */
    public static double relativeRebarArea(int nBars, double faOne,
                                           double[] corrodedLoss, int nDisconnected) {
        double AsFull = nBars * faOne;
        double sumFi = 0.0;
        if (corrodedLoss != null) {
            for (double fi : corrodedLoss) sumFi += fi;
        }
        double AsActual = AsFull - sumFi - nDisconnected * faOne;
        return AsActual / AsFull;
    }

    // =====================================================================
    // 12.4 / 12.6 / 12.7 — ВЫСОТА СЖАТОЙ ЗОНЫ С УЧЁТОМ РАКОВИНЫ/СКОЛА
    // =====================================================================

    /** Высота сжатой зоны прямоугольного сечения с ослаблением A_0 (ф. 12.4), м. */
    public static double compressionHeightVoidRect(double RbKpa, double RsKpa, double RscKpa,
                                                   double b, double As, double AsPrime, double A0) {
        return (RsKpa * As - RscKpa * AsPrime + RbKpa * A0) / (RbKpa * b);
    }

    /** Высота сжатой зоны таврового сечения с ослаблением A_0 (ф. 12.6), м. */
    public static double compressionHeightVoidTee(double RbKpa, double RsKpa, double RscKpa,
                                                  double b, double bfPrime, double hfPrime,
                                                  double As, double AsPrime, double A0) {
        return (RsKpa * As - RscKpa * AsPrime - RbKpa * (bfPrime - b) * hfPrime + RbKpa * A0)
                / (RbKpa * b);
    }

    /** Высота сжатой зоны таврового сечения с напрягаемой арматурой и ослаблением A_0 (ф. 12.7), м. */
    public static double compressionHeightVoidPrestressed(double RbKpa, double RsKpa, double RscKpa,
                                                          double RpKpa, double sigmaPcKpa,
                                                          double b, double bfPrime, double hfPrime,
                                                          double As, double AsPrime,
                                                          double Ap, double ApPrime, double A0) {
        return (RsKpa * As + RpKpa * Ap - RscKpa * AsPrime - sigmaPcKpa * ApPrime
                - RbKpa * (bfPrime - b) * hfPrime + RbKpa * A0) / (RbKpa * b);
    }

    // ---- предельные изгибающие моменты (те же, что в разделах 7 и 9) ----

    /** Предельный момент прямоугольного сечения при заданной высоте сжатой зоны (ф. 7.24 с x→x_0), кН·м. */
    private static double momentRect(double RbKpa, double RscKpa, double b, double x,
                                     double h0, double AsPrime, double asPrime) {
        return RbKpa * b * x * (h0 - 0.5 * x) + RscKpa * AsPrime * (h0 - asPrime);
    }

    /** Предельный момент таврового сечения при заданной высоте сжатой зоны (ф. 7.22 с x→x_0), кН·м. */
    private static double momentTee(double RbKpa, double RscKpa, double b, double bfPrime,
                                    double hfPrime, double x, double h0,
                                    double AsPrime, double asPrime) {
        return RbKpa * b * x * (h0 - 0.5 * x)
                + RbKpa * (bfPrime - b) * hfPrime * (h0 - 0.5 * hfPrime)
                + RscKpa * AsPrime * (h0 - asPrime);
    }

    /** Предельный момент таврового сечения с напрягаемой арматурой (ф. 9.2 с x→x_0), кН·м. */
    private static double momentPrestressed(double RbKpa, double RscKpa, double sigmaPcKpa,
                                            double b, double bfPrime, double hfPrime, double x,
                                            double h0, double AsPrime, double asPrime,
                                            double ApPrime, double apPrime) {
        return RbKpa * b * x * (h0 - 0.5 * x)
                + RbKpa * (bfPrime - b) * hfPrime * (h0 - 0.5 * hfPrime)
                + RscKpa * AsPrime * (h0 - asPrime)
                + sigmaPcKpa * ApPrime * (h0 - apPrime);
    }

    // =====================================================================
    // ДИСПЕТЧЕР
    // =====================================================================

    public static DefectOutput calculate(DefectInput in) {
        switch (in.type) {
            case CORROSION:            return calculateCorrosion(in);
            case CRACK_IN_COMPRESSION: return calculateCrack(in);
            case VOID_RECTANGULAR:     return calculateVoidRect(in);
            case VOID_TSECTION:        return calculateVoidTee(in);
            case VOID_PRESTRESSED:     return calculateVoidPrestressed(in);
            default: throw new IllegalArgumentException("Неизвестный вид дефекта: " + in.type);
        }
    }

    // ----- 12.2 -----
    private static DefectOutput calculateCorrosion(DefectInput in) {
        DefectOutput r = new DefectOutput();
        r.As_full = in.nBars * in.faOne;
        double sumFi = 0.0;
        if (in.corrodedLoss != null) for (double fi : in.corrodedLoss) sumFi += fi;
        r.As_actual = r.As_full - sumFi - in.nDisconnected * in.faOne;
        r.j = r.As_actual / r.As_full;
        r.note = "Фактическую площадь A_si = j·A_s подставляют в расчёты разделов 7/8/9.";
        return r;
    }

    // ----- 12.3 -----
    private static DefectOutput calculateCrack(DefectInput in) {
        DefectOutput r = new DefectOutput();
        double RbK = in.Rb * MPA_TO_KPA, RsK = in.Rs * MPA_TO_KPA, RscK = in.Rsc * MPA_TO_KPA;

        // Предельный момент M по разделу 7 (если не задан — считаем как прямоугольное сечение)
        double xDesign = (RsK * in.As - RscK * in.AsPrime) / (RbK * in.b); // высота сжатой зоны по разделу 7
        double M = in.Mult;
        if (M <= 0) {
            M = momentRect(RbK, RscK, in.b, xDesign, in.h0, in.AsPrime, in.asPrime);
        }
        r.xDesign = xDesign;

        // Условие 12.2 и стабильная высота сжатой зоны
        r.condFormula82 = in.Mbar >= 0.8 * M;                 // (12.2): M̄ ≥ 0,8·M
        if (r.condFormula82) {
            r.xPhi = in.xBarPhi;                              // используем измеренную x̄_φ
        } else {
            r.xPhi = in.xBarPhi / (1.5 - 0.63 * in.Mbar / M); // (12.3)
        }

        // Влияние трещины на предельный момент
        if (r.xPhi >= xDesign) {
            r.crackReducesCapacity = false;
            r.Mcrack = M;                                     // раздел 7 без изменений
        } else {
            r.crackReducesCapacity = true;
            r.Mcrack = momentRect(RbK, RscK, in.b, r.xPhi, in.h0, in.AsPrime, in.asPrime); // раздел 7 с x→x_φ
        }
        r.governingMoment = r.Mcrack;
        r.note = r.crackReducesCapacity
                ? "x_φ < x — трещина снижает несущую способность, момент пересчитан с x→x_φ."
                : "x_φ ≥ x — трещина несущую способность не снижает (момент по разделу 7).";
        return r;
    }

    // ----- 12.4 (прямоугольное) -----
    private static DefectOutput calculateVoidRect(DefectInput in) {
        DefectOutput r = new DefectOutput();
        double RbK = in.Rb * MPA_TO_KPA, RsK = in.Rs * MPA_TO_KPA, RscK = in.Rsc * MPA_TO_KPA;

        r.x0 = compressionHeightVoidRect(RbK, RsK, RscK, in.b, in.As, in.AsPrime, in.A0); // (12.4)
        r.Mx0 = momentRect(RbK, RscK, in.b, r.x0, in.h0, in.AsPrime, in.asPrime);         // M по 7.24 с x→x_0
        r.M0 = r.Mx0 - RbK * in.A0 * in.a0;                                               // (12.5)
        r.governingMoment = r.M0;
        r.note = "Прямоугольное сечение: x_0 по ф. 12.4, M_0 по ф. 12.5.";
        return r;
    }

    // ----- 12.4 (тавровое) -----
    private static DefectOutput calculateVoidTee(DefectInput in) {
        DefectOutput r = new DefectOutput();
        double RbK = in.Rb * MPA_TO_KPA, RsK = in.Rs * MPA_TO_KPA, RscK = in.Rsc * MPA_TO_KPA;

        r.x0 = compressionHeightVoidTee(RbK, RsK, RscK, in.b, in.bfPrime, in.hfPrime,
                in.As, in.AsPrime, in.A0);                                                // (12.6)
        r.Mx0 = momentTee(RbK, RscK, in.b, in.bfPrime, in.hfPrime, r.x0, in.h0,
                in.AsPrime, in.asPrime);                                                  // M по 7.22 с x→x_0
        r.M0 = r.Mx0 - RbK * in.A0 * in.a0;                                               // (12.5)
        r.governingMoment = r.M0;
        r.note = "Тавровое сечение: x_0 по ф. 12.6, момент по 7.22, вычет раковины по ф. 12.5.";
        return r;
    }

    // ----- 12.5 (напрягаемая арматура) -----
    private static DefectOutput calculateVoidPrestressed(DefectInput in) {
        DefectOutput r = new DefectOutput();
        double RbK = in.Rb * MPA_TO_KPA, RsK = in.Rs * MPA_TO_KPA, RscK = in.Rsc * MPA_TO_KPA;
        double RpK = in.Rp * MPA_TO_KPA, spcK = in.sigmaPc * MPA_TO_KPA;

        r.x0 = compressionHeightVoidPrestressed(RbK, RsK, RscK, RpK, spcK,
                in.b, in.bfPrime, in.hfPrime, in.As, in.AsPrime, in.Ap, in.ApPrime, in.A0); // (12.7)
        r.Mx0 = momentPrestressed(RbK, RscK, spcK, in.b, in.bfPrime, in.hfPrime, r.x0,
                in.h0, in.AsPrime, in.asPrime, in.ApPrime, in.apPrime);                     // M по 9.2 с x→x_0
        r.M0 = r.Mx0 - RbK * in.A0 * in.a0;                                                 // (12.5)
        r.governingMoment = r.M0;
        r.note = "Напрягаемая арматура, тавровое сечение: x_0 по ф. 12.7, момент по 9.2, вычет раковины по ф. 12.5.";
        return r;
    }

    // =====================================================================
    // ОТЧЁТ
    // =====================================================================

    public static void printReport(DefectInput in, DefectOutput r) {
        line();
        System.out.println(" УЧЁТ ВЛИЯНИЯ ДЕФЕКТОВ ПРОЛЁТНОГО СТРОЕНИЯ [Раздел 12]");
        line();

        switch (in.type) {
            case CORROSION:
                System.out.println("\n[12.2. Ослабление арматуры коррозией и выключенные стержни (ф. 12.1)]");
                System.out.printf("   n = %d ; f_a = %.6g ; выключено стержней n_2 = %d%n",
                        in.nBars, in.faOne, in.nDisconnected);
                System.out.printf("   Σ f_i (потери от коррозии) = %.6g%n", sumLoss(in.corrodedLoss));
                System.out.printf("   A_s  = n·f_a           = %.6g%n", r.As_full);
                System.out.printf("   A_si = n·f_a − Σf_i − n_2·f_a = %.6g%n", r.As_actual);
                System.out.printf("   >>> j = A_si / A_s = %.4f%n", r.j);
                break;

            case CRACK_IN_COMPRESSION:
                System.out.println("\n[12.3. Трещина в сжатой зоне (условие 12.2, ф. 12.3)]");
                System.out.printf("   x̄_φ (по эпюре раскрытия) = %.4f м%n", in.xBarPhi);
                System.out.printf("   M̄ (испытательный момент) = %.2f кН·м ; M (предельный, разд. 7) = %.2f кН·м%n",
                        in.Mbar, (in.Mult > 0 ? in.Mult : momentRectPreview(in)));
                System.out.printf("   Условие 12.2: M̄ ≥ 0,8·M  →  %s%n",
                        r.condFormula82 ? "выполнено (x_φ = x̄_φ)" : "не выполнено (x_φ по ф. 12.3)");
                System.out.printf("   x_φ = %.4f м ; расчётная x (разд. 7) = %.4f м%n", r.xPhi, r.xDesign);
                System.out.printf("   %s%n", r.note);
                System.out.printf("   >>> Предельный момент с учётом трещины M = %.2f кН·м%n", r.Mcrack);
                break;

            case VOID_RECTANGULAR:
                System.out.println("\n[12.4. Раковины/сколы бетона, прямоугольное сечение (ф. 12.4–12.5)]");
                printVoidCommon(in, r, "12.4");
                break;

            case VOID_TSECTION:
                System.out.println("\n[12.4. Раковины/сколы бетона, тавровое сечение (ф. 12.6, момент 7.22, ф. 12.5)]");
                System.out.printf("   b = %.3f м ; b'_f = %.3f м ; h'_f = %.3f м%n", in.b, in.bfPrime, in.hfPrime);
                printVoidCommon(in, r, "12.6");
                break;

            case VOID_PRESTRESSED:
                System.out.println("\n[12.5. Дефекты в пролётном строении с напрягаемой арматурой (ф. 12.7, момент 9.2, ф. 12.5)]");
                System.out.printf("   b = %.3f м ; b'_f = %.3f м ; h'_f = %.3f м%n", in.b, in.bfPrime, in.hfPrime);
                System.out.printf("   R_p = %.1f МПа ; σ'_pc = %.1f МПа ; A_p = %.6g м² ; A'_p = %.6g м²%n",
                        in.Rp, in.sigmaPc, in.Ap, in.ApPrime);
                printVoidCommon(in, r, "12.7");
                break;
        }
        line();
    }

    private static void printVoidCommon(DefectInput in, DefectOutput r, String heightFormula) {
        System.out.printf("   R_b = %.1f МПа ; R_s = %.1f МПа ; R_sc = %.1f МПа%n", in.Rb, in.Rs, in.Rsc);
        System.out.printf("   h0 = %.3f м ; a'_s = %.3f м ; A_s = %.6g м² ; A'_s = %.6g м²%n",
                in.h0, in.asPrime, in.As, in.AsPrime);
        System.out.printf("   A_0 (площадь ослабления) = %.6g м² ; a_0 = %.3f м%n", in.A0, in.a0);
        System.out.printf("   x_0 = %.4f м (ф. %s)%n", r.x0, heightFormula);
        System.out.printf("   M(x_0) = %.2f кН·м%n", r.Mx0);
        System.out.printf("   >>> M_0 = M − R_b·A_0·a_0 = %.2f кН·м (ф. 12.5)%n", r.M0);
    }

    public static DefectOutput calculateAndReport(DefectInput in) {
        DefectOutput r = calculate(in);
        printReport(in, r);
        return r;
    }

    // ----- вспомогательные -----
    private static double sumLoss(double[] a) {
        double s = 0.0;
        if (a != null) for (double v : a) s += v;
        return s;
    }

    private static double momentRectPreview(DefectInput in) {
        double RbK = in.Rb * MPA_TO_KPA, RsK = in.Rs * MPA_TO_KPA, RscK = in.Rsc * MPA_TO_KPA;
        double x = (RsK * in.As - RscK * in.AsPrime) / (RbK * in.b);
        return momentRect(RbK, RscK, in.b, x, in.h0, in.AsPrime, in.asPrime);
    }

    private static void line() {
        System.out.println("============================================================");
    }
}