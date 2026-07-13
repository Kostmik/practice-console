package org.example.calculator.paragraph_10;

/**
 * Результаты расчёта продольного борта (Раздел 10).
 * Все допускаемые нагрузки k — в кН/м; моменты — в кН·м; силы — в кН.
 */
public class BoardResult {

    // Промежуточные величины
    public double sigmaBmax;    // σ_b,max — горизонтальное давление балласта, кН/м² (ф. 10.8)
    public double Mp;           // момент от постоянных нагрузок, кН·м (ф. 10.7)
    public double Qp;           // поперечная сила от постоянных нагрузок, кН (ф. 10.10)
    public double Mlimit;       // предельный изгибающий момент M, кН·м (ф. 7.11/7.14)
    public double Qlimit;       // предельная поперечная сила Q, кН (ф. 7.17)

    // Выносливость
    public double Rbf;          // сопротивление бетона на выносливость, МПа (ф. 5.1)
    public double Rsf;          // сопротивление арматуры на выносливость, МПа (ф. 5.2)
    public double xFatigue;     // высота сжатой зоны x', м (ф. 7.39)
    public double Ired;         // момент инерции приведённого сечения, м⁴ (ф. 7.38)

    // Допускаемые временные нагрузки
    public double kMoment;      // по прочности на изгиб (ф. 10.1), кН/м
    public double kShear;       // по прочности на поперечную силу (ф. 10.9), кН/м
    public double kFatigueConcrete; // по выносливости бетона (ф. 10.11), кН/м
    public double kFatigueRebar;    // по выносливости арматуры (ф. 10.12), кН/м

    // Итог
    public double kGoverning;   // минимальная (определяющая) допускаемая нагрузка, кН/м
    public String governingCase; // какой расчёт оказался определяющим
    public Double classK;       // класс борта K (ф. 4.1), null если не считался

    public double minAllowable() {
        double m = Math.min(kMoment, kShear);
        m = Math.min(m, kFatigueConcrete);
        m = Math.min(m, kFatigueRebar);
        return m;
    }
}