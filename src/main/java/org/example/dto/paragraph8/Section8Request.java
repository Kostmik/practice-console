package org.example.dto.paragraph8;

import jakarta.validation.constraints.NotNull;
import org.example.dto.common.BridgeCommonData;

public record Section8Request(
    @NotNull BridgeCommonData commonData, // <-- Паспортные данные приходят здесь

    // Геометрия и параметры
    Double slabHeight,
    Double ls,
    Double B,
    Double lt,
    Double lk,
    Double P0,
    Double j,
    Double alpha,

    // 8.1 Внешняя консоль
    Double l0Cantilever,
    Double etaMCantilever,
    Double delta,
    Double Z,
    Double mpCantilever,

    // 8.2 Монолитный участок
    Double l0Monolithic,
    Double etaMMonolithic,

    // 8.4 Главная балка
    Double epsilon,
    Double pp,
    Double pb
) {}