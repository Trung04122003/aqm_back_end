package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForecastRepository extends JpaRepository<Forecast, Long> {
    List<Forecast> findByLocationIdOrderByTimestampUtcAsc(Long locationId);
}
