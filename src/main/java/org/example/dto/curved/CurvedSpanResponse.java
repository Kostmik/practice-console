package org.example.dto.curved;

/**
 * Результат расчёта пролётного строения на кривой (Раздел 11).
 * Определяющие доли epsMDesign / epsQDesign подставляются в расчёт балки
 * вместо долей, полученных для прямого участка по п. 6.6–6.7.
 */
public record CurvedSpanResponse(

        // === Угол возвышения наружного рельса (ф. 11.14) ===
        Double sinAlpha,
        Double cosAlpha,

        // === Геометрия и моменты инерции сечения ===
        Double c1,                  // ф. 11.9
        Double bigH,                // H, ф. 11.9
        Double iyPrime10,           // I_y′ по ф. 11.10 (6 слагаемых), м⁴
        Double iyPrime7,            // I_y′ по ф. 11.7 (4 слагаемых), м⁴
        Double ik,                  // I_к — момент инерции при кручении, м⁴ (ф. 11.5)
        Double iOmega,              // I_ω — секториальный момент инерции (ф. 11.12)
        Double d,                   // ф. 11.8
        Double omega,               // ω, ф. 11.6
        Double omegaD,              // ω_D, ф. 11.13
        Double omegaK,              // ω_κ, ф. 11.13
        Double y,                   // ф. 11.13
        Double m,                   // ф. 11.11
        Double g,                   // g = 0,5·m·l
        Double gamma,               // коэффициент γ по табл. 11.1

        // === Смещение вертикальной нагрузки (ф. 11.2–11.4) ===
        Double dlMMoving,           // Δl_M, поезд движется (11.2)
        Double dlQMoving,           // Δl_Q, поезд движется (11.3)
        Double dlStanding,          // Δl_M = Δl_Q, поезд стоит (11.4)

        // === Доли нагрузки ε (ф. 11.1), поезд движется ===
        Double epsMBeam1,
        Double epsQBeam1,
        Double epsMBeam2,
        Double epsQBeam2,

        // === Доли нагрузки ε (ф. 11.1), поезд стоит ===
        Double epsMBeam1Standing,
        Double epsQBeam1Standing,
        Double epsMBeam2Standing,
        Double epsQBeam2Standing,

        // === Определяющие доли на наиболее загруженную балку ===
        Double epsMDesign,
        Double epsQDesign,

        String detailedReport
) {}