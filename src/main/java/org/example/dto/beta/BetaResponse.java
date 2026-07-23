package org.example.dto.beta;

public record BetaResponse(
    Double kCantilever,
    Double kMonolithic,
    Double kBeam,
    Double beta,
    String detailedReport,
    // Дополнительные поля для отображения на фронтенде
    Double ppUsed,
    Double pbUsed,
    Double epsilonUsed,
    Integer mUsed
) {}