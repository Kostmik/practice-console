package org.example.dto.slab;

public record LoadsData(
        Double gammaReinforcedConcrete,
        Double gammaBallastWithTrack,
        Double ppSlab,
        Double pbSlab,
        Double np,
        Double npPrime,
        Double nk,
        Double dynamicCoeffSlab
) {}