package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.paragraph_12.DefectCalculator;
import org.example.calculator.paragraph_12.DefectInput;
import org.example.calculator.paragraph_12.DefectOutput;
import org.example.dto.defect.DefectRequest;
import org.example.dto.defect.DefectResponse;
import org.example.dto.slab.MaterialsData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * РАЗДЕЛ 12. Учёт влияния дефектов пролётного строения.
 *
 * <p>Раздел не даёт самостоятельного расчёта грузоподъёмности: он корректирует величины,
 * входящие в расчёты на прочность разделов 7–9 — фактическую площадь арматуры (12.2),
 * высоту сжатой зоны при трещинах (12.3) и предельный изгибающий момент при раковинах
 * и сколах бетона (12.4–12.7).
 *
 * <p>Один запрос обрабатывает один вид дефекта, выбираемый полем {@code defectType}.
 */
@RestController
@RequestMapping("/api/v1/defect")
@CrossOrigin(origins = "*")
public class DefectController {

    // =====================================================================
    // 12.1-12.7 УЧЁТ ДЕФЕКТА ВЫБРАННОГО ВИДА
    // =====================================================================
    @PostMapping("/calculate")
    public ResponseEntity<DefectResponse> calculateDefect(
            @Valid @RequestBody DefectRequest request) {

        DefectInput in = mapToInput(request);
        DefectOutput r = DefectCalculator.calculate(in);

        String report = captureReport(() -> {
            DefectCalculator.printReport(in, r);
        });

        return ResponseEntity.ok(new DefectResponse(
                in.type.name(),
                safe(r.As_full),
                safe(r.As_actual),
                safe(r.j),
                r.condFormula82,
                safe(r.xPhi),
                safe(r.xDesign),
                r.crackReducesCapacity,
                safe(r.Mcrack),
                safe(r.x0),
                safe(r.Mx0),
                safe(r.M0),
                safe(r.governingMoment),
                r.note,
                report
        ));
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================

    /** Перекладывает DTO в DefectInput. */
    private DefectInput mapToInput(DefectRequest req) {
        DefectInput in = new DefectInput();

        in.type = parseType(req.defectType());

        // --- Материалы (Раздел 5) ---
        MaterialsData m = req.materials();
        if (m != null) {
            in.Rb = val(m.Rb(), 0.0);
            in.Rs = val(m.Rs(), 0.0);
        }
        // Для ненапрягаемой арматуры Rsc принимается равным Rs
        in.Rsc = val(req.rsc(), in.Rs);
        in.Rp = val(req.rp(), 0.0);
        in.sigmaPc = val(req.sigmaPc(), 0.0);

        // --- Геометрия сечения ---
        in.b = val(req.b(), 0.0);
        in.bfPrime = val(req.bfPrime(), 0.0);
        in.hfPrime = val(req.hfPrime(), 0.0);
        in.h0 = val(req.h0(), 0.0);
        in.asPrime = val(req.asPrime(), 0.0);

        // --- Арматура ---
        in.As = val(req.asArea(), 0.0);
        in.AsPrime = val(req.asPrimeArea(), 0.0);
        in.Ap = val(req.apArea(), 0.0);
        in.ApPrime = val(req.apPrimeArea(), 0.0);
        in.apPrime = val(req.apPrimeDist(), 0.0);

        // --- 12.2 ---
        in.nBars = req.nBars() != null ? req.nBars() : 0;
        in.faOne = val(req.faOne(), 0.0);
        in.nDisconnected = req.nDisconnected() != null ? req.nDisconnected() : 0;
        in.corrodedLoss = toArray(req.corrodedLoss());

        // --- 12.3 ---
        in.xBarPhi = val(req.xBarPhi(), 0.0);
        in.Mbar = val(req.mBar(), 0.0);
        in.Mult = val(req.mUlt(), 0.0);

        // --- 12.4-12.7 ---
        in.A0 = val(req.a0Area(), 0.0);
        in.a0 = val(req.a0Dist(), 0.0);

        validate(in);
        return in;
    }

    /** Разбор вида дефекта с понятным сообщением об ошибке. */
    private DefectInput.DefectType parseType(String raw) {
        try {
            return DefectInput.DefectType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неизвестный вид дефекта: '" + raw + "'. Допустимо: CORROSION, "
                            + "CRACK_IN_COMPRESSION, VOID_RECTANGULAR, VOID_TSECTION, VOID_PRESTRESSED");
        }
    }

    /**
     * Проверка данных, без которых расчёт даёт деление на ноль.
     * Валидация аннотациями здесь не годится: обязательность поля зависит от вида дефекта.
     */
    private void validate(DefectInput in) {
        switch (in.type) {
            case CORROSION -> {
                if (in.nBars <= 0 || in.faOne <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для расчёта по ф. 12.1 задайте число стержней n > 0 и площадь одного стержня f_a > 0");
                }
            }
            case CRACK_IN_COMPRESSION -> {
                if (in.Rb <= 0 || in.b <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для расчёта по п. 12.3 задайте характеристики материалов (Раздел 5) и ширину сечения b > 0");
                }
            }
            default -> {
                if (in.Rb <= 0 || in.b <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для расчёта по пп. 12.4–12.5 задайте характеристики материалов (Раздел 5) и ширину ребра b > 0");
                }
            }
        }
    }

    private static double[] toArray(List<Double> list) {
        if (list == null || list.isEmpty()) {
            return new double[0];
        }
        return list.stream().mapToDouble(v -> v != null ? v : 0.0).toArray();
    }

    private static double val(Double value, double defaultValue) {
        return value != null ? value : defaultValue;
    }

    /**
     * Защита от NaN и бесконечности: они не являются корректным JSON и ломают разбор
     * ответа на фронтенде. Некорректное значение отдаётся как null.
     */
    private static Double safe(double value) {
        return Double.isFinite(value) ? value : null;
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