package org.example.calculator.paragraph_15;

/**
 * Калькулятор Раздела 15 "Обследование и испытание пролётных строений".
 *
 * Реализованы формулы:
 *   (15.1) l = a' − b' − 0,5·b'₀
 *   (15.2) R̄ = ΣRᵢ / n
 *   (15.3) ε_M = (f·I) / Σ(fᵢ·Iᵢ)
 */
public final class InspectionCalculator {

    private InspectionCalculator() {
    }

    // =====================================================================
    // 15.3. Определение смещения оси пути (ф. 15.1)
    // =====================================================================
    /**
     * Смещение оси пути относительно оси пролётного строения.
     * Положительное значение — смещение в сторону балки 1.
     */
    public static double calculateTrackOffset(double aPrime, double bPrime, double b0Prime) {
        return aPrime - bPrime - 0.5 * b0Prime;
    }

    // =====================================================================
    // 15.4. Определение прочности бетона (ф. 15.2)
    // =====================================================================
    /**
     * Средняя прочность бетона по результатам неразрушающего контроля.
     *
     * @param strengths массив единичных значений прочности Rᵢ, МПа
     * @return среднее значение R̄, МПа
     */
    public static double calculateAvgConcreteStrength(double[] strengths) {
        if (strengths == null || strengths.length == 0) {
            throw new IllegalArgumentException("Массив значений прочности бетона не может быть пустым");
        }
        double sum = 0.0;
        for (double r : strengths) {
            sum += r;
        }
        return sum / strengths.length;
    }

    // =====================================================================
    // 15.5. Испытание пролётных строений (ф. 15.3)
    // =====================================================================
    /**
     * Доля временной нагрузки, приходящаяся на одну балку,
     * уточнённая по результатам испытаний (прогибам).
     *
     * @param deflections массив прогибов балок fᵢ, мм (с учётом осадки опор)
     * @param inertias    массив моментов инерции бетонного сечения Iᵢ, м⁴
     * @param targetIndex индекс балки, для которой считается ε_M (0-based)
     * @return ε_M — доля временной нагрузки
     */
    public static double[] calculateShareByTest(double[] deflections, double[] inertias, int targetIndex) {
        if (deflections == null || inertias == null) {
            throw new IllegalArgumentException("Массивы прогибов и моментов инерции не могут быть null");
        }
        if (deflections.length != inertias.length) {
            throw new IllegalArgumentException("Длины массивов прогибов и моментов инерции должны совпадать");
        }
        if (targetIndex < 0 || targetIndex >= deflections.length) {
            throw new IllegalArgumentException("Недопустимый индекс балки: " + targetIndex);
        }

        double sumFiIi = 0.0;
        for (int i = 0; i < deflections.length; i++) {
            sumFiIi += deflections[i] * inertias[i];
        }

        double fTargetITarget = deflections[targetIndex] * inertias[targetIndex];
        double epsilonM = (sumFiIi != 0.0) ? fTargetITarget / sumFiIi : 0.0;

        return new double[]{epsilonM, sumFiIi, fTargetITarget};
    }

    // =====================================================================
    // Сводный расчёт и отчёт
    // =====================================================================
    public static InspectionOutput calculate(InspectionInput in) {
        InspectionOutput out = new InspectionOutput();

        // 15.3
        out.trackOffset = calculateTrackOffset(in.aPrime, in.bPrime, in.b0Prime);

        // 15.4
        if (in.concreteStrengths != null && in.concreteStrengths.length > 0) {
            out.avgConcreteStrength = calculateAvgConcreteStrength(in.concreteStrengths);
            out.numberOfMeasurements = in.concreteStrengths.length;
        }

        // 15.5
        if (in.deflections != null && in.inertias != null
            && in.deflections.length > 0 && in.inertias.length > 0) {
            double[] res = calculateShareByTest(in.deflections, in.inertias, in.targetBeamIndex);
            out.epsilonM = res[0];
            out.sumFiIi = res[1];
            out.fTargetITarget = res[2];
        }

        return out;
    }

    public static void printReport(InspectionInput in, InspectionOutput out) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║   ОБСЛЕДОВАНИЕ И ИСПЫТАНИЕ ПРОЛЁТНЫХ СТРОЕНИЙ [Раздел 15]    ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");

        // 15.3
        System.out.println("\n[15.3. Определение смещения оси пути (ф. 15.1)]");
        System.out.printf("   a'  = %.4f м (расст. внутр. грани головки рельса — отвес)%n", in.aPrime);
        System.out.printf("   b'  = %.4f м (расст. оси пролётного строения — отвес)%n", in.bPrime);
        System.out.printf("   b'₀ = %.4f м (ширина колеи по внутр. граням головок рельсов)%n", in.b0Prime);
        System.out.printf("   >>> l = a' − b' − 0,5·b'₀ = %.4f м%n", out.trackOffset);
        if (out.trackOffset > 0) {
            System.out.println("   Смещение оси пути — в сторону балки 1.");
        } else if (out.trackOffset < 0) {
            System.out.println("   Смещение оси пути — в сторону балки 2.");
        } else {
            System.out.println("   Смещение оси пути отсутствует.");
        }

        // 15.4
        if (in.concreteStrengths != null && in.concreteStrengths.length > 0) {
            System.out.println("\n[15.4. Определение прочности бетона (ф. 15.2)]");
            System.out.printf("   Число измерений n = %d%n", out.numberOfMeasurements);
            System.out.print("   Единичные значения Rᵢ (МПа): ");
            for (int i = 0; i < in.concreteStrengths.length; i++) {
                System.out.printf("%.1f", in.concreteStrengths[i]);
                if (i < in.concreteStrengths.length - 1) System.out.print(", ");
            }
            System.out.println();
            System.out.printf("   >>> R̄ = ΣRᵢ / n = %.2f МПа%n", out.avgConcreteStrength);
        }

        // 15.5
        if (in.deflections != null && in.deflections.length > 0) {
            System.out.println("\n[15.5. Испытание пролётных строений (ф. 15.3)]");
            System.out.printf("   Число балок m = %d%n", in.deflections.length);
            System.out.printf("   Целевая балка (индекс %d):%n", in.targetBeamIndex);
            System.out.println("   ┌──────┬────────────┬────────────┬──────────────┐");
            System.out.println("   │ Балка│  Прогиб fᵢ │  Момент Iᵢ │   fᵢ · Iᵢ    │");
            System.out.println("   │      │    (мм)    │    (м⁴)    │              │");
            System.out.println("   ├──────┼────────────┼────────────┼──────────────┤");
            for (int i = 0; i < in.deflections.length; i++) {
                String marker = (i == in.targetBeamIndex) ? " ◄──" : "";
                System.out.printf("   │  %3d │ %10.3f │ %10.6f │ %12.6f │%s%n",
                    i + 1, in.deflections[i], in.inertias[i],
                    in.deflections[i] * in.inertias[i], marker);
            }
            System.out.println("   └──────┴────────────┴────────────┴──────────────┘");
            System.out.printf("   Σ(f·Iᵢ) = %.6f%n", out.sumFiIi);
            System.out.printf("   f·I (балка %d) = %.6f%n", in.targetBeamIndex + 1, out.fTargetITarget);
            System.out.printf("   >>> ε_M = (f·I) / Σ(f·Iᵢ) = %.4f%n", out.epsilonM);
        }

        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    public static InspectionOutput calculateAndReport(InspectionInput in) {
        InspectionOutput out = calculate(in);
        printReport(in, out);
        return out;
    }
}