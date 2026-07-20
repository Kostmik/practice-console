package org.example.dto.slab;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.dto.common.BridgeCommonData;

public record SlabStrengthRequest(
        @NotNull @Valid
        BridgeCommonData commonData,

        MaterialsData materials,      // Уже рассчитанные материалы (может быть null)
        LoadsData loads,              // Уже рассчитанные нагрузки (может быть null)

        @NotNull @Positive
        Double slabHeight,              // Высота плиты h, м

        @NotNull @Positive
        Double asTensile,               // Расстояние до растянутой арматуры as, м

        @NotNull @Positive
        Double asCompressive,           // Расстояние до сжатой арматуры as', м

        @NotNull @Positive
        Double asTensileArea,           // Площадь растянутой арматуры As, м²

        Double asCompressiveArea,       // Площадь сжатой арматуры As', м²

        @NotNull @Positive
        Double lp,                      // Расстояние между внутренними гранями рёбер lp, м

        @NotNull @Positive
        Double B,                       // Расстояние между наружными гранями рёбер B, м

        @NotNull @Positive
        Double ls,                      // Длина шпалы ls, м

        @NotNull @Positive
        Double lbPrime,                 // Расстояние от ребра до левого борта l'b, м

        @NotNull @Positive
        Double lbDoubleprime,           // Расстояние от ребра до правого борта l''b, м

        @NotNull @Positive
        Double hbPrime,                 // Толщина балласта под левым концом шпалы h'b, м

        @NotNull @Positive
        Double hbDoubleprime,           // Толщина балласта под правым концом шпалы h''b, м

        Double mpMonolithic,            // Момент от пост. нагрузок в монолитном участке (0 для авто-расчёта)

        @NotNull
        Double mpExternalCantilever     // Момент от пост. нагрузок во внешней консоли, кН·м
) {}