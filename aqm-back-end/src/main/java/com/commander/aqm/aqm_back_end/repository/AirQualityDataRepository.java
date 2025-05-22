package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AirQualityDataRepository extends JpaRepository<AirQualityData, Long> {
}
