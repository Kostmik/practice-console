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

    // === ХАРАКТЕРИСТИКИ МАТЕРИАЛОВ (Раздел 5) ===
    public double Rb;          // Расчетное сопротивление бетона сжатию, МПа
    public double Rbt;         // Расчетное сопротивление бетона растяжению, МПа
    public double Eb;          // Модуль упругости бетона, МПа
    public double nPrime;      // Условное отношение модулей упругости арматуры и бетона (n')

    public double Rs;          // Расчетное сопротивление арматуры растяжению, МПа
    public double Es = 206000; // Модуль упругости арматуры, МПа (2.06 * 10^5)
    public String rebarType;   // Тип арматуры (для вывода)

    // Результаты расчетов (Раздел 6)
    public Double dynamicCoeff;
    public Double ppSlab;
    public Double pbSlab;
    public Double ppBeam;
    public Double pbBeam;
}