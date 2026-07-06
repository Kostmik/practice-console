package org.example.context;

import org.example.model.RebarType;
import org.example.model.TrackType;

public class BridgeContext {
    // Исходные данные
    public double spanLength;                    // Расчетный пролет l, м
    public double ballastThickness;              // Толщина балласта hb, м
    public TrackType trackType;                  // Тип пути
    public double concreteStrengthR;             // Фактическая прочность бетона R, МПа

    // Геометрия балок
    public double distanceBetweenBeams; // Расстояние между осями главных балок c, м
    public double trackOffsetLeft;      // Смещение оси пути у левой опоры e1, м
    public double trackOffsetRight;     // Смещение оси пути у правой опоры e2, м

    // Удельные веса
    public double gammaReinforcedConcrete = 24.52;  // кН/м³
    public double gammaBallastWithTrack;            // кН/м³

    // Коэффициенты надежности
    public double np = 1.1;      // для веса ж/б
    public double npPrime = 1.2; // для веса балласта
    public double nk = 1.15;     // для временной нагрузки

    // === ХАРАКТЕРИСТИКИ МАТЕРИАЛОВ (Раздел 5) ===
    public double Rb;          // Расчетное сопротивление бетона сжатию, МПа
    public double Rbt;         // Расчетное сопротивление бетона растяжению, МПа
    public double Eb;          // Модуль упругости бетона, МПа
    public double nPrime;      // Условное отношение модулей упругости арматуры и бетона (n')

    public double Rs;          // Расчетное сопротивление арматуры растяжению, МПа
    public double Es = 206000; // Модуль упругости арматуры, МПа (2.06 * 10^5)
    public String rebarType;   // Тип арматуры (для вывода)

    // Результаты расчетов (Раздел 6)
    public Double dynamicCoeffBeam;
    public Double dynamicCoeffSlab;
    public Double ppSlab;
    public Double pbSlab;
    public Double ppBeam;
    public Double pbBeam;

    // === ДОЛИ ВРЕМЕННОЙ НАГРУЗКИ (Раздел 6.6 - 6.7) ===
    public Double epsilonM_Beam1; // Доля на балку 1 по моменту
    public Double epsilonQ_Beam1; // Доля на балку 1 по силе
    public Double epsilonM_Beam2; // Доля на балку 2 по моменту
    public Double epsilonQ_Beam2; // Доля на балку 2 по силе

    // === ГЕОМЕТРИЯ И АРМАТУРА ПЛИТЫ (Раздел 7.2) ===
    public double slabHeight;                // Высота плиты h, м
    public double as_tensile;                // Расстояние до центра растянутой арматуры as, м
    public double as_compressive;            // Расстояние до центра сжатой арматуры as', м
    public double As_tensile;                // Площадь растянутой арматуры As, м²
    public double As_compressive;            // Площадь сжатой арматуры As', м²

    // Геометрия поперечного сечения плиты
    public double lp;                        // Расстояние между внутренними гранями ребер lp, м
    public double B;                         // Расстояние между наружными гранями ребер B, м
    public double ls;                        // Длина шпалы ls, м
    public double lb_prime;                  // Расстояние от наружной грани ребра до внутренней грани левого борта l'b, м
    public double lb_doubleprime;            // Расстояние от наружной грани ребра до внутренней грани правого борта l''b, м
    public double hb_prime;                  // Толщина балласта под левым концом шпалы h'b, м
    public double hb_doubleprime;            // Толщина балласта под правым концом шпалы h''b, м

    // Моменты от постоянных нагрузок (вводятся или считаются)
    public double Mp_monolithic;             // Момент от пост. нагрузок в монолитном участке, кН·м
    public double Mp_external_cantilever;    // Момент от пост. нагрузок во внешней консоли, кН·м

    // Предельные моменты (считаются программой)
    public double M_pred_I;                  // Предельный момент в сечении I-I, кН·м
    public double M_pred_II;                 // Предельный момент в сечении II-II, кН·м
    public double M_pred_III;                // Предельный момент в сечении III-III, кН·м

    // Допускаемые нагрузки и классы
    public double k_monolithic;              // Допускаемая нагрузка для монолитного участка, кН/м
    public double K_monolithic;              // Класс монолитного участка
    public double k_external_cantilever;     // Допускаемая нагрузка для внешней консоли, кН/м
    public double K_external_cantilever;     // Класс внешней консоли


    public double b;
    public double l_o;
    public double j;
    public int designYear;
    public double beta;
}