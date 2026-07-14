package org.example.calculator.paragraph_14;

import org.example.calculator.paragraph_14.CarbonRecommendationTables.StrengtheningScheme;

/**
 * Калькулятор рекомендаций по усилению (Раздел 14 Руководства)
 */
public class CarbonRecommendationCalculator {

    private CarbonRecommendationCalculator() {
    }

    /**
     * Рассчитать увеличение несущей способности для выбранной схемы
     */
    public static double calculateIncreasePercent(StrengtheningScheme scheme) {
        return scheme.getIncreasePercent();
    }

    /**
     * Получить рекомендацию по выбору схемы усиления
     * на основе требуемого процента увеличения
     */
    public static StrengtheningScheme recommendScheme(double requiredIncreasePercent) {
        StrengtheningScheme recommended = null;
        double minDiff = Double.MAX_VALUE;

        for (StrengtheningScheme scheme : StrengtheningScheme.values()) {
            double diff = Math.abs(scheme.getIncreasePercent() - requiredIncreasePercent);
            if (diff < minDiff) {
                minDiff = diff;
                recommended = scheme;
            }
        }

        return recommended;
    }

    /**
     * Вывести подробную информацию о схеме усиления
     */
    public static void printSchemeDetails(StrengtheningScheme scheme) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║              РЕКОМЕНДАЦИИ ПО УСИЛЕНИЮ (Раздел 14)            ║");
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Схема усиления:                                              ║");
        System.out.printf("║   %s%n", scheme.getDescription());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.printf("║ Увеличение несущей способности: %5.0f%%%n", scheme.getIncreasePercent());
        System.out.println("╠══════════════════════════════════════════════════════════════╣");
        System.out.println("║ Примечания:                                                  ║");
        System.out.println("║ • Усиление выполняется согласно Руководству по усилению      ║");
        System.out.println("║   железобетонных пролетных строений железнодорожных мостов   ║");
        System.out.println("║   системой внешнего армирования на основе углеродных волокон ║");
        System.out.println("║ • Материал усиления наклеивают на нижнюю грань балки         ║");
        System.out.println("║ • Закрепление от отслоения - вертикальными или наклонными    ║");
        System.out.println("║   обоймами                                                    ║");
        System.out.println("╚══════════════════════════════════════════════════════════════╝");
    }

    /**
     * Сравнить несколько схем усиления
     */
    public static void compareSchemes(StrengtheningScheme... schemes) {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║            СРАВНЕНИЕ СХЕМ УСИЛЕНИЯ (Таблица 14.1)            ║");
        System.out.println("╠══════╦═══════════════════════════════════════════╦═══════════╣");
        System.out.println("║  №   ║           Описание конструкции            ║ Увеличение║");
        System.out.println("║      ║                  усиления                  несущей   ║");
        System.out.println("║      ║                                           ║ способн.  ║");
        System.out.println("╠══════╬═══════════════════════════════════════════╬═══════════╣");

        for (StrengtheningScheme scheme : schemes) {
            System.out.printf("║  %-2d  ║ %-43s ║ %6.0f%%    ║%n",
                scheme.ordinal() + 1, scheme.getDescription(), scheme.getIncreasePercent());
        }
        System.out.println("╚══════╩═══════════════════════════════════════════╩═══════════╝");
    }
}