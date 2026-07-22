package org.example.controller;

import jakarta.validation.Valid;
import org.example.calculator.common.FatigueResistance;
import org.example.calculator.paragraph_13.CarbonCalculator;
import org.example.calculator.paragraph_13.CarbonInput;
import org.example.calculator.paragraph_13.CarbonOutput;
import org.example.calculator.paragraph_13.CarbonTables;
import org.example.dto.carbon.CarbonRequest;
import org.example.dto.carbon.CarbonResponse;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.slab.MaterialsData;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

/**
 * РАЗДЕЛ 13. Расчёт усиления композиционными материалами на основе углеродного волокна.
 *
 * <p>Раздел выполняет пять независимых проверок усиленной балки, выбираемых полем {@code mode}:
 * прочность нормального сечения по моменту, прочность наклонного сечения по поперечной силе,
 * момент в наклонном сечении, выносливость и учёт технологии усиления.
 *
 * <p>Опирается на результаты Раздела 5 (характеристики материалов), Раздела 6 (коэффициенты
 * надёжности, постоянные нагрузки, доли ε) и Раздела 7 (предельные M и Q неусиленного сечения).
 * Повреждения материала усиления учитывают по п. 12.6.
 */
@RestController
@RequestMapping("/api/v1/carbon")
@CrossOrigin(origins = "*")
public class CarbonController {

    // =====================================================================
    // 13.1-13.29 РАСЧЁТ УСИЛЕНИЯ В ВЫБРАННОМ РЕЖИМЕ
    // =====================================================================
    @PostMapping("/calculate")
    public ResponseEntity<CarbonResponse> calculateCarbon(
            @Valid @RequestBody CarbonRequest request) {

        CarbonInput in = mapToInput(request);
        CarbonOutput r = CarbonCalculator.calculate(in);

        String report = captureReport(() -> {
            CarbonCalculator.printReport(in, r);
        });

        return ResponseEntity.ok(new CarbonResponse(
                in.mode.name(),
                in.reinfType.name(),
                in.reinfType.title(),
                safe(in.reinfType.ks()),

                // прочность по моменту
                safe(r.sigmaFu),
                safe(r.sigmaFu2),
                safe(r.omega),
                safe(r.xiFy),
                safe(r.x),
                r.boundaryInWeb,
                r.xiCapped,
                safe(r.My),
                safe(r.kMoment),

                // поперечная сила
                safe(r.c),
                safe(r.Qb),
                safe(r.Qunstr),
                safe(r.QyConcrete),
                safe(r.QyCrack),
                safe(r.Qy),
                safe(r.QpCalc),
                safe(r.kShear),

                // наклонное сечение
                safe(r.MyInclined),
                safe(r.kInclined),

                // выносливость
                safe(in.Rbf),
                safe(in.Rsf),
                safe(r.xyFatigue),
                r.fatigueTee,
                safe(r.IredFatigue),
                safe(r.MyConcrete),
                safe(r.MyRebar),
                safe(r.es),
                safe(r.kFatigueConcrete),
                safe(r.kFatigueRebar),

                // технология
                safe(r.Mub),

                safe(r.governingK),
                r.note,
                report
        ));
    }

    // =====================================================================
    // ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ
    // =====================================================================

