package org.example.controller;

import org.example.calculator.paragraph_14.CarbonRecsTables.StrengtheningScheme;
import org.example.dto.carbonRecs.SchemeDto;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/carbonRecs")
@CrossOrigin(origins = "*")
public class CarbonRecsController {

    @GetMapping("/schemes")
    public List<SchemeDto> getSchemes() {
        return Arrays.stream(StrengtheningScheme.values())
            .map(scheme -> new SchemeDto(
                scheme.ordinal() + 1,
                scheme.getDescription().trim(),
                scheme.getIncreasePercent()
            ))
            .toList();
    }
}