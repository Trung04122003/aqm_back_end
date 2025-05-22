package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.AirQualityData;

import java.util.List;

public interface AirQualityDataService {
    List<AirQualityData> getAll();
    AirQualityData getById(Long id);
    AirQualityData save(AirQualityData data);
}
