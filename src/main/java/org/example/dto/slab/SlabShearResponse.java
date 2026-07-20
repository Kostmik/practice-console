package org.example.dto.slab;

public record SlabShearResponse(
        Double kShearMonolithic,     // Допускаемая нагрузка для монолитного участка, кН/м
        Double KShearMonolithic,     // Класс монолитного участка
        Double kShearExternal,       // Допускаемая нагрузка для внешней консоли, кН/м
        Double KShearExternal,       // Класс внешней консоли
        Double minClass,             // Минимальный класс по поперечной силе
        String detailedReport        // Полный текстовый отчёт
) {}