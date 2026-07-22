package org.example.dto.paragraph8;

public record Section8Response(
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