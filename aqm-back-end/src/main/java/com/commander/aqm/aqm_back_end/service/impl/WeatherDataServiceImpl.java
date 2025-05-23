package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.WeatherData;
import com.commander.aqm.aqm_back_end.repository.WeatherDataRepository;
import com.commander.aqm.aqm_back_end.service.WeatherDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WeatherDataServiceImpl implements WeatherDataService {

    private final WeatherDataRepository weatherRepo;

    @Override
    public List<WeatherData> getAll() {
        return weatherRepo.findAll();
    }

    @Override
    public WeatherData getById(Long id) {
        return weatherRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Weather data not found"));
    }

    @Override
    public WeatherData save(WeatherData data) {
        return weatherRepo.save(data);
    }

    @Override
    public List<WeatherData> getWeatherByLocation(Long locationId) {
        return weatherRepo.findByLocationId(locationId);
    }
}
