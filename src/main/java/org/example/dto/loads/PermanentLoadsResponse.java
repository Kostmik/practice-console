package org.example.dto.loads;

public record PermanentLoadsResponse(
        Double gammaReinforcedConcrete,     // Удельный вес ж/б, кН/м³
        Double gammaBallastWithTrack,       // Удельный вес балласта, кН/м³

        Double ppSlab,                      // Нагрузка от веса плиты, кН/м
        Double pbSlab,                      // Нагрузка от веса балласта на плиту, кН/м

        Double ppBeam,                      // Нагрузка от веса ПС на балку, кН/м
        Double pbBeam,                      // Нагрузка от веса балласта на балку, кН/м

        Double np,                          // Коэффициент надёжности для ж/б
        Double npPrime,                     // Коэффициент надёжности для балласта
        Double nk,                          // Коэффициент надёжности для временной нагрузки
        String detailedReport
) {}