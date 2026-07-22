package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_10.BoardInput;
import org.example.calculator.paragraph_10.BoardResult;
import org.example.calculator.paragraph_10.BoardTables;
import org.example.calculator.paragraph_10.LongitudinalBoardCalculator;
import org.example.dto.board.BoardRequest;
import org.example.dto.board.BoardResponse;
import org.example.dto.slab.MaterialsData;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * РАЗДЕЛ 10. Расчёт грузоподъёмности продольного борта балластного корыта.
 *
 * <p>Калькулятор {@link LongitudinalBoardCalculator} считает все четыре проверки
 * (момент, поперечная сила, выносливость бетона и арматуры) за один проход,
 * поэтому раздел обслуживается одним эндпоинтом.
 */
@RestController
@RequestMapping("/api/v1/board")
@CrossOrigin(origins = "*")
public class BoardController {

    // =====================================================================
    // 10.1-10.3 ПОЛНЫЙ РАСЧЁТ ПРОДОЛЬНОГО БОРТА
    // =====================================================================
    @PostMapping("/calculate")
    public ResponseEntity<BoardResponse> calculateBoard(
            @Valid @RequestBody BoardRequest request) {

        BoardInput in = mapToInput(request);
        BoardResult r = LongitudinalBoardCalculator.calculate(in);

        String report = captureReport(() -> {
            LongitudinalBoardCalculator.printReport(in, r);
        });

        return ResponseEntity.ok(new BoardResponse(
                r.sigmaBmax,
                r.Mp,
                r.Qp,
                r.Mlimit,
                r.Qlimit,
                BoardTables.F(in.hb, in.xShoulder),
                BoardTables.Phi(in.hb, in.xShoulder),
                r.Rbf,
                r.Rsf,
                r.xFatigue,
                r.Ired,
                r.kMoment,
                r.kShear,
                r.kFatigueConcrete,
                r.kFatigueRebar,
                r.kGoverning,
                r.governingCase,
                r.classK,
                report
        ));
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================

    /** Перекладывает DTO в BoardInput, подставляя значения по умолчанию из Руководства. */
    private BoardInput mapToInput(BoardRequest req) {
        BoardInput in = new BoardInput();

        // --- Материалы (Раздел 5) ---
        MaterialsData m = req.materials();
        if (m != null) {
            in.Rb = val(m.Rb(), 0.0);
            in.Rbt = val(m.Rbt(), 0.0);
            in.Rs = val(m.Rs(), 0.0);
            in.nPrime = val(m.nPrime(), 10.0);
        }
        // Для ненапрягаемой арматуры Rsc принимается равным Rs
        in.Rsc = val(req.rsc(), in.Rs);

        // --- Коэффициенты надёжности (Раздел 6) ---
        in.np = val(req.np(), 1.1);
        in.npPrime = val(req.npPrime(), 1.2);
        in.nk = val(req.nk(), 1.15);
        in.gammaBallast = val(req.gammaBallast(), 19.6);
        in.dynamicCoeff = val(req.dynamicCoeff(), 1.0);

        // --- Геометрия борта ---
        in.hb = val(req.hb(), 0.0);
        in.xShoulder = val(req.xShoulder(), 0.0);
        in.Hbr = val(req.hbr(), 0.0);
        in.ls = val(req.ls(), 2.7);
        in.b = val(req.b(), 1.0);
        in.aBallastCm = val(req.aBallastCm(), 0.0);

        // --- Сечение и арматура ---
        in.h0 = val(req.h0(), 0.0);
        in.As = val(req.asArea(), 0.0);
        in.AsPrime = val(req.asPrimeArea(), 0.0);
        in.asPrime = val(req.asPrimeDist(), 0.0);

        // --- Поперечная сила (ф. 7.17) ---
        in.cShear = val(req.cShear(), 0.0);
        in.sStirrup = val(req.sStirrup(), 1.0);
        in.Asw = val(req.asw(), 0.0);
        in.sumAsiSinAlpha = val(req.sumAsiSinAlpha(), 0.0);

        // --- Постоянные нагрузки на борт ---
        in.phiFrictionDeg = val(req.phiFrictionDeg(), 40.0);
        in.pt = val(req.pt(), 0.0);
        in.yt = val(req.yt(), 0.0);
        in.P0 = val(req.p0(), 0.0);
        in.lt = val(req.lt(), 0.0);
        in.lk = val(req.lk(), 0.0);

        // --- Кривая ---
        in.R = val(req.curveRadius(), 0.0);
        int track = req.track() != null ? req.track() : 1;
        in.track = switch (track) {
            case 2 -> BoardTables.Track.CURVE_R600;
            case 3 -> BoardTables.Track.CURVE_R300;
            default -> BoardTables.Track.STRAIGHT;
        };

        // --- Выносливость ---
        in.rhoB = val(req.rhoB(), 0.0);
        in.rhoS = val(req.rhoS(), 0.0);
        in.epsilonDyn = val(req.epsilonDyn(), 1.0);

        // --- Класс (ф. 4.1) ---
        in.kc = val(req.kc(), 0.0);

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