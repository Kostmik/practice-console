package org.example.dto.beam;

public record BeamShearResponse(
        Double qUltimate,
        Double qpShear,
        Double kBeamShear,
        Double KBeamShear,
        String detailedReport
) {}