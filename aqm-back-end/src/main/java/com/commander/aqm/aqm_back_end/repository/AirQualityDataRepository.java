package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

//public interface AirQualityDataRepository extends JpaRepository<AirQualityData, Long> {
//    List<AirQualityData> findByLocationIdAndTimestampUtcBetween(Long locationId, LocalDateTime from, LocalDateTime to);
//}

public interface AirQualityDataRepository extends JpaRepository<AirQualityData, Long> {
    List<AirQualityData> findByLocationIdAndTimestampUtcAfter(Long locationId, LocalDateTime after);

    List<AirQualityData> findByLocationIdAndTimestampUtcBetween(Long locationId, LocalDateTime from, LocalDateTime to);

    // ✅ ADD THIS NEW METHOD
    Optional<AirQualityData> findTopByLocationIdOrderByTimestampUtcDesc(Long locationId);
}