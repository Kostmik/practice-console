package org.example.dto.loads;

public record DynamicCoeffResponse(
        Double dynamicCoeff,            // Коэффициент (1+μ)
        Double mu0,                     // Базовый коэффициент
        Double lambda                   // Длина загружения
) {}