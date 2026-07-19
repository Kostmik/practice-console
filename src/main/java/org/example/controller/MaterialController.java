package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_5.MaterialCalculator;
import org.example.context.BridgeContext;
import org.example.dto.MaterialRequest;
import org.example.dto.MaterialResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/materials")
@CrossOrigin(origins = "*")
public class MaterialController {

    @PostMapping("/calculate")
    public ResponseEntity<MaterialResponse> calculateMaterials(@Valid @RequestBody MaterialRequest request) {
        BridgeContext ctx = new BridgeContext();
        ctx.concreteStrengthR = request.concreteStrengthR();

        // Получаем полный текстовый отчёт
        String detailedReport = MaterialCalculator.calculateAndReturnReport(ctx, request.rebarType());

        MaterialResponse response = new MaterialResponse(
                ctx.Rb,
                ctx.Rbt,
                ctx.Eb,
                ctx.Rs,
                ctx.Es,
                ctx.nPrime,
                detailedReport
        );

        return ResponseEntity.ok(response);
    }
}