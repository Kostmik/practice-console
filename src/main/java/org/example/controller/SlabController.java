package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_5.MaterialCalculator;
import org.example.calculator.paragraph_6.LoadsCalculator;
import org.example.calculator.paragraph_7.SlabCalculator;
import org.example.calculator.paragraph_7.SlabFatigueCalculator;
import org.example.calculator.paragraph_7.SlabShearCalculator;
import org.example.context.BridgeContext;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.slab.*;
import org.example.model.TrackType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/slab")
@CrossOrigin(origins = "*")
public class SlabController {

    // =====================================================================
    // 7.2 РАСЧЕТ ПЛИТЫ НА ПРОЧНОСТЬ
    // =====================================================================
    @PostMapping("/strength")
    public ResponseEntity<SlabStrengthResponse> calculateSlabStrength(
            @Valid @RequestBody SlabStrengthRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyHybridData(ctx, request.commonData(), request.materials(), request.loads());
        applySlabGeometry(ctx, request);

        String report = captureReport(() -> {
            SlabCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new SlabStrengthResponse(
                ctx.l0,
                ctx.k_monolithic,
                ctx.K_monolithic,
                ctx.k_external_cantilever,
                ctx.K_external_cantilever,
                Math.min(ctx.K_monolithic, ctx.K_external_cantilever),
                report
        ));
    }

    // =====================================================================
    // 7.2.4 РАСЧЕТ ПЛИТЫ ПО ПОПЕРЕЧНОЙ СИЛЕ
    // =====================================================================
    @PostMapping("/shear")
    public ResponseEntity<SlabShearResponse> calculateSlabShear(
            @Valid @RequestBody SlabShearRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyHybridData(ctx, request.commonData(), request.materials(), request.loads());
        applySlabGeometry(ctx, request);

        ctx.P0 = request.P0();
        ctx.pt = request.pt();
        ctx.lt = request.lt();
        ctx.lk = request.lk();

        String report = captureReport(() -> {
            SlabShearCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new SlabShearResponse(
                ctx.k_shear_monolithic,
                ctx.K_shear_monolithic,
                ctx.k_shear_external,
                ctx.K_shear_external,
                Math.min(ctx.K_shear_monolithic, ctx.K_shear_external),
                report
        ));
    }

    // =====================================================================
    // 7.3.1 РАСЧЕТ ПЛИТЫ НА ВЫНОСЛИВОСТЬ
    // =====================================================================
    @PostMapping("/fatigue")
    public ResponseEntity<SlabFatigueResponse> calculateSlabFatigue(
            @Valid @RequestBody SlabFatigueRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyHybridData(ctx, request.commonData(), request.materials(), request.loads());
        applySlabGeometry(ctx, request);

        // Запускаем расчет прочности "тихо", чтобы заполнить ctx.l0 и ctx.k_monolithic
        SlabCalculator.calculateAndPrintReport(ctx);

        String report = captureReport(() -> {
            SlabFatigueCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new SlabFatigueResponse(
                ctx.k_fatigue_slab_concrete,
                ctx.K_fatigue_slab_concrete,
                ctx.k_fatigue_slab_rebar,
                ctx.K_fatigue_slab_rebar,
                Math.min(ctx.K_fatigue_slab_concrete, ctx.K_fatigue_slab_rebar),
                report
        ));
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================
    private BridgeContext mapCommonData(BridgeCommonData common) {
        BridgeContext ctx = new BridgeContext();
        ctx.spanLength = common.spanLength();
        ctx.ballastThickness = common.ballastThickness();
        ctx.trackType = common.trackType() == 2 ? TrackType.CONTINUOUS : TrackType.LINKED;
        ctx.concreteStrengthR = common.concreteStrengthR();
        ctx.distanceBetweenBeams = common.distanceBetweenBeams();
        ctx.trackOffsetLeft = common.trackOffsetLeft() != null ? common.trackOffsetLeft() : 0.0;
        ctx.trackOffsetRight = common.trackOffsetRight() != null ? common.trackOffsetRight() : 0.0;
        return ctx;
    }

    private void applyHybridData(BridgeContext ctx, BridgeCommonData common,
                                 MaterialsData materials, LoadsData loads) {
        // 1. Материалы
        if (materials != null) {
            ctx.Rb = materials.Rb();
            ctx.Rbt = materials.Rbt();
            ctx.Eb = materials.Eb();
            ctx.Rs = materials.Rs();
            ctx.Es = materials.Es();
            ctx.nPrime = materials.nPrime();
        } else {
            MaterialCalculator.calculateAndReturnReport(ctx, common.rebarType());
        }

        // 2. Нагрузки
        boolean woodenSleepers = common.sleeperType() == 2;
        LoadsCalculator.setBallastDensity(ctx, woodenSleepers);

        if (loads != null) {
            ctx.gammaReinforcedConcrete = loads.gammaReinforcedConcrete();
            ctx.gammaBallastWithTrack = loads.gammaBallastWithTrack();
            ctx.ppSlab = loads.ppSlab();
            ctx.pbSlab = loads.pbSlab();
            ctx.np = loads.np();
            ctx.npPrime = loads.npPrime();
            ctx.nk = loads.nk();
            ctx.dynamicCoeffSlab = loads.dynamicCoeffSlab();
        } else {
            ctx.np = 1.1;
            ctx.npPrime = 1.2;
            ctx.nk = 1.15;
            ctx.pbSlab = common.ballastThickness() * ctx.gammaBallastWithTrack;
            ctx.dynamicCoeffSlab = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0);
        }
    }

    // === НОВЫЙ МЕТОД: Применение геометрии плиты ===
    // Использует рефлексию для универсальной работы с разными Request DTO
    private void applySlabGeometry(BridgeContext ctx, Object request) {
        try {
            setIfNotNull(ctx, request, "slabHeight", "slabHeight");
            setIfNotNull(ctx, request, "asTensile", "as_tensile");
            setIfNotNull(ctx, request, "asCompressive", "as_compressive");
            setIfNotNull(ctx, request, "asTensileArea", "As_tensile");
            setIfNotNull(ctx, request, "asCompressiveArea", "As_compressive");
            setIfNotNull(ctx, request, "lp", "lp");
            setIfNotNull(ctx, request, "B", "B");
            setIfNotNull(ctx, request, "ls", "ls");
            setIfNotNull(ctx, request, "lbPrime", "lb_prime");
            setIfNotNull(ctx, request, "lbDoubleprime", "lb_doubleprime");
            setIfNotNull(ctx, request, "hbPrime", "hb_prime");
            setIfNotNull(ctx, request, "hbDoubleprime", "hb_doubleprime");
            setIfNotNull(ctx, request, "mpMonolithic", "Mp_monolithic");
            setIfNotNull(ctx, request, "mpExternalCantilever", "Mp_external_cantilever");
        } catch (Exception e) {
            // Если DTO не содержит некоторых полей — просто пропускаем
        }
    }

    private void setIfNotNull(BridgeContext ctx, Object request, String getterName, String ctxFieldName)
            throws Exception {
        try {
            java.lang.reflect.Method getter = request.getClass().getMethod(getterName);
            Object value = getter.invoke(request);
            if (value != null) {
                java.lang.reflect.Field field = BridgeContext.class.getField(ctxFieldName);
                field.set(ctx, value);
            }
        } catch (NoSuchMethodException e) {
            // Поле отсутствует в DTO — игнорируем
        }
    }

    private String captureReport(Runnable reportGenerator) {
        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        java.io.PrintStream oldOut = System.out;
        System.setOut(new java.io.PrintStream(baos, true, java.nio.charset.StandardCharsets.UTF_8));
        try {
            reportGenerator.run();
        } finally {
            System.setOut(oldOut);
        }
        return baos.toString(java.nio.charset.StandardCharsets.UTF_8);
    }
}