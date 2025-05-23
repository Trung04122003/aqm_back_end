package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {

    List<WeatherData> findByLocationId(Long locationId);
}
