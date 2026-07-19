package org.example.dto.common;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record BridgeCommonData(
        @NotNull @Positive
        Double spanLength,              // Расчётный пролёт l, м

        @NotNull @Positive
        Double ballastThickness,        // Толщина балласта hb, м

        @NotNull
        Integer trackType,              // 1 - звеньевой, 2 - бесстыковой

        @NotNull
        Integer sleeperType,            // 1 - ж/б, 2 - деревянные

        @NotNull @Positive
        Double concreteStrengthR,       // Прочность бетона R, МПа

        @NotNull @Positive
        Double distanceBetweenBeams,    // Расстояние между осями балок c, м

        Double trackOffsetLeft,         // Смещение пути у левой опоры e1, м
        Double trackOffsetRight,        // Смещение пути у правой опоры e2, м

        // --- НОВЫЕ ПОЛЯ ---
        @NotNull @Positive
        Integer mBeams,                 // Количество главных балок (обычно 2)

        @NotNull @Min(1) @Max(2)
        Integer rebarType,              // 1 - Гладкая (А240), 2 - Периодическая (А400)

        @NotNull @Positive
        Integer designYear,             // Год выпуска норм проектирования (например, 1931)

        @NotNull
        String loadType                 // Тип нагрузки ("Н7" или "Н8")
) {}