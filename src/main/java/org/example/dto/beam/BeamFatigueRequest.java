package org.example.dto.beam;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.slab.MaterialsData;

public record BeamFatigueRequest(
        @NotNull @Valid BridgeCommonData commonData,
        MaterialsData materials,

        // Нагрузки из раздела 6
        Double ppBeam, Double pbBeam,
        Double np, Double npPrime, Double nk,

        // Доли и динамика
        Double epsilonM,
        Double dynamicCoeffBeam,

        // Геометрия балки
        @NotNull Double beamHeight,
        @NotNull Double beamWidth,
        @NotNull Double bf,
        @NotNull Double hf,
        @NotNull Double asBeamTensile,
        @NotNull Double asBeamCompressive,
        @NotNull Double asBeamTensileArea,
        @NotNull Double asBeamCompressiveArea
) {}