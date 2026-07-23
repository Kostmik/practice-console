package org.example.controller;

import org.example.calculator.paragraph_8.BeamCalculator;
import org.example.calculator.paragraph_8.BetaCalculator;
import org.example.calculator.paragraph_8.SlabCantileverCalculator;
import org.example.calculator.paragraph_8.SlabMonolithicCalculator;
import org.example.context.BridgeContext;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.beta.BetaRequest;
import org.example.dto.beta.BetaResponse;
import org.example.model.RebarType;
import org.example.model.TrackType;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/beta")
@CrossOrigin(origins = "*")
public class BetaController {

    @PostMapping("/calculate")
    public BetaResponse calculate(@RequestBody BetaRequest request) {
        BridgeCommonData common = request.commonData();

        // 1. Собираем BridgeContext из доступных полей
        BridgeContext ctx = new BridgeContext();
        ctx.spanLength = common.spanLength();
        ctx.ballastThickness = common.ballastThickness();
        ctx.slabHeight = request.slabHeight() != null ? request.slabHeight() : 0.26;
        ctx.ls = request.ls() != null ? request.ls() : 2.7;
        ctx.B = request.B() != null ? request.B() : 2.4;
        ctx.lt = request.lt() != null ? request.lt() : 1.25;
        ctx.lk = request.lk() != null ? request.lk() : 1.25;
        ctx.P0 = request.P0() != null ? request.P0() : 0.7;
        ctx.j = request.j() != null ? request.j() : 1.0;
        ctx.designYear = common.designYear();

        // Безопасный маппинг Enum (1 = Звеньевой, 2 = Бесстыковой)
        try {
            ctx.trackType = TrackType.values()[common.trackType() - 1];
        } catch (Exception e) {
            ctx.trackType = TrackType.values()[0];
        }

        // Маппинг типа арматуры (1 = Гладкая, 2 = Периодическая)
        RebarType rebarType = (common.rebarType() == 1) ? RebarType.SMOOTH : RebarType.RIBBED;

        // ================================================================
        // 2. ИСПОЛЬЗУЕМ ЗНАЧЕНИЯ ИЗ РАЗДЕЛА 6 (если они уже рассчитаны)
        // ================================================================

        // 2.1. Доля временной нагрузки на балку εM
        // Приоритет: 1) из запроса, 2) из BridgeContext (Раздел 6), 3) значение по умолчанию
        Double epsilon = request.epsilon();
        if (epsilon == null && ctx.epsilonM_Beam1 != null) {
            epsilon = ctx.epsilonM_Beam1;
        }
        if (epsilon == null) {
            epsilon = 0.565; // значение по умолчанию для двухблочных ПС
        }

        // 2.2. Вес пролетного строения на балку pp
        Double pp = request.pp();
        if (pp == null && ctx.ppBeam != null) {
            pp = ctx.ppBeam;
        }
        if (pp == null) {
            pp = 34.0; // значение по умолчанию
        }

        // 2.3. Вес балласта на балку pb
        Double pb = request.pb();
        if (pb == null && ctx.pbBeam != null) {
            pb = ctx.pbBeam;
        }
        if (pb == null) {
            pb = 20.6; // значение по умолчанию
        }

        // 2.4. Количество главных балок m
        Integer m = common.mBeams();
        if (m == null) {
            m = 2; // значение по умолчанию
        }

        // 2.5. Постоянная нагрузка на консоль плиты p1
        // (используется в SlabCantileverCalculator)
        // ctx.ppSlab и ctx.pbSlab заполняются в Разделе 6

        // ================================================================
        // 3. Перехват консоли для отчёта
        // ================================================================

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

        try {
            Double kCantilever = null;
            Double kMonolithic = null;
            Double kBeam = null;
            double alpha = request.alpha() != null ? request.alpha() : 0.5;

            // === Расчет 8.1 (Консоль) ===
            if (request.l0Cantilever() != null && request.etaMCantilever() != null && request.delta() != null) {
                kCantilever = SlabCantileverCalculator.calculate(
                    ctx, common.designYear(), request.l0Cantilever(), request.etaMCantilever(),
                    rebarType, ctx.j, common.loadType(), alpha,
                    request.delta(), request.Z() != null ? request.Z() : 0.0,
                    ctx.lk, ctx.P0, ctx.lt,
                    request.mpCantilever() != null ? request.mpCantilever() : 0.0
                );
                SlabCantileverCalculator.printReport(
                    ctx, common.designYear(), request.l0Cantilever(), request.etaMCantilever(),
                    rebarType, ctx.j, common.loadType(), alpha,
                    request.delta(), request.Z() != null ? request.Z() : 0.0,
                    ctx.lk, ctx.P0, ctx.lt,
                    request.mpCantilever() != null ? request.mpCantilever() : 0.0, kCantilever
                );
            }

            // === Расчет 8.2 (Монолитный участок) ===
            if (request.l0Monolithic() != null && request.etaMMonolithic() != null) {
                kMonolithic = SlabMonolithicCalculator.calculate(
                    ctx, common.designYear(), request.l0Monolithic(), request.etaMMonolithic(),
                    rebarType, ctx.j, common.loadType(), alpha
                );
                SlabMonolithicCalculator.printReport(
                    ctx, common.designYear(), request.l0Monolithic(), request.etaMMonolithic(),
                    rebarType, ctx.j, common.loadType(), alpha, kMonolithic
                );
            }

            // === Расчет 8.4 (Главная балка) ===
            // Используем значения pp, pb, epsilon, m из Раздела 6 (если есть)
            kBeam = BeamCalculator.calculate(
                ctx, common.designYear(), rebarType, ctx.j, common.loadType(),
                alpha, epsilon, m, pp, pb
            );
            BeamCalculator.printReport(
                ctx, common.designYear(), rebarType, ctx.j, common.loadType(),
                alpha, epsilon, m, pp, pb, kBeam
            );

            // Вычисляем beta для отображения на фронтенде
            double beta = BetaCalculator.calculateBeta(common.designYear(), rebarType, ctx.j);

            // Сохраняем результаты в BridgeContext для использования в следующих разделах
            ctx.k_external_cantilever = kCantilever;
            ctx.k_monolithic = kMonolithic;
            ctx.k_beam_moment = kBeam;
            ctx.beta = beta;
            ctx.ppBeam = pp;
            ctx.pbBeam = pb;
            ctx.epsilonM_Beam1 = epsilon;

            return new BetaResponse(
                kCantilever,
                kMonolithic,
                kBeam,
                beta,
                baos.toString(StandardCharsets.UTF_8),
                // Дополнительные поля для отображения на фронтенде
                pp,
                pb,
                epsilon,
                m
            );

        } finally {
            System.setOut(oldOut);
        }
    }
}