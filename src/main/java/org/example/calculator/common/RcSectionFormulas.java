package org.example.calculator.common;

/**
 * Переиспользуемые формулы прочности и геометрии железобетонного сечения из Раздела 7
 * Руководства (по опалубочным и рабочим чертежам).
 *
 * <p>Класс намеренно сделан набором "чистых" функций (принимают числа — возвращают число,
 * ничего не печатают), чтобы его можно было использовать из любого раздела: 10, 11, 12, 13.
 * Раздел 10 (продольный борт) использует прямоугольные варианты формул.
 *
 * <p>ЕДИНИЦЫ: сопротивления передаются в кПа, линейные размеры — в метрах, площади — в м².
 * Тогда моменты получаются в кН·м, а поперечные силы — в кН.
 *
 * <p>Соответствие формулам Руководства:
 * <ul>
 *   <li>{@link #limitMomentRect} — ф. 7.11 и 7.14 (предельный изгибающий момент);</li>
 *   <li>{@link #limitShearForce} — ф. 7.17 (предельная поперечная сила);</li>
 *   <li>{@link #compressedZoneHeightFatigueRect} — ф. 7.39 (высота сжатой зоны для выносливости);</li>
 *   <li>{@link #reducedInertiaRect} — ф. 7.38 (момент инерции приведённого сечения).</li>
 * </ul>
 */
public final class RcSectionFormulas {

    private RcSectionFormulas() {
    }

    /**
     * Предельный изгибающий момент прямоугольного сечения с ненапрягаемой арматурой
     * (ф. 7.11 / 7.14). Логика выбора формулы повторяет п. 7.2 Руководства:
     * <ul>
     *   <li>x &lt; a's — сжатую арматуру не учитываем (ф. 7.11 без A's);</li>
     *   <li>a's ≤ x &lt; 2·a's — ф. 7.14;</li>
     *   <li>x ≥ 2·a's — ф. 7.11 с учётом сжатой арматуры.</li>
     * </ul>
     *
     * @param RbkPa   расчётное сопротивление бетона сжатию, кПа
     * @param RskPa   расчётное сопротивление растянутой арматуры, кПа
     * @param RscKpa  расчётное сопротивление сжатой арматуры, кПа (для ненапрягаемой R_sc = R_s)
     * @param b       ширина сечения, м
     * @param h0      рабочая высота сечения, м
     * @param As      площадь растянутой арматуры, м²
     * @param AsPrime площадь сжатой арматуры, м²
     * @param asPrime расстояние от сжатой грани до центра сжатой арматуры, м
     * @return предельный изгибающий момент, кН·м
     */
    public static double limitMomentRect(double RbkPa, double RskPa, double RscKpa,
                                         double b, double h0,
                                         double As, double AsPrime, double asPrime) {
        // высота сжатой зоны без учёта сжатой арматуры (первое приближение)
        double x0 = RskPa * As / (RbkPa * b);

        if (x0 < asPrime) {
            // ф. 7.11 без сжатой арматуры
            return RbkPa * b * x0 * (h0 - 0.5 * x0);
        } else if (x0 < 2.0 * asPrime) {
            // ф. 7.14
            return RskPa * As * (h0 - asPrime);
        } else {
            // ф. 7.11 с учётом сжатой арматуры
            double x = (RskPa * As - RscKpa * AsPrime) / (RbkPa * b);
            return RbkPa * b * x * (h0 - 0.5 * x) + RscKpa * AsPrime * (h0 - asPrime);
        }
    }

    /**
     * Высота сжатой зоны прямоугольного сечения из условий прочности (без проверок на a's).
     * Используется, когда нужна именно величина x (например, для контроля ξ).
     *
     * @return высота сжатой зоны x, м
     */
    public static double compressedZoneHeightRect(double RbkPa, double RskPa, double RscKpa,
                                                  double b, double As, double AsPrime) {
        return (RskPa * As - RscKpa * AsPrime) / (RbkPa * b);
    }

    /**
     * Предельная поперечная сила (ф. 7.17).
     * Для сплошного сечения продольного борта без отогнутых стержней и хомутов
     * достаточно передать sumAsiSinAlpha = 0 и Asw = 0 — тогда останется только Q_b.
     *
     * @param RskPa          расчётное сопротивление арматуры, кПа
     * @param RbtKpa         расчётное сопротивление бетона растяжению, кПа
     * @param b              ширина сечения, м
     * @param h0             рабочая высота, м
     * @param c              длина проекции наклонного сечения на продольную ось, м
     * @param s              шаг хомутов, м (можно передать 1.0, если Asw = 0)
     * @param sumAsiSinAlpha сумма A_si·sinα по отогнутым стержням, м²
     * @param Asw            площадь одной ветви хомутов, м²
     * @return предельная поперечная сила, кН
     */
    public static double limitShearForce(double RskPa, double RbtKpa,
                                         double b, double h0, double c, double s,
                                         double sumAsiSinAlpha, double Asw) {
        double Qb = 2.0 * RbtKpa * b * h0 * h0 / c;              // поперечная сила, воспринимаемая бетоном
        double Qsw = (s > 0) ? 0.8 * RskPa * Asw * c / s : 0.0;  // вклад хомутов
        double Qsi = 0.8 * RskPa * sumAsiSinAlpha;               // вклад отогнутых стержней
        return Qsi + Qsw + Qb;
    }

    /**
     * Высота сжатой зоны приведённого сечения для расчётов на выносливость (ф. 7.39),
     * прямоугольный случай. Получается из условия равенства статических моментов
     * относительно нейтральной оси: x' = -s + sqrt(s² + r).
     *
     * @param b       ширина сечения, м
     * @param nPrime  условное отношение модулей упругости n' (п. 5.2.1)
     * @param As      площадь растянутой арматуры, м²
     * @param AsPrime площадь сжатой арматуры, м²
     * @param h0      рабочая высота, м
     * @param asPrime расстояние от сжатой грани до сжатой арматуры, м
     * @return высота сжатой зоны x', м
     */
    public static double compressedZoneHeightFatigueRect(double b, double nPrime,
                                                         double As, double AsPrime,
                                                         double h0, double asPrime) {
        double sCoef = nPrime * (As + AsPrime) / b;
        double rCoef = 2.0 * nPrime * (As * h0 + AsPrime * asPrime) / b;
        return -sCoef + Math.sqrt(sCoef * sCoef + rCoef);
    }

    /**
     * Момент инерции приведённого прямоугольного сечения (ф. 7.38).
     *
     * @param b       ширина сечения, м
     * @param xPrime  высота сжатой зоны для выносливости (ф. 7.39), м
     * @param h0      рабочая высота, м
     * @param nPrime  условное отношение модулей упругости n'
     * @param As      площадь растянутой арматуры, м²
     * @param AsPrime площадь сжатой арматуры, м²
     * @param asPrime расстояние до центра сжатой арматуры, м
     * @return момент инерции приведённого сечения I_red, м⁴
     */
    public static double reducedInertiaRect(double b, double xPrime, double h0,
                                            double nPrime, double As, double AsPrime,
                                            double asPrime) {
        return b * Math.pow(xPrime, 3) / 3.0
                + nPrime * As * Math.pow(h0 - xPrime, 2)
                + nPrime * AsPrime * Math.pow(xPrime - asPrime, 2);
    }
}