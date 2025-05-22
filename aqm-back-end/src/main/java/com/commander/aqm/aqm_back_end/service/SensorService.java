package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.Sensor;

import java.util.List;

public interface SensorService {
    List<Sensor> getAll();
    Sensor getById(Long id);
    Sensor save(Sensor sensor);
}
