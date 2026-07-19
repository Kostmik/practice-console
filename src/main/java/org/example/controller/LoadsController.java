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
    public ResponseEntity<PermanentLoadsResponse> calculatePermanentLoads(
            @Valid @RequestBody PermanentLoadsRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        LoadsCalculator.printLoadsReport(
                ctx,
                request.hSlab(),
                request.vConcrete(),
                request.pDevices() != null ? request.pDevices() : 0.0,
                request.sBallast(),
                request.mBeams()
        );

        return ResponseEntity.ok(new PermanentLoadsResponse(
                ctx.gammaReinforcedConcrete,
                ctx.gammaBallastWithTrack,
                ctx.ppSlab,
                ctx.pbSlab,
                ctx.ppBeam,
                ctx.pbBeam,
                ctx.np,
                ctx.npPrime,
                ctx.nk
        ));
    }

    @PostMapping("/dynamic-coeff")
    public ResponseEntity<DynamicCoeffResponse> calculateDynamicCoeff(
            @Valid @RequestBody DynamicCoeffRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        double lambda = request.lambda() != null ? request.lambda() : ctx.spanLength;
        boolean useMax = request.useMaxCoefficient() != null && request.useMaxCoefficient();

        double dynamicCoeff;
        if (useMax) {
            dynamicCoeff = LoadsCalculator.calculateDynamicCoeffForSlab(ctx, true, 0.0);
        } else {
            dynamicCoeff = LoadsCalculator.calculateDynamicCoeff(ctx, lambda);
        }

        LoadsCalculator.printDynamicCoeffReport(ctx, lambda, request.elementName(), useMax);

        return ResponseEntity.ok(new DynamicCoeffResponse(
                dynamicCoeff,
                10.0, // mu0 (упрощённо, можно вынести из калькулятора)
                lambda
        ));
    }

    @PostMapping("/share")
    public ResponseEntity<ShareResponse> calculateShare(
            @Valid @RequestBody ShareRequest request) {

        BridgeContext ctx = mapCommonData(request.commonData());
        LoadsCalculator.setBallastDensity(ctx, request.commonData().sleeperType() == 2);

        if (request.isMonolithic()) {
            ShareCalculator.calculateMonolithic(ctx, request.xRatio());
        } else {
            ShareCalculator.calculatePrecast(ctx, request.xRatio());
        }

        ShareCalculator.printReport(ctx, request.xRatio(), request.isMonolithic());

        return ResponseEntity.ok(new ShareResponse(
                ctx.epsilonM_Beam1,
                ctx.epsilonQ_Beam1,
                ctx.epsilonM_Beam2,
                ctx.epsilonQ_Beam2
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
}