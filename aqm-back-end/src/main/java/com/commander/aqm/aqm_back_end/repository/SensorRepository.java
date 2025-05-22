package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorRepository extends JpaRepository<Sensor, Long> {
}
