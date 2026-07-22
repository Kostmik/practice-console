package org.example.dto.curved;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.slab.MaterialsData;

/**
 * Запрос на расчёт доли временной нагрузки на главную балку пролётного строения,
 * расположенного на кривой (Раздел 11).
 *
 * <p>Расчётный пролёт l, расстояние между осями балок c и толщина балласта h_b берутся
 * из паспорта объекта. Модули упругости E_s и E_b — из результата Раздела 5,
 * динамический коэффициент μ₀ — из Раздела 6.4.
 */
public record CurvedSpanRequest(

        @NotNull @Valid
        BridgeCommonData commonData,

        // === Материалы (Раздел 5) ===
        MaterialsData materials,

        // === Динамика (Раздел 6.4) ===
        Double mu0,                 // базовый динамический коэффициент μ₀ (входит как 1+μ₀)

        // === Геометрия поперечного сечения ===
        @NotNull @Positive
        Double b,                   // ширина ребра (стенки) балки, м
        @NotNull @Positive
        Double h,                   // полная высота балки, м
        @NotNull @Positive
        Double h1,                  // средняя толщина плиты между рёбрами, м
        @NotNull @Positive
        Double h2,                  // средняя толщина плиты консоли, м
        @NotNull
        Double lk,                  // геометрический параметр (c1 = l_k + 0,5·b), м
        @NotNull @Positive
        Double asDist,              // расстояние до ц.т. растянутой арматуры a_s, м
        @NotNull @Positive
        Double asArea,              // площадь сечения растянутой арматуры A_s, м²

        // === Верхнее строение пути ===
        @NotNull @Positive
        Double hp,                  // высота рельса, м
        @NotNull @Positive
        Double hs,                  // высота шпалы, м
        @NotNull @Positive
        Double ls,                  // длина шпалы, м
        Double ht,                  // высота приложения нагрузки, м (рис. 11, обычно 2.2)

        // === Кривая ===
        @NotNull @Positive
        Double curveRadius,         // радиус кривой R, м
        @NotNull @Positive
        Double speed,               // наибольшая скорость движения поездов v, км/ч
        @NotNull
        Double cantElevation,       // возвышение наружного рельса Δh, м
        @NotNull @Positive
        Double b0,                  // расстояние между осями головок рельсов, м

        // === Смещение оси пути ===
        @NotNull
        Double lPrime,              // смещение оси пути l' (участок 0,25l…0,75l), м
        @NotNull
        Double lDoublePrime,        // смещение оси пути l'' (участки у опор), м

        // === Прочее ===
        Double theta                // коэффициент Θ (приложение), по умолчанию 1.0
) {}