package org.example.dto.paragraph9;

public record Section9Response(
    Double k,
    Double xiY,
    Double sigmaPc,
    Double x,
    Double M,
    Double Mp,
    Double Omega,
    Double Mk,
    Double Q_pred,
    Double Rpf,               // <-- 10. Сопротивление выносливости (было Rsf, исправлено на Rpf)
    Double rho,
    Double sigmaRebarMin,
    Double sigmaRebarMax,
    Double sigmaConcreteMin,
    Double sigmaConcreteMax,
    Double xPrime,
    String detailedReport     // <-- 17. Строго String!
) {}