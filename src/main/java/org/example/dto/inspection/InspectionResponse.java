package org.example.dto.inspection;

import java.util.List;

public record InspectionResponse(
    Double trackOffset,
    Double avgConcreteStrength,
    Integer numberOfMeasurements,
    Double epsilonM,
    Double sumFiIi,
    Double fTargetITarget,
    Integer targetBeamIndex,
    Integer totalBeams,
    List<BeamDeflectionRow> beamRows,  // данные для таблицы
    String detailedReport
) {
    public record BeamDeflectionRow(
        int beamNumber,      // номер балки (1-based)
        double deflection,   // fᵢ, мм
        double inertia,      // Iᵢ, м
        double product,      // f · Iᵢ
        boolean isTarget     // является ли целевой
    ) {}
}