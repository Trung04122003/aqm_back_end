package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.WeatherData;

import java.util.List;

public interface WeatherDataService {
    List<WeatherData> getAll();
    WeatherData getById(Long id);
    WeatherData save(WeatherData data);

    List<WeatherData> getWeatherByLocation(Long locationId);
}