    /** Перекладывает DTO в CarbonInput, подставляя значения по умолчанию из Руководства. */
    private CarbonInput mapToInput(CarbonRequest req) {
        CarbonInput in = new CarbonInput();

        in.mode = parseMode(req.mode());
        in.reinfType = parseReinfType(req.reinfType());

        // --- Материалы (Раздел 5) ---
        MaterialsData m = req.materials();
        if (m != null) {
            in.Rb = val(m.Rb(), 0.0);
            in.Rbt = val(m.Rbt(), 0.0);
            in.Rs = val(m.Rs(), 0.0);
            in.Es = val(m.Es(), 206000.0);
            in.Eb = val(m.Eb(), 0.0);
        }
        // Для ненапрягаемой арматуры R_sc принимается равным R_s
        in.Rsc = val(req.rsc(), in.Rs);
        in.Rsw = val(req.rsw(), in.Rs);

        // Сопротивления на выносливость: если не заданы явно — считаем по ф. 5.1 и 5.2,
        // как это делает раздел 10 (см. LongitudinalBoardCalculator).
        in.Rbf = req.rbf() != null && req.rbf() > 0
                ? req.rbf()
                : FatigueResistance.concrete(in.Rb, val(req.rhoB(), 0.0));
        in.Rsf = req.rsf() != null && req.rsf() > 0
                ? req.rsf()
                : FatigueResistance.rebar(in.Rs, val(req.rhoS(), 0.0));

        // --- Материал усиления ---
        in.Ef = val(req.ef(), 0.0);
        in.Rf = val(req.rf(), 0.0);
        in.tfLayerMm = val(req.tfLayerMm(), 0.0);
        in.nLayers = req.nLayers() != null ? req.nLayers() : 1;
        in.bUnitMm = val(req.bUnitMm(), 1.0);
        in.epsBult = val(req.epsBult(), 0.0033);

        // --- Геометрия сечения ---
        in.b = val(req.b(), 0.0);
        in.bfPrime = val(req.bfPrime(), 0.0);
        in.hfPrime = val(req.hfPrime(), 0.0);
        in.h = val(req.h(), 0.0);
        in.h0 = val(req.h0(), 0.0);
        in.asPrime = val(req.asPrimeDist(), 0.0);
        in.as = val(req.asDist(), 0.0);
        in.au = val(req.au(), 0.0);
        in.d = val(req.dJacket(), 0.0);

        // --- Арматура ---
        in.As = val(req.asArea(), 0.0);
        in.AsPrime = val(req.asPrimeArea(), 0.0);
        in.Asw = val(req.asw(), 0.0);
        in.s = val(req.sStirrup(), 1.0);
        in.sumAsiSinAlpha = val(req.sumAsiSinAlpha(), 0.0);
        in.sumAsi = val(req.sumAsi(), 0.0);
        in.cShear = val(req.cShear(), 0.0);

        // --- Материал усиления: площади ---
        in.Af1 = val(req.af1(), 0.0);
        in.Af2 = val(req.af2(), 0.0);
        in.Afw = val(req.afw(), 0.0);
        in.Afi = val(req.afi(), 0.0);
        in.phiFiberDeg = val(req.phiFiberDeg(), 90.0);
        in.AfPrime = val(req.afPrimeArea(), 0.0);

        // --- Плечи z (ф. 13.18) ---
        in.zS = val(req.zS(), 0.0);
        in.zSw = val(req.zSw(), 0.0);
        in.zSi = val(req.zSi(), 0.0);
        in.zC1 = val(req.zC1(), 0.0);
        in.zC2 = val(req.zC2(), 0.0);
        in.zCw = val(req.zCw(), 0.0);
        in.zCi = val(req.zCi(), 0.0);

        // --- Нагрузки и коэффициенты ---
        in.M = val(req.mUlt(), 0.0);
        in.Mp = val(req.mp(), 0.0);
        in.Q = val(req.qUlt(), 0.0);
        in.Qp = val(req.qp(), 0.0);
        in.nk = val(req.nk(), 1.15);
        in.np = val(req.np(), 1.1);
        in.npPrime = val(req.npPrime(), 1.2);
        in.pp = val(req.pp(), 0.0);
        in.pb = val(req.pb(), 0.0);
        in.epsM = val(req.epsM(), 0.0);
        in.epsQ = val(req.epsQ(), 0.0);
        in.Theta = val(req.theta(), 1.0);

        // Ω по умолчанию — как в разделе 7: Ω = 0,5·l·(l/4) = l²/8
        BridgeCommonData common = req.commonData();
        double span = common != null ? val(common.spanLength(), 0.0) : 0.0;
        in.Omega = req.omegaM() != null && req.omegaM() > 0
                ? req.omegaM()
                : 0.5 * span * (span / 4.0);
        in.OmegaK = val(req.omegaK(), 0.0);
        in.OmegaP = val(req.omegaP(), 0.0);

        // --- Выносливость ---
        in.nPrimeSteel = val(req.nPrimeSteel(), m != null ? val(m.nPrime(), 0.0) : 0.0);
        // n'_f по умолчанию — отношение модулей упругости углеволокна и бетона
        in.nPrimeFiber = req.nPrimeFiber() != null && req.nPrimeFiber() > 0
                ? req.nPrimeFiber()
                : (in.Eb > 0 ? in.Ef / in.Eb : 0.0);

        // --- Технология ---
        in.My = val(req.my(), 0.0);
        in.Mk = val(req.mk(), 0.0);
        in.MyK = val(req.myK(), 0.0);

        validate(in);
        return in;
    }

    /**
     * Проверка данных, без которых выбранный режим даёт деление на ноль.
     * Аннотации здесь не годятся: обязательность поля зависит от режима.
     */
    private void validate(CarbonInput in) {
        switch (in.mode) {
            case STRENGTH_MOMENT -> {
                requireFiber(in);
                if (in.M <= 0 || in.epsM <= 0 || in.Omega <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для ф. 13.1 нужны предельный момент M > 0 (Раздел 7), доля ε_M > 0 "
                                    + "(Раздел 6.6) и площадь линии влияния Ω > 0");
                }
            }
            case STRENGTH_SHEAR -> {
                if (in.b <= 0 || in.h0 <= 0 || in.s <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для расчёта по поперечной силе нужны b > 0, h0 > 0 и шаг хомутов s > 0");
                }
                if (in.epsQ <= 0 || in.OmegaK <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для ф. 13.10 нужны доля ε_Q > 0 (Раздел 6.7) и площадь линии влияния Ω_к > 0");
                }
            }
            case INCLINED_MOMENT -> {
                if (in.M <= 0 || in.epsM <= 0 || in.Omega <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для ф. 13.17 нужны предельный момент M > 0 (Раздел 7), доля ε_M > 0 и Ω > 0");
                }
            }
            case FATIGUE -> {
                if (in.b <= 0 || in.bfPrime <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для расчёта на выносливость нужны ширина ребра b > 0 и ширина полки b'_f > 0");
                }
                if (in.M <= 0 || in.epsM <= 0 || in.Omega <= 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для ф. 13.19 нужны предельный момент M > 0 (Раздел 7), доля ε_M > 0 и Ω > 0");
                }
            }
            case TECHNOLOGY -> {
                if (in.Mp + in.Mk == 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Для ф. 13.29 сумма M_p + M_k не должна быть нулевой");
                }
            }
        }
    }

    /** Материал усиления обязателен там, где считается σ_fu по ф. 13.2. */
    private void requireFiber(CarbonInput in) {
        if (in.Ef <= 0 || in.Rf <= 0 || in.tfLayerMm <= 0 || in.nLayers <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Для ф. 13.2 задайте характеристики материала усиления: E_f, R_f, "
                            + "толщину слоя t_f и число слоёв");
        }
    }

    private CarbonInput.Mode parseMode(String raw) {
        try {
            return CarbonInput.Mode.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неизвестный режим расчёта: '" + raw + "'. Допустимо: STRENGTH_MOMENT, "
                            + "STRENGTH_SHEAR, INCLINED_MOMENT, FATIGUE, TECHNOLOGY");
        }
    }

    private CarbonTables.ReinforcementType parseReinfType(String raw) {
        if (raw == null || raw.isBlank()) {
            return CarbonTables.ReinforcementType.U_JACKET_FREE;
        }
        try {
            return CarbonTables.ReinforcementType.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Неизвестный тип усиления: '" + raw + "'. Допустимо: SHEET_BOTTOM_FREE, "
                            + "SHEET_BOTTOM_ANCHORED, U_JACKET_FREE, U_JACKET_ANCHORED, PLATE_ANCHORED");
        }
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