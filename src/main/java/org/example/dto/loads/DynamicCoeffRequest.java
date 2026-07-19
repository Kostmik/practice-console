package org.example.dto.loads;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;

public record DynamicCoeffRequest(
        @NotNull @Valid
        BridgeCommonData commonData,

        @NotNull
        String elementName,             // "ГЛАВНАЯ БАЛКА" или "ПЛИТА БАЛЛАСТНОГО КОРЫТА"

        Boolean useMaxCoefficient,      // true - максимальное значение (для плиты)
        Double lambda                   // Длина загружения (для плиты, если useMaxCoefficient=false)
) {}