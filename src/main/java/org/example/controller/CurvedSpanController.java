package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_11.CurvedSpanCalculator;
import org.example.calculator.paragraph_11.CurvedSpanInput;
import org.example.calculator.paragraph_11.CurvedSpanResult;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.curved.CurvedSpanRequest;
import org.example.dto.curved.CurvedSpanResponse;
import org.example.dto.slab.MaterialsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * РАЗДЕЛ 11. Расчёт грузоподъёмности пролётных строений, расположенных на кривых.
 *
 * <p>Раздел не даёт допускаемой нагрузки напрямую: он определяет доли временной нагрузки
 * ε_M и ε_Q, приходящиеся на главные балки монолитного пролётного строения с ненапрягаемой
 * арматурой на кривой. Эти доли далее подставляются в расчёт балки (Раздел 7) вместо долей,
 * полученных по п. 6.6–6.7 для прямого участка.
 */
@RestController
@RequestMapping("/api/v1/curved")
@CrossOrigin(origins = "*")
public class CurvedSpanController {

    // =====================================================================
    // 11.1-11.14 ДОЛИ ВРЕМЕННОЙ НАГРУЗКИ НА БАЛКУ НА КРИВОЙ
    // =====================================================================
    @PostMapping("/calculate")
    public ResponseEntity<CurvedSpanResponse> calculateCurvedSpan(
            @Valid @RequestBody CurvedSpanRequest request) {

        CurvedSpanInput in = mapToInput(request);
        CurvedSpanResult r = CurvedSpanCalculator.calculate(in);

        String report = captureReport(() -> {
            CurvedSpanCalculator.printReport(in, r);
        });

        return ResponseEntity.ok(new CurvedSpanResponse(
                r.sinAlpha,
                r.cosAlpha,
                r.c1,
                r.H,
                r.IyPrime10,
                r.IyPrime7,
                r.Ik,
                r.Iomega,
                r.d,
                r.omega,
                r.omegaD,
                r.omegaK,
                r.y,
                r.m,
                r.g,
                r.gamma,
                r.dlM_moving,
                r.dlQ_moving,
                r.dl_standing,
                r.epsM_beam1,
                r.epsQ_beam1,
                r.epsM_beam2,
                r.epsQ_beam2,
                r.epsM_beam1_standing,
                r.epsQ_beam1_standing,
                r.epsM_beam2_standing,
                r.epsQ_beam2_standing,
                r.epsM_design,
                r.epsQ_design,
                report
        ));
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================

    /** Перекладывает DTO в CurvedSpanInput, подставляя значения по умолчанию. */
    private CurvedSpanInput mapToInput(CurvedSpanRequest req) {
        CurvedSpanInput in = new CurvedSpanInput();

        // --- Из паспорта объекта ---
        BridgeCommonData common = req.commonData();
        in.c = val(common.distanceBetweenBeams(), 0.0);
        in.l = val(common.spanLength(), 0.0);
        in.hb = val(common.ballastThickness(), 0.0);

        // --- Материалы (Раздел 5) ---
        MaterialsData m = req.materials();
        if (m != null) {
            in.Es = val(m.Es(), 0.0);
            in.Eb = val(m.Eb(), 0.0);
        }

        // --- Геометрия поперечного сечения ---
        in.b = val(req.b(), 0.0);
        in.h = val(req.h(), 0.0);
        in.h1 = val(req.h1(), 0.0);
        in.h2 = val(req.h2(), 0.0);
        in.lk = val(req.lk(), 0.0);
        in.as = val(req.asDist(), 0.0);
        in.As = val(req.asArea(), 0.0);

        // --- Верхнее строение пути ---
        in.hp = val(req.hp(), 0.0);
        in.hs = val(req.hs(), 0.0);
        in.ls = val(req.ls(), 2.7);
        in.ht = val(req.ht(), 2.2);

        // --- Кривая ---
        in.R = val(req.curveRadius(), 0.0);
        in.v = val(req.speed(), 0.0);
        in.cantElevation = val(req.cantElevation(), 0.0);
        in.b0 = val(req.b0(), 1.6);

        // --- Смещение оси пути ---
        in.lPrime = val(req.lPrime(), 0.0);
        in.lDoublePrime = val(req.lDoublePrime(), 0.0);

        // --- Прочее ---
        in.Theta = val(req.theta(), 1.0);
        in.mu0 = val(req.mu0(), 0.0);

        return in;
    }

    private static double val(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
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