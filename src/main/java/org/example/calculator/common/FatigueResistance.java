package org.example.calculator.common;

import org.example.util.Interpolation;

/**
 * Расчётные сопротивления материалов при расчёте элементов на выносливость (Раздел 5).
 * Вынесено отдельно, так как эти величины нужны в §7.3, §9.3, §10.3, §13.3.
 *
 * <ul>
 *   <li>{@link #concrete} — ф. 5.1: R_bf = 0,6·β_b·R_b;</li>
 *   <li>{@link #rebar}    — ф. 5.2: R_sf = ε_ρ·R_s.</li>
 * </ul>
 *
 * ПРИМЕЧАНИЕ: коэффициент ε_ρ по таблице 5.3 приведён для строки ненапрягаемой стержневой
 * арматуры (гладкая / периодического профиля). Если в расчёте используется другой вид
 * арматуры — значения коэффициента следует уточнить по таблице 5.3 Руководства.
 */
public final class FatigueResistance {

    private FatigueResistance() {
    }

    // --- Коэффициент β_b для бетона по асимметрии цикла ρ_b (таблица в п. 5.1) ---
    private static final double[] RHO_B = {0.1, 0.2, 0.3, 0.4, 0.5, 0.6};
    private static final double[] BETA_B = {1.00, 1.06, 1.10, 1.15, 1.20, 1.24};

    // --- Коэффициент ε_ρ для ненапрягаемой арматуры по ρ (таблица 5.3) ---
    private static final double[] RHO_S = {0.0, 0.1, 0.2, 0.3, 0.35, 0.4, 0.5,
            0.6, 0.7, 0.75, 0.8, 0.85, 0.9, 1.0};
    private static final double[] EPS_RHO = {0.81, 0.85, 0.89, 0.97, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0};

    /**
     * Коэффициент β_b, зависящий от асимметрии цикла напряжений в бетоне ρ_b (п. 5.1).
     */
    public static double concreteAsymmetryCoeff(double rhoB) {
        if (rhoB <= RHO_B[0]) return BETA_B[0];
        if (rhoB >= RHO_B[RHO_B.length - 1]) return BETA_B[BETA_B.length - 1];
        return Interpolation.linear(RHO_B, BETA_B, rhoB);
    }

    /**
     * Расчётное сопротивление бетона на выносливость (ф. 5.1): R_bf = 0,6·β_b·R_b.
     *
     * @param Rb   расчётное сопротивление бетона сжатию (прочность), в тех же единицах, что и результат
     * @param rhoB асимметрия цикла напряжений в бетоне
     * @return R_bf в тех же единицах, что и Rb
     */
    public static double concrete(double Rb, double rhoB) {
        return 0.6 * concreteAsymmetryCoeff(rhoB) * Rb;
    }

    /**
     * Коэффициент ε_ρ, зависящий от асимметрии цикла напряжений в арматуре ρ (таблица 5.3).
     */
    public static double rebarAsymmetryCoeff(double rho) {
        if (rho <= RHO_S[0]) return EPS_RHO[0];
        if (rho >= RHO_S[RHO_S.length - 1]) return EPS_RHO[EPS_RHO.length - 1];
        return Interpolation.linear(RHO_S, EPS_RHO, rho);
    }

    /**
     * Расчётное сопротивление арматуры на выносливость (ф. 5.2): R_sf = ε_ρ·R_s.
     *
     * @param Rs  расчётное сопротивление арматуры (прочность), в тех же единицах, что и результат
     * @param rho асимметрия цикла напряжений в арматуре (п. 5.2.2)
     * @return R_sf в тех же единицах, что и Rs
     */
    public static double rebar(double Rs, double rho) {
        return rebarAsymmetryCoeff(rho) * Rs;
    }
}