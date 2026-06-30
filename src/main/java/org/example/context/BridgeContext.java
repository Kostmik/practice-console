package org.example.context;

import org.example.model.TrackType;

public class BridgeContext {
    // Исходные данные
    public double spanLength;                    // Расчетный пролет l, м
    public double ballastThickness;              // Толщина балласта hb, м
    public TrackType trackType;                  // Тип пути
    public double concreteStrengthR;             // Фактическая прочность бетона R, МПа

    // Удельные веса
    public double gammaReinforcedConcrete = 24.52;  // кН/м³
    public double gammaBallastWithTrack;            // кН/м³

    // Коэффициенты надежности
    public double np = 1.1;      // для веса ж/б
    public double npPrime = 1.2; // для веса балласта
    public double nk = 1.15;     // для временной нагрузки

    // Результаты расчетов
    public Double dynamicCoeff;  // Динамический коэффициент (1+μ)

    // Постоянные нагрузки для плиты
    public Double ppSlab;  // Нагрузка от веса плиты, кН/м
    public Double pbSlab;  // Нагрузка от веса балласта, кН/м

    // Постоянные нагрузки для главной балки
    public Double ppBeam;  // Нагрузка от веса ПС, кН/м
    public Double pbBeam;  // Нагрузка от веса балласта, кН/м
}