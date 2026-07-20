package org.example.dto.inspection;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

public record InspectionRequest(
    // 15.3 - Смещение оси пути
    @NotNull @Positive Double aPrime,
    @NotNull @Positive Double bPrime,
    @NotNull @Positive Double b0Prime,

    // 15.4 - Прочность бетона
    @NotNull List<Double> concreteStrengths,

    // 15.5 - Испытание пролётных строений
    @NotNull List<Double> deflections,    // прогибы f для ВСЕХ балок (мм)
    @NotNull List<Double> inertias,       // моменты инерции Iᵢ для ВСЕХ балок (м⁴)
    @NotNull Integer targetBeamIndex      // индекс целевой балки (0-based)
) {}