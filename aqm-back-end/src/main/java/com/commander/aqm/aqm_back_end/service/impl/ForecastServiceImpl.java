package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.Forecast;
import com.commander.aqm.aqm_back_end.repository.ForecastRepository;
import com.commander.aqm.aqm_back_end.service.ForecastService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ForecastServiceImpl implements ForecastService {

    private final ForecastRepository repo;

    @Override
    public List<Forecast> getForecastByLocation(Long locationId) {
        return repo.findByLocationIdOrderByTimestampUtcAsc(locationId);
    }
}
