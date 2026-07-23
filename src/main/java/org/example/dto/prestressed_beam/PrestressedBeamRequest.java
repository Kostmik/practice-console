package org.example.dto.prestressed_beam;

import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;

public record PrestressedBeamRequest(
    @NotNull BridgeCommonData commonData,

    // Прочностные характеристики материалов (Раздел 5)
    Double Rb,
    Double Rs,
    Double Rsc,
    Double nPrime,

    // Геометрия балки
    Double beamHeight,
    Double beamWidth,
    Double bf,
    Double hf,

    // Ненапрягаемая арматура
    Double As_tensile,
    Double As_compressive,
    Double as_tensile,
    Double as_compressive,

    // Напрягаемая арматура (Раздел 9)
    @NotNull Double Rp,
    @NotNull Double Rpc,
    @NotNull Double sigmaP2,
    @NotNull Double sigmaP2s,
    @NotNull Double Ap,
    @NotNull Double Ap_s,
    @NotNull Double ap,
    @NotNull Double ap_s,

    // Приведенные характеристики сечения
    @NotNull Double Ared,
    @NotNull Double Ired,
    @NotNull Double Theta,

    // Нагрузки и коэффициенты (Раздел 6)
    Double ppBeam,
    Double pbBeam,
    Double epsilonM,
    Double epsilonQ,

    // Параметры для поперечной силы (Формула 9.7)
    Double sumApi,
    Double sinAlpha,
    Double Asw,
    Double c,
    Double s,
    Double Qb,

    // Параметры для расчета выносливости (Формула 9.14)
    Double hc
) {}