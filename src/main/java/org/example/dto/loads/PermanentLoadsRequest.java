package org.example.dto.loads;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.dto.common.BridgeCommonData;

public record PermanentLoadsRequest(
        @NotNull @Valid
        BridgeCommonData commonData,

        @NotNull @Positive
        Double hSlab,                   // Толщина плиты, м

        @NotNull @Positive
        Double vConcrete,               // Объём ж/б пролётного строения, м³

        Double pDevices,                // Вес обустройств, кН (может быть 0)

        @NotNull @Positive
        Double sBallast,                // Площадь поперечного сечения балластной призмы, м²

        @NotNull @Positive
        Integer mBeams                  // Количество главных балок
) {}
