package org.example.dto.slab;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;

public record SlabShearRequest(
        @NotNull @Valid BridgeCommonData commonData,
        MaterialsData materials,
        LoadsData loads,

        // Геометрия из раздела 7.2
        Double slabHeight, Double asTensile, Double asCompressive,
        Double asTensileArea, Double asCompressiveArea,
        Double lp, Double B, Double ls, Double lbPrime, Double lbDoubleprime,
        Double hbPrime, Double hbDoubleprime,
        Double mpMonolithic, Double mpExternalCantilever,

        // Специфичные для поперечной силы
        @NotNull Double P0,
        @NotNull Double pt,
        @NotNull Double lt,
        @NotNull Double lk
) {}