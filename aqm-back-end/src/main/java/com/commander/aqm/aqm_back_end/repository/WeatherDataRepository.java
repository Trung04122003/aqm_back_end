package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.WeatherData;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherDataRepository extends JpaRepository<WeatherData, Long> {
}
