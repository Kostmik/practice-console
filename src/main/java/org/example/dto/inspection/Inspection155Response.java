package org.example.dto.inspection;

public record Inspection155Response(
        int totalBeams,
        int targetBeamIndex,
        double fTargetITarget,
        double sumFiIi,
        double epsilonM,
        String detailedReport
) {}
