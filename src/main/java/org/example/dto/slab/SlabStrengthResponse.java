package org.example.dto.slab;

public record SlabStrengthResponse(
        Double l0,                      // Длина распределения временной нагрузки, м

        Double kMonolithic,             // Допускаемая нагрузка для монолитного участка, кН/м
        Double KMonolithic,             // Класс монолитного участка

        Double kExternalCantilever,     // Допускаемая нагрузка для внешней консоли, кН/м
        Double KExternalCantilever,     // Класс внешней консоли

        Double minClass,                // Минимальный класс плиты

        String detailedReport           // Полный текстовый отчёт
) {}