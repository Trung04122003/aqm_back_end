package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.ForecastDto;
import com.commander.aqm.aqm_back_end.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/forecast")
@RequiredArgsConstructor
public class ForecastController {

    private final ForecastService forecastService;

    @GetMapping
    public List<ForecastDto> getForecast(@RequestParam("location") Long locationId) {
        return forecastService.getForecastByLocation(locationId).stream()
                .map(ForecastDto::from)
                .toList();
    }
}
