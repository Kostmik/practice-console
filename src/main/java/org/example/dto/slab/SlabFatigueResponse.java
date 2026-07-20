package org.example.dto.slab;

public record SlabFatigueResponse(
        Double kFatigueConcrete,   // Допускаемая нагрузка по выносливости бетона, кН/м
        Double KFatigueConcrete,   // Класс по выносливости бетона
        Double kFatigueRebar,      // Допускаемая нагрузка по выносливости арматуры, кН/м
        Double KFatigueRebar,      // Класс по выносливости арматуры
        Double minClass,           // Минимальный класс по выносливости
        String detailedReport      // Полный текстовый отчёт
) {}