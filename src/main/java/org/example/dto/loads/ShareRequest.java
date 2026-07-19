package org.example.dto.loads;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;

public record ShareRequest(
        @NotNull @Valid
        BridgeCommonData commonData,

        @NotNull
        Boolean isMonolithic,           // true - монолитное, false - сборное

        @NotNull
        Double xRatio                   // Относительная координата сечения x/l
) {}