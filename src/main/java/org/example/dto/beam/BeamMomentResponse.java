package org.example.dto.beam;

public record BeamMomentResponse(
        Double mPredBeam,
        Double mpBeam,
        Double kBeamMoment,
        Double kcBeam,
        Double KBeamMoment,
        String detailedReport
) {}