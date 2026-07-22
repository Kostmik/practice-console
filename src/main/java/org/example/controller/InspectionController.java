package org.example.controller;

import org.example.calculator.paragraph_15.InspectionCalculator;
import org.example.dto.inspection.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/inspection")
@CrossOrigin(origins = "*")
public class InspectionController {

    @PostMapping("/153")
    public ResponseEntity<Inspection153Response> calculate153(@RequestBody Inspection153Request request) {
        double trackOffset = InspectionCalculator.calculateTrackOffset(request.aPrime(), request.bPrime(), request.b0Prime());

        String report = captureReport(() -> {
            InspectionCalculator.printReport153(request.aPrime(), request.bPrime(), request.b0Prime(), trackOffset);
        });

        return ResponseEntity.ok(new Inspection153Response(trackOffset, report));
    }

    @PostMapping("/154")
    public ResponseEntity<Inspection154Response> calculate154(@RequestBody Inspection154Request request) {
        double[] strengths = request.concreteStrengths().stream().mapToDouble(Double::doubleValue).toArray();
        double avg = InspectionCalculator.calculateAvgConcreteStrength(strengths);

        String report = captureReport(() -> {
            InspectionCalculator.printReport154(strengths, avg);
        });

        return ResponseEntity.ok(new Inspection154Response(avg, strengths.length, report));
    }

    @PostMapping("/155")
    public ResponseEntity<Inspection155Response> calculate155(@RequestBody Inspection155Request request) {
        double[] deflections = request.deflections().stream().mapToDouble(Double::doubleValue).toArray();
        double[] inertias = request.inertias().stream().mapToDouble(Double::doubleValue).toArray();

        double[] res = InspectionCalculator.calculateShareByTest(deflections, inertias, request.targetBeamIndex());
        // res[0] = epsilonM, res[1] = sumFiIi, res[2] = fTargetITarget

        String report = captureReport(() -> {
            InspectionCalculator.printReport155(deflections, inertias, request.targetBeamIndex(), res[1], res[2], res[0]);
        });

        return ResponseEntity.ok(new Inspection155Response(
                deflections.length,
                request.targetBeamIndex(),
                res[2],
                res[1],
                res[0],
                report
        ));
    }

    // Вспомогательный метод для перехвата консоли (стандартный для проекта)
    private String captureReport(Runnable reportGenerator) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));
        try {
            reportGenerator.run();
        } finally {
            System.setOut(oldOut);
        }
        return baos.toString(StandardCharsets.UTF_8);
    }
}