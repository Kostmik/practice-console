package org.example.dto.inspection;

import java.util.List;

public record Inspection155Request(
        List<Double> deflections,
        List<Double> inertias,
        int targetBeamIndex
) {}
