package org.example.calculator.paragraph_15;

/**
 * Результаты расчётов Раздела 15.
 */
public class InspectionOutput {

    // 15.3
    public double trackOffset;           // l — смещение оси пути, м (ф. 15.1)

    // 15.4
    public double avgConcreteStrength;   // R̄ — средняя прочность бетона, МПа (ф. 15.2)
    public int numberOfMeasurements;     // n — число измерений

    // 15.5
    public double epsilonM;              // ε_M — доля временной нагрузки на балку (ф. 15.3)
    public double sumFiIi;               // Σ(fᵢ·Iᵢ) — знаменатель формулы
    public double fTargetITarget;        // f·I — числитель для целевой балки
}