package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Forecast;

import java.util.List;

public interface ForecastService {
    List<Forecast> getForecastByLocation(Long locationId);
}
