package org.example.calculator.paragraph_14;

/**
 * Таблица 14.1 - Возможные схемы усиления
 */
public class CarbonRecsTables {

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
}