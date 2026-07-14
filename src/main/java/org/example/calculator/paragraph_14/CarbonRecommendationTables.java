package org.example.calculator.paragraph_14;

/**
 * Таблица 14.1 - Возможные схемы усиления
 */
public class CarbonRecommendationTables {

    public enum StrengtheningScheme {
        SHEET_BOTTOM("Холст на нижней грани", 3),
        SHEET_BOTTOM_GENTLE_STRAPS("То же, но с устройством пологих обойм", 5),
        SHEET_BOTTOM_VERTICAL_STRAPS("То же, но с устройством вертикальных обойм", 7),
        SHEET_BOTTOM_TO_SUPPORTS("Холст на нижней грани \"до опор\"", 7),
        U_JACKET("U-образная обойма", 26),
        LAMELLE_BOTTOM_VERTICAL_STRAPS("Ламель на нижней грани с устройством вертикальных обойм", 11),
        LAMELLE_BOTTOM_ADDITIONAL_STRAPS("То же, но с устройством дополнительных обойм", 22);

        private final String description;
        private final double increasePercent;

        StrengtheningScheme(String description, double increasePercent) {
            this.description = description;
            this.increasePercent = increasePercent;
        }

        public String getDescription() {
            return description;
        }

        public double getIncreasePercent() {
            return increasePercent;
        }
    }

    /**
     * Получить схему усиления по номеру (1-7)
     */
    public static StrengtheningScheme getScheme(int number) {
        if (number < 1 || number > StrengtheningScheme.values().length) {
            throw new IllegalArgumentException("Неверный номер схемы: " + number);
        }
        return StrengtheningScheme.values()[number - 1];
    }

    /**
     * Вывести все доступные схемы усиления
     */
    public static void printAllSchemes() {
        System.out.println("\n╔══════════════════════════════════════════════════════════════╗");
        System.out.println("║           ТАБЛИЦА 14.1 - ВОЗМОЖНЫЕ СХЕМЫ УСИЛЕНИЯ            ║");
        System.out.println("╠══════╦═══════════════════════════════════════════╦═══════════╣");
        System.out.println("║  №   ║           Описание конструкции            ║ Увеличение║");
        System.out.println("║      ║                  усиления                 ║ несущей   ║");
        System.out.println("║      ║                                           ║ способн.  ║");
        System.out.println("══════╬═══════════════════════════════════════════╬═══════════╣");

        StrengtheningScheme[] schemes = StrengtheningScheme.values();
        for (int i = 0; i < schemes.length; i++) {
            System.out.printf("║  %-2d  ║ %-43s ║ %6.0f%%    ║%n",
                i + 1, schemes[i].getDescription(), schemes[i].getIncreasePercent());
        }
        System.out.println("╚══════╩═══════════════════════════════════════════╩═══════════╝");
    }
}