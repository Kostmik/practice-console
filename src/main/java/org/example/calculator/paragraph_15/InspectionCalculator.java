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
     * @return массив [epsilonM, sumFiIi, fTargetITarget]
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
    // НОВЫЕ МЕТОДЫ: Подробный текстовый отчёт с подстановкой чисел
    // =====================================================================

    public static void printReport153(double aPrime, double bPrime, double b0Prime, double trackOffset) {
        System.out.println("[15.3. Определение смещения оси пути (ф. 15.1)]");
        System.out.println("Исходные данные:");
        System.out.printf("  a'  = %.4f м (расстояние от внутр. грани головки рельса до отвеса)%n", aPrime);
        System.out.printf("  b'  = %.4f м (расстояние от оси пролётного строения до отвеса)%n", bPrime);
        System.out.printf("  b'₀ = %.4f м (ширина колеи по внутр. граням головок рельсов)%n", b0Prime);
        System.out.println("Расчет:");
        System.out.printf("  l = a' − b' − 0,5·b'₀ = %.4f − %.4f − 0,5·%.4f = %.4f м%n", aPrime, bPrime, b0Prime, trackOffset);
        System.out.println("Результат:");
        if (trackOffset > 0) {
            System.out.println("  Смещение оси пути — в сторону балки 1.");
        } else if (trackOffset < 0) {
            System.out.println("  Смещение оси пути — в сторону балки 2.");
        } else {
            System.out.println("  Смещение оси пути отсутствует.");
        }
    }

    public static void printReport154(double[] strengths, double avg) {
        System.out.println("[15.4. Определение прочности бетона (ф. 15.2)]");
        System.out.printf("  Число измерений: n = %d%n", strengths.length);

        // Формируем строку для подстановки в формулу
        StringBuilder sumStr = new StringBuilder();
        for (int i = 0; i < strengths.length; i++) {
            sumStr.append(String.format("%.1f", strengths[i]));
            if (i < strengths.length - 1) {
                sumStr.append(" + ");
            }
        }

        System.out.printf("  Единичные значения Rᵢ (МПа): %s%n", sumStr.toString());
        System.out.println("Расчет:");
        System.out.printf("  R̄ = ΣRᵢ / n = (%s) / %d = %.2f МПа%n", sumStr.toString(), strengths.length, avg);
    }

    public static void printReport155(double[] deflections, double[] inertias, int targetIndex, double sum, double fTarget, double epsilon) {
        System.out.println("[15.5. Испытание пролётных строений (ф. 15.3)]");
        System.out.printf("  Число балок: m = %d%n", deflections.length);
        System.out.printf("  Целевая балка для расчета: № %d (индекс %d)%n", targetIndex + 1, targetIndex);
        System.out.println("Расчет произведений fᵢ·Iᵢ по каждой балке:");

        for (int i = 0; i < deflections.length; i++) {
            String marker = (i == targetIndex) ? "  <-- Целевая балка" : "";
            System.out.printf("  Балка %d: %.3f · %.6f = %.6f%s%n",
                    i + 1, deflections[i], inertias[i], deflections[i] * inertias[i], marker);
        }

        System.out.println("Суммирование и итоговый расчет:");
        System.out.printf("  Σ(fᵢ·Iᵢ) = %.6f%n", sum);
        System.out.printf("  f·I (целевая балка №%d) = %.6f%n", targetIndex + 1, fTarget);
        System.out.println("Итоговый расчет доли временной нагрузки:");
        System.out.printf("  ε_M = (f·I) / Σ(fᵢ·Iᵢ) = %.6f / %.6f = %.4f%n", fTarget, sum, epsilon);
    }

    // =====================================================================
    // СТАРЫЕ МЕТОДЫ: Оставлены для обратной совместимости (если где-то используются)
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
        printReport153(in.aPrime, in.bPrime, in.b0Prime, out.trackOffset);

        if (in.concreteStrengths != null && in.concreteStrengths.length > 0) {
            printReport154(in.concreteStrengths, out.avgConcreteStrength);
        }

        if (in.deflections != null && in.deflections.length > 0) {
            printReport155(in.deflections, in.inertias, in.targetBeamIndex, out.sumFiIi, out.fTargetITarget, out.epsilonM);
        }
    }

    public static InspectionOutput calculateAndReport(InspectionInput in) {
        InspectionOutput out = calculate(in);
        printReport(in, out);
        return out;
    }
}