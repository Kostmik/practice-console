package org.example.dto.beam;

public record BeamFatigueResponse(
        Double kFatigueConcrete,
        Double KFatigueConcrete,
        Double kFatigueRebar,
        Double KFatigueRebar,
        Double minClass,
        String detailedReport
) {}