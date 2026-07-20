package org.example.controller;

import org.example.calculator.paragraph_14.CarbonRecommendationTables.StrengtheningScheme;
import org.example.dto.carbonRecs.CarbonRecsResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carbonRecs")
@CrossOrigin(origins = "*")
public class CarbonRecsController {

    @GetMapping("/schemes")
    public CarbonRecsResponse getSchemes() {
        StrengtheningScheme[] allSchemes = StrengtheningScheme.values();
        List<CarbonRecsResponse.SchemeDto> schemes = Arrays.stream(allSchemes)
            .map(s -> new CarbonRecsResponse.SchemeDto(
                s.ordinal() + 1,
                s.getDescription().trim(),
                s.getIncreasePercent()
            ))
            .toList();

        return new CarbonRecsResponse(schemes, null, null);
    }
}