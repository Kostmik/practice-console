package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_6.LoadsCalculator;
import org.example.calculator.paragraph_6.ShareCalculator;
import org.example.context.BridgeContext;
import org.example.dto.loads.*;
import org.example.model.TrackType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loads")
@CrossOrigin(origins = "*")
public class LoadsController {

    @PostMapping("/permanent")
    public ResponseEntity<PermanentLoadsResponse> calculatePermanentLoads(@Valid @RequestBody PermanentLoadsRequest request) {
        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        String report = captureReport(() -> LoadsCalculator.printLoadsReport(
                ctx, request.hSlab(), request.vConcrete(),
                request.pDevices() != null ? request.pDevices() : 0.0,
                request.sBallast(), request.mBeams()
        ));

        return ResponseEntity.ok(new PermanentLoadsResponse(
                ctx.gammaReinforcedConcrete, ctx.gammaBallastWithTrack,
                ctx.ppSlab, ctx.pbSlab, ctx.ppBeam, ctx.pbBeam,
                ctx.np, ctx.npPrime, ctx.nk, report
        ));
    }

    @PostMapping("/dynamic-coeff")
    public ResponseEntity<DynamicCoeffResponse> calculateDynamicCoeff(@Valid @RequestBody DynamicCoeffRequest request) {
        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        double lambda = request.lambda() != null ? request.lambda() : ctx.spanLength;
        boolean useMax = request.useMaxCoefficient() != null && request.useMaxCoefficient();

        String report = captureReport(() -> {
            double dc = useMax ? LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0)
                    : LoadsCalculator.calculateDynamicCoeff(ctx, lambda);
            LoadsCalculator.printDynamicCoeffReport(ctx, lambda, request.elementName(), useMax);
        });

        return ResponseEntity.ok(new DynamicCoeffResponse(
                useMax ? LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0) : LoadsCalculator.calculateDynamicCoeff(ctx, lambda),
                10.0, lambda, report
        ));
    }

    @PostMapping("/share")
    public ResponseEntity<ShareResponse> calculateShare(@Valid @RequestBody ShareRequest request) {
        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        String report = captureReport(() -> {
            if (request.isMonolithic()) {
                ShareCalculator.calculateMonolithic(ctx, request.xRatio());
            } else {
                ShareCalculator.calculatePrecast(ctx, request.xRatio());
            }
            ShareCalculator.printReport(ctx, request.xRatio(), request.isMonolithic());
        });

        return ResponseEntity.ok(new ShareResponse(
                ctx.epsilonM_Beam1, ctx.epsilonQ_Beam1,
                ctx.epsilonM_Beam2, ctx.epsilonQ_Beam2, report
        ));
    }

    // Вспомогательный метод для маппинга общих данных
    private BridgeContext mapCommonData(org.example.dto.common.BridgeCommonData common) {
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

    // Вспомогательный метод для перехвата консоли в строку
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