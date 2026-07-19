package org.example.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record MaterialRequest(
        @NotNull(message = "Прочность бетона не может быть пустой")
        @Positive(message = "Прочность бетона должна быть больше 0")
        Double concreteStrengthR,

        @NotNull(message = "Тип арматуры не может быть пустым")
        Integer rebarType // 1 - гладкая, 2 - периодическая
) {}