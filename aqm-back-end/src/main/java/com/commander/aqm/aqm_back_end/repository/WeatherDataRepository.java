// src/main/java/com/commander/aqm/aqm_back_end/repository/WeatherDataRepository.java
package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    /**
     * Find all weather data for a location
     */
    List<WeatherData> findByLocationId(Long locationId);

    /**
     * ✅ NEW: Get latest weather for location
     */
    Optional<WeatherData> findTopByLocationIdOrderByTimestampUtcDesc(Long locationId);

    /**
     * ✅ NEW: Get weather data within time range
     */
    List<WeatherData> findByLocationIdAndTimestampUtcAfter(Long locationId, LocalDateTime after);

    /**
     * ✅ NEW: Get latest N records for location
     */
    List<WeatherData> findTop10ByLocationIdOrderByTimestampUtcDesc(Long locationId);
}