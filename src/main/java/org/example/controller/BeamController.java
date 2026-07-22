package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_5.MaterialCalculator;
import org.example.calculator.paragraph_7.BeamFatigueCalculator;
import org.example.calculator.paragraph_7.BeamMomentCalculator;
import org.example.calculator.paragraph_7.BeamShearCalculator;
import org.example.context.BridgeContext;
import org.example.dto.beam.*;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.slab.MaterialsData;
import org.example.model.TrackType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/beam")
@CrossOrigin(origins = "*")
public class BeamController {

    // =====================================================================
    // 7.2.5-7.2.6 РАСЧЕТ БАЛКИ ПО ИЗГИБАЮЩЕМУ МОМЕНТУ
    // =====================================================================
    @PostMapping("/moment")
    public ResponseEntity<BeamMomentResponse> calculateBeamMoment(
            @Valid @RequestBody BeamMomentRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyMaterials(ctx, request.commonData(), request.materials());
        applyBeamGeometry(ctx, request.beamHeight(), request.beamWidth(),
                request.bf(), request.hf(), request.asBeamTensile(),
                request.asBeamCompressive(), request.asBeamTensileArea(),
                request.asBeamCompressiveArea());

        ctx.np = request.np() != null ? request.np() : 1.1;
        ctx.npPrime = request.npPrime() != null ? request.npPrime() : 1.2;
        ctx.nk = request.nk() != null ? request.nk() : 1.15;
        ctx.ppBeam = request.ppBeam() != null ? request.ppBeam() : 0.0;
        ctx.pbBeam = request.pbBeam() != null ? request.pbBeam() : 0.0;
        ctx.epsilonM_Beam1 = request.epsilonM() != null ? request.epsilonM() : 0.5;
        ctx.dynamicCoeffBeam = request.dynamicCoeffBeam() != null ? request.dynamicCoeffBeam() : 1.325;

        String report = captureReport(() -> {
            BeamMomentCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new BeamMomentResponse(
                ctx.M_pred_beam,
                ctx.Mp_beam,
                ctx.k_beam_moment,
                ctx.kc_beam,
                ctx.K_beam_moment,
                report
        ));
    }

    // =====================================================================
    // 7.2.7-7.2.8 РАСЧЕТ БАЛКИ ПО ПОПЕРЕЧНОЙ СИЛЕ
    // =====================================================================
    @PostMapping("/shear")
    public ResponseEntity<BeamShearResponse> calculateBeamShear(
            @Valid @RequestBody BeamShearRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyMaterials(ctx, request.commonData(), request.materials());
        applyBeamGeometry(ctx, request.beamHeight(), request.beamWidth(),
                request.bf(), request.hf(), request.asBeamTensile(),
                request.asBeamCompressive(), request.asBeamTensileArea(),
                request.asBeamCompressiveArea());

        ctx.np = request.np() != null ? request.np() : 1.1;
        ctx.npPrime = request.npPrime() != null ? request.npPrime() : 1.2;
        ctx.nk = request.nk() != null ? request.nk() : 1.15;
        ctx.ppBeam = request.ppBeam() != null ? request.ppBeam() : 0.0;
        ctx.pbBeam = request.pbBeam() != null ? request.pbBeam() : 0.0;
        ctx.epsilonQ_Beam1 = request.epsilonQ() != null ? request.epsilonQ() : 0.5;
        ctx.dynamicCoeffBeam = request.dynamicCoeffBeam() != null ? request.dynamicCoeffBeam() : 1.325;

        ctx.Asw = request.asw();
        ctx.s_stirrups = request.sStirrups();
        ctx.sum_Asi = request.sumAsi();
        ctx.alpha_bent = request.alphaBent();
        ctx.Omega_p = request.omegaP();
        ctx.Omega_k = request.omegaK();

        String report = captureReport(() -> {
            BeamShearCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new BeamShearResponse(
                ctx.Q_ultimate,
                ctx.Q_p_shear,
                ctx.k_beam_shear,
                ctx.K_beam_shear,
                report
        ));
    }

    // =====================================================================
    // 7.3.2 РАСЧЕТ БАЛКИ НА ВЫНОСЛИВОСТЬ
    // =====================================================================
    @PostMapping("/fatigue")
    public ResponseEntity<BeamFatigueResponse> calculateBeamFatigue(
            @Valid @RequestBody BeamFatigueRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        applyMaterials(ctx, request.commonData(), request.materials());
        applyBeamGeometry(ctx, request.beamHeight(), request.beamWidth(),
                request.bf(), request.hf(), request.asBeamTensile(),
                request.asBeamCompressive(), request.asBeamTensileArea(),
                request.asBeamCompressiveArea());

        ctx.np = request.np() != null ? request.np() : 1.1;
        ctx.npPrime = request.npPrime() != null ? request.npPrime() : 1.2;
        ctx.nk = request.nk() != null ? request.nk() : 1.15;
        ctx.ppBeam = request.ppBeam() != null ? request.ppBeam() : 0.0;
        ctx.pbBeam = request.pbBeam() != null ? request.pbBeam() : 0.0;
        ctx.epsilonM_Beam1 = request.epsilonM() != null ? request.epsilonM() : 0.5;
        ctx.dynamicCoeffBeam = request.dynamicCoeffBeam() != null ? request.dynamicCoeffBeam() : 1.325;

        // Для выносливости нужен k_beam_moment из расчёта на прочность.
        // Запускаем расчёт момента "тихо".
        BeamMomentCalculator.calculateAndPrintReport(ctx);

        String report = captureReport(() -> {
            BeamFatigueCalculator.calculateAndPrintReport(ctx);
        });

        return ResponseEntity.ok(new BeamFatigueResponse(
                ctx.k_fatigue_beam_concrete,
                ctx.K_fatigue_beam_concrete,
                ctx.k_fatigue_beam_rebar,
                ctx.K_fatigue_beam_rebar,
                Math.min(ctx.K_fatigue_beam_concrete, ctx.K_fatigue_beam_rebar),
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

    private void applyMaterials(BridgeContext ctx, BridgeCommonData common, MaterialsData materials) {
        if (materials != null) {
            ctx.Rb = materials.Rb();
            ctx.Rbt = materials.Rbt();
            ctx.Eb = materials.Eb();
            ctx.Rs = materials.Rs();
            ctx.Es = materials.Es();
            ctx.nPrime = materials.nPrime();
            ctx.rebarType = common.rebarType() == 1 ? "Гладкая (А240)" : "Периодического профиля (А400)";
        } else {
            MaterialCalculator.calculateAndReturnReport(ctx, common.rebarType());
        }
    }

    private void applyBeamGeometry(BridgeContext ctx, Double beamHeight, Double beamWidth,
                                   Double bf, Double hf, Double asTensile, Double asCompressive,
                                   Double asTensileArea, Double asCompressiveArea) {
        if (beamHeight != null) ctx.beamHeight = beamHeight;
        if (beamWidth != null) ctx.beamWidth = beamWidth;
        if (bf != null) ctx.bf = bf;
        if (hf != null) ctx.hf = hf;
        if (asTensile != null) ctx.as_beam_tensile = asTensile;
        if (asCompressive != null) ctx.as_beam_compressive = asCompressive;
        if (asTensileArea != null) ctx.As_beam_tensile = asTensileArea;
        if (asCompressiveArea != null) ctx.As_beam_compressive = asCompressiveArea;
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