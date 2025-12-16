package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Forecast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForecastRepository extends JpaRepository<Forecast, Long> {

    List<Forecast> findByLocationIdOrderByTimestampUtcAsc(Long locationId);

    // ✅ NEW: Delete old forecasts when generating new ones
    void deleteByLocationId(Long locationId);
}