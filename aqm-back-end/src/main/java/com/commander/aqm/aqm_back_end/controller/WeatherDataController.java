package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.WeatherDataDto;
import com.commander.aqm.aqm_back_end.model.WeatherData;
import com.commander.aqm.aqm_back_end.service.WeatherDataService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
@Tag(name = "Weather APIs", description = "Weather data by location")
@RequiredArgsConstructor
public class WeatherDataController {

    private final WeatherDataService weatherDataService;

    // 🧠 Get weather data by location ID
    @GetMapping
    public List<WeatherDataDto> getWeatherByLocation(@RequestParam("location") Long locationId) {
        List<WeatherData> data = weatherDataService.getWeatherByLocation(locationId);
        return data.stream()
                .map(WeatherDataDto::from)
                .toList();
    }
}
