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
    public double l0;
    public double j;
    public int designYear;
    public double beta;

    // Дополнительные параметры для поперечной силы
    public double lt;           // Длина внешней консоли с учетом тротуара, м
    public double lk;           // Длина внешней консоли плиты, м
    public double P0;           // Нагрузка от веса перил, кН/м
    public double pt;           // Нагрузка от веса тротуара, кН/м

    // === ПОПЕРЕЧНАЯ СИЛА (7.2.4) ===
    public double Q_pred_I;           // Предельная поперечная сила в сечении I-I, кН
    public double Q_pred_II;          // Предельная поперечная сила в сечении II-II, кН
    public double Q_pred_III;         // Предельная поперечная сила в сечении III-III, кН
    public double Qp_I;               // Поперечная сила от пост. нагрузок в сечении I-I, кН
    public double Qp_II;              // Поперечная сила от пост. нагрузок в сечении II-II, кН
    public double Qp_III;             // Поперечная сила от пост. нагрузок в сечении III-III, кН
    public double k_shear_monolithic; // Допускаемая нагрузка по поперечной силе (монолитный), кН/м
    public double K_shear_monolithic; // Класс по поперечной силе (монолитный)
    public double k_shear_external;   // Допускаемая нагрузка по поперечной силе (консоль), кН/м
    public double K_shear_external;   // Класс по поперечной силе (консоль)
    public double etaQ;               // Коэффициент неравномерности для поперечной силы

    // === ГЛАВНАЯ БАЛКА (п. 7.2.5-7.2.6) ===
    public double beamHeight;           // Высота балки h, м
    public double beamWidth;            // Ширина ребра балки b, м
    public double bf;                   // Расчетная ширина плиты bf, м
    public double hf;                   // Приведенная толщина плиты hf, м
    public double As_beam_tensile;      // Площадь растянутой арматуры балки, м²
    public double As_beam_compressive;  // Площадь сжатой арматуры балки, м²
    public double as_beam_tensile;      // Расстояние до центра растянутой арматуры, м
    public double as_beam_compressive;  // Расстояние до центра сжатой арматуры, м

    // Результаты расчета балки
    public double M_pred_beam;          // Предельный момент балки, кН·м
    public double Mp_beam;              // Момент от пост. нагрузок, кН·м
    public Double Omega_M;              // Площадь линии влияния момента, м²
    public double k_beam_moment;        // Допускаемая нагрузка по моменту, кН/м
    public double K_beam_moment;        // Класс балки по моменту
    public double kc_beam;              // Эталонная нагрузка для балки, кН/м

    // === ГЛАВНАЯ БАЛКА: ПОПЕРЕЧНАЯ СИЛА (п. 7.2.7-7.2.8) ===
    public double Asw;            // Площадь поперечного сечения одной ветви хомутов, м²
    public double s_stirrups;     // Шаг хомутов s, м
    public double sum_Asi;        // Сумма площадей отогнутых стержней, м²
    public double alpha_bent;     // Угол наклона отогнутых стержней, град
    public Double Omega_k;        // Площадь линии влияния поперечной силы (временная), м²
    public double Omega_p;        // Площадь линии влияния поперечной силы (постоянная), м²

    // Результаты расчета
    public double Q_ultimate;     // Предельная поперечная сила, кН
    public double Q_p_shear;      // Поперечная сила от постоянных нагрузок, кН
    public double k_beam_shear;   // Допускаемая нагрузка по поперечной силе, кН/м
    public double K_beam_shear;   // Класс балки по поперечной силе

    // === ВЫНОСЛИВОСТЬ ПЛИТЫ (п. 7.3.1) ===
    public double x_prime_slab;           // Высота сжатой зоны для выносливости, м
    public double I_red_slab;             // Приведенный момент инерции, м⁴
    public double rho_b_slab;             // Асимметрия цикла для бетона
    public double rho_s_slab;             // Асимметрия цикла для арматуры
    public double Rbf;                    // Расчетное сопротивление бетона на выносливость, МПа
    public double Rsf;                    // Расчетное сопротивление арматуры на выносливость, МПа
    public double Theta_slab;             // Коэффициент уменьшения динамики для плиты
    public double k_fatigue_slab_concrete; // Допускаемая нагрузка по выносливости бетона, кН/м
    public double K_fatigue_slab_concrete; // Класс по выносливости бетона
    public double k_fatigue_slab_rebar;    // Допускаемая нагрузка по выносливости арматуры, кН/м
    public double K_fatigue_slab_rebar;    // Класс по выносливости арматуры

    // === ВЫНОСЛИВОСТЬ ГЛАВНОЙ БАЛКИ (п. 7.3.2) ===
    public double x_prime_beam;           // Высота сжатой зоны для выносливости, м
    public double I_red_beam;             // Приведенный момент инерции, м
    public double rho_b_beam;             // Асимметрия цикла для бетона
    public double rho_s_beam;             // Асимметрия цикла для арматуры
    public double Rbf_beam;               // Расчетное сопротивление бетона на выносливость, МПа
    public double Rsf_beam;               // Расчетное сопротивление арматуры на выносливость, МПа
    public double Theta_beam;             // Коэффициент уменьшения динамики для балки
    public double k_fatigue_beam_concrete; // Допускаемая нагрузка по выносливости бетона, кН/м
    public double K_fatigue_beam_concrete; // Класс по выносливости бетона
    public double k_fatigue_beam_rebar;    // Допускаемая нагрузка по выносливости арматуры, кН/м
    public double K_fatigue_beam_rebar;    // Класс по выносливости арматуры
}