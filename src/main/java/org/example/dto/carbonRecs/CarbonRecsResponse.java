package org.example.dto.carbonRecs;

import java.util.List;

public record CarbonRecsResponse(
    List<SchemeDto> schemes,
    SchemeDto recommendedScheme,
    String detailedReport
) {
    public record SchemeDto(
        int number,
        String description,
        double increasePercent
    ) {}
}