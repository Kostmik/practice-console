package org.example.controller;

import org.example.calculator.paragraph_15.InspectionCalculator;
import org.example.calculator.paragraph_15.InspectionInput;
import org.example.calculator.paragraph_15.InspectionOutput;
import org.example.dto.inspection.InspectionRequest;
import org.example.dto.inspection.InspectionResponse;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/inspection")
@CrossOrigin(origins = "*")
public class InspectionController {

    @PostMapping("/calculate")
    public InspectionResponse calculate(@RequestBody InspectionRequest request) {
        // 1. Маппим DTO в InspectionInput
        InspectionInput input = new InspectionInput();
        input.aPrime = request.aPrime();
        input.bPrime = request.bPrime();
        input.b0Prime = request.b0Prime();
        input.concreteStrengths = request.concreteStrengths().stream()
            .mapToDouble(Double::doubleValue).toArray();
        input.deflections = request.deflections().stream()
            .mapToDouble(Double::doubleValue).toArray();
        input.inertias = request.inertias().stream()
            .mapToDouble(Double::doubleValue).toArray();
        input.targetBeamIndex = request.targetBeamIndex();

        // 2. Перехватываем вывод отчёта
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

        InspectionOutput output;
        try {
            output = InspectionCalculator.calculateAndReport(input);
        } finally {
            System.setOut(oldOut);
        }

        // 3. Формируем строки для таблицы
        List<InspectionResponse.BeamDeflectionRow> beamRows = new ArrayList<>();
        for (int i = 0; i < input.deflections.length; i++) {
            beamRows.add(new InspectionResponse.BeamDeflectionRow(
                i + 1,
                input.deflections[i],
                input.inertias[i],
                input.deflections[i] * input.inertias[i],
                i == input.targetBeamIndex
            ));
        }

        // 4. Возвращаем результат
        return new InspectionResponse(
            output.trackOffset,
            output.avgConcreteStrength,
            output.numberOfMeasurements,
            output.epsilonM,
            output.sumFiIi,
            output.fTargetITarget,
            input.targetBeamIndex,
            input.deflections.length,
            beamRows,
            baos.toString(StandardCharsets.UTF_8)
        );
    }
}