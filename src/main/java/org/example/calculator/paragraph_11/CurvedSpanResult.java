package org.example.calculator.paragraph_11;

/**
 * Результаты расчёта пролётного строения на кривой (Раздел 11).
 */
public class CurvedSpanResult {

    // Угол наклона (возвышение наружного рельса), ф. 11.14
    public double sinAlpha;
    public double cosAlpha;

    // Геометрия и моменты инерции сечения
    public double c1;       // ф. 11.9
    public double H;        // ф. 11.9
    public double IyPrime10; // I_y′ по ф. 11.10 (6 слагаемых) — используется в 11.8 для d
    public double IyPrime7;  // I_y′ по ф. 11.7  (4 слагаемых) — используется в члене кручения 11.2
    public double Ik;       // ф. 11.5 — момент инерции при кручении
    public double Iomega;   // ф. 11.12 — секториальный момент инерции
    public double d;        // ф. 11.8
    public double omega;    // ф. 11.6
    public double omegaD;   // ф. 11.13
    public double omegaK;   // ф. 11.13
    public double y;        // ф. 11.13
    public double m;        // ф. 11.11
    public double g;        // g = 0,5·m·l
    public double gamma;    // коэффициент по таблице 11.1

    // Смещение вертикальной нагрузки, ф. 11.2–11.4
    public double dlM_moving;    // Δl_M, поезд движется (11.2)
    public double dlQ_moving;    // Δl_Q, поезд движется (11.3)
    public double dl_standing;   // Δl_M = Δl_Q, поезд стоит (11.4)

    // Доли нагрузки ε (ф. 11.1). Балка 1 — знак «+», балка 2 — знак «−».
    // Поезд движется:
    public double epsM_beam1;
    public double epsQ_beam1;
    public double epsM_beam2;
    public double epsQ_beam2;
    // Поезд стоит:
    public double epsM_beam1_standing;
    public double epsQ_beam1_standing;
    public double epsM_beam2_standing;
    public double epsQ_beam2_standing;

    // Определяющие (наибольшие) доли на наиболее загруженную балку — для дальнейшего расчёта балки
    public double epsM_design;
    public double epsQ_design;
}