package org.example.dto.loads;

public record ShareResponse(
        Double epsilonM_Beam1,          // Доля на балку 1 по моменту
        Double epsilonQ_Beam1,          // Доля на балку 1 по силе
        Double epsilonM_Beam2,          // Доля на балку 2 по моменту
        Double epsilonQ_Beam2           // Доля на балку 2 по силе
) {}