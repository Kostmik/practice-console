package org.example.controller;

import org.example.calculator.paragraph_9.PrestressedBeamCalculator;
import org.example.context.BridgeContext;
import org.example.dto.common.BridgeCommonData;
import org.example.dto.paragraph9.Section9Request;
import org.example.dto.paragraph9.Section9Response;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/section9")
@CrossOrigin(origins = "*")
public class Section9Controller {

    @PostMapping("/calculate")
    public Section9Response calculate(@RequestBody Section9Request request) {
        BridgeCommonData common = request.commonData();

        BridgeContext ctx = new BridgeContext();
        ctx.spanLength = common.spanLength();
        ctx.ballastThickness = common.ballastThickness();
        ctx.concreteStrengthR = common.concreteStrengthR();
        ctx.nk = 1.15;
        ctx.np = 1.1;
        ctx.npPrime = 1.2;

        ctx.Rb = request.Rb() != null ? request.Rb() : 16.0;
        ctx.Rs = request.Rs() != null ? request.Rs() : 240.0;
        double Rsc = request.Rsc() != null ? request.Rsc() : ctx.Rs;
        ctx.nPrime = request.nPrime() != null ? request.nPrime() : 20.0;

        double h = request.beamHeight() != null ? request.beamHeight() : 1.35;
        double b = request.beamWidth() != null ? request.beamWidth() : 0.25;
        double bf = request.bf() != null ? request.bf() : 1.6;
        double hf = request.hf() != null ? request.hf() : 0.20;

        double As = request.As_tensile() != null ? request.As_tensile() : 0.0010;
        double As_s = request.As_compressive() != null ? request.As_compressive() : 0.0005;
        double as_s = request.as_compressive() != null ? request.as_compressive() : 0.05;

        double Rp = request.Rp();
        double Rpc = request.Rpc();
        double sigmaP2 = request.sigmaP2();
        double sigmaP2s = request.sigmaP2s();
        double Ap = request.Ap();
        double Ap_s = request.Ap_s();
        double ap = request.ap();
        double ap_s = request.ap_s();

        double Ared = request.Ared();
        double Ired = request.Ired();
        double Theta = request.Theta();

        double pp = ctx.ppBeam != null ? ctx.ppBeam : (request.ppBeam() != null ? request.ppBeam() : 34.0);
        double pb = ctx.pbBeam != null ? ctx.pbBeam : (request.pbBeam() != null ? request.pbBeam() : 20.6);

        double epsilonM = request.epsilonM() != null ? request.epsilonM() : 0.565;
        double epsilonQ = request.epsilonQ() != null ? request.epsilonQ() : epsilonM;

        double OmegaM = (ctx.Omega_M != null && ctx.Omega_M > 0) ? ctx.Omega_M : Math.pow(ctx.spanLength, 2) / 8.0;
        double OmegaQ = (ctx.Omega_k != null && ctx.Omega_k > 0) ? ctx.Omega_k : ctx.spanLength / 2.0;

        ctx.ppBeam = pp;
        ctx.pbBeam = pb;
        ctx.epsilonM_Beam1 = epsilonM;
        ctx.epsilonQ_Beam1 = epsilonQ;
        ctx.Omega_M = OmegaM;
        ctx.Omega_k = OmegaQ;

        double h01 = h - ap;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream oldOut = System.out;
        System.setOut(new PrintStream(baos, true, StandardCharsets.UTF_8));

        try {
            // ШАГ 1-3: Характеристики сжатой зоны и предельный момент
            double xiY = PrestressedBeamCalculator.calculateXiY(ctx.Rb, Rp, sigmaP2, Rpc);
            double sigmaPc = PrestressedBeamCalculator.calculateSigmaPc(Rpc, sigmaP2s);

            double xFlange = PrestressedBeamCalculator.calculateXWithFlange(ctx.Rs, As, Rp, Ap, Rsc, As_s, sigmaPc, Ap_s, ctx.Rb, bf);
            double x;
            double M;

            if (xFlange <= hf) {
                x = xFlange;
                M = PrestressedBeamCalculator.calculateMomentWithFlange(ctx.Rb, bf, x, h01, Rsc, As_s, as_s, sigmaPc, Ap_s, ap_s);
            } else {
                x = PrestressedBeamCalculator.calculateXWithWeb(ctx.Rs, As, Rp, Ap, Rsc, As_s, sigmaPc, Ap_s, ctx.Rb, bf, b, hf);
                M = PrestressedBeamCalculator.calculateMomentWithWeb(ctx.Rb, b, x, h01, bf, hf, Rsc, As_s, as_s, sigmaPc, Ap_s, ap_s);
            }

            if (x > xiY * h01) {
                x = xiY * h01;
                if (x <= hf) {
                    M = PrestressedBeamCalculator.calculateMomentWithFlange(ctx.Rb, bf, x, h01, Rsc, As_s, as_s, sigmaPc, Ap_s, ap_s);
                } else {
                    M = PrestressedBeamCalculator.calculateMomentWithWeb(ctx.Rb, b, x, h01, bf, hf, Rsc, As_s, as_s, sigmaPc, Ap_s, ap_s);
                }
            }

            // ШАГ 4-5: Допускаемая нагрузка по изгибу
            double Mp = (ctx.np * pp + ctx.npPrime * pb) * OmegaM;
            double k_moment = Math.max(0, (M - Mp) / (ctx.nk * epsilonM * OmegaM));

            // ИЗМЕНЕНИЕ: Сначала считаем поперечную силу (ШАГ 6-7)
            double Qp = (ctx.np * pp + ctx.npPrime * pb) * OmegaQ;

            double sumApi = request.sumApi() != null ? request.sumApi() : 0.0015;
            double sinAlpha = request.sinAlpha() != null ? request.sinAlpha() : 0.707;
            double Asw = request.Asw() != null ? request.Asw() : 0.0003;
            double c = request.c() != null ? request.c() : 0.25;
            double s = request.s() != null ? request.s() : 0.15;
            double Qb = request.Qb() != null ? request.Qb() : 150.0;

            double Q_pred = PrestressedBeamCalculator.calculateShearForce(Rp, sumApi, sinAlpha, ctx.Rs, Asw, c, s, Qb);
            double k_shear = Math.max(0, (Q_pred - Qp) / (ctx.nk * epsilonQ * OmegaQ));

            // ШАГ 8: Итоговая нагрузка k_result
            double k_result = Math.min(k_moment, k_shear);

            // ИЗМЕНЕНИЕ: ШАГ 9: Теперь считаем Mk на основе реалистичного k_result
            double Mk = PrestressedBeamCalculator.calculateMk(OmegaM, epsilonM, k_result, Theta);

            // ШАГ 10-14: Расчет выносливости с адекватным Mk
            double hc = request.hc() != null ? request.hc() : 0.7;
            double xPrime = PrestressedBeamCalculator.calculateXForFatigue(h, hc);

            double sigmaRebarMin = PrestressedBeamCalculator.calculateSigmaInRebarMin(sigmaP2, sigmaP2s, ctx.nPrime, Ap, Ap_s, h01, xPrime, ap_s, Ared, Ired, Mp);
            double sigmaRebarMax = PrestressedBeamCalculator.calculateSigmaInRebarMax(sigmaRebarMin, ctx.nPrime, Mk, Ired, h01, xPrime);

            double sigmaConcreteMin = PrestressedBeamCalculator.calculateSigmaInConcreteMin(sigmaP2, Ap, sigmaP2s, Ap_s, h01, xPrime, ap_s, Ared, Ired, Mp);
            double sigmaConcreteMax = PrestressedBeamCalculator.calculateSigmaInConcreteMax(sigmaConcreteMin, Mk, Ired, xPrime);

            double rho = PrestressedBeamCalculator.calculateRho(sigmaRebarMin, sigmaRebarMax);
            double epsilonRo = PrestressedBeamCalculator.getEpsilonRo(rho);
            double Rpf = PrestressedBeamCalculator.calculateRpf(epsilonRo, Rp);

            // Отчет вызывается с новым порядком аргументов (синхронизировано)
            // Отчет вызывается с обновленным порядком аргументов
            PrestressedBeamCalculator.printReport(
                ctx, ctx.Rb, Rp, Rpc,
                sigmaP2, sigmaP2s,
                As, Ap, As_s, Ap_s,
                b, bf, hf,
                h, h01,
                as_s, ap_s,
                Mp, Mk,
                epsilonM, epsilonQ, // <-- ИСПРАВЛЕНО: передаем epsilonQ
                OmegaM, OmegaQ,     // <-- ИСПРАВЛЕНО: передаем OmegaQ
                Theta, ctx.nPrime,
                Ared, Ired,
                x, M,
                xiY, sigmaPc,
                k_result,
                Q_pred,
                hc,
                sigmaRebarMin,
                sigmaRebarMax,
                sigmaConcreteMin,
                sigmaConcreteMax
            );

            double K_moment = k_moment / PrestressedBeamCalculator.getStandardLoad(ctx.spanLength, 0.5);
            double K_shear = k_shear / PrestressedBeamCalculator.getStandardLoad(ctx.spanLength, 0.5);
            double K_result = k_result / PrestressedBeamCalculator.getStandardLoad(ctx.spanLength, 0.5);

            ctx.M_pred_beam = M;
            ctx.Mp_beam = Mp;
            ctx.k_beam_moment = k_moment;
            ctx.K_beam_moment = K_moment;
            ctx.Q_ultimate = Q_pred;
            ctx.Q_p_shear = Qp;
            ctx.k_beam_shear = k_shear;
            ctx.K_beam_shear = K_shear;
            ctx.x_prime_beam = xPrime;
            ctx.I_red_beam = Ired;
            ctx.rho_b_beam = Math.abs(rho);
            ctx.rho_s_beam = Math.abs(rho);
            ctx.Rsf_beam = Rpf;

            // =================================================================
            // ШАГ 17: Возврат результата (СТРОГО по порядку полей в Section9Response)
            // =================================================================
            return new Section9Response(
                k_result,                      // 1. Double k
                xiY,                           // 2. Double xiY
                sigmaPc,                       // 3. Double sigmaPc
                x,                             // 4. Double x
                M,                             // 5. Double M
                Mp,                            // 6. Double Mp
                OmegaM,                        // 7. Double Omega
                Mk,                            // 8. Double Mk
                Q_pred,                        // 9. Double Q_pred
                Rpf,                           // 10. Double Rpf
                rho,                           // 11. Double rho
                sigmaRebarMin,                 // 12. Double sigmaRebarMin
                sigmaRebarMax,                 // 13. Double sigmaRebarMax
                sigmaConcreteMin,              // 14. Double sigmaConcreteMin
                sigmaConcreteMax,              // 15. Double sigmaConcreteMax
                xPrime,                        // 16. Double xPrime
                baos.toString(StandardCharsets.UTF_8) // 17. String detailedReport
            );
        } finally {
            System.setOut(oldOut);
        }
    }
}