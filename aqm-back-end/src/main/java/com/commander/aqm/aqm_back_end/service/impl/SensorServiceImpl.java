package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.repository.SensorRepository;
import com.commander.aqm.aqm_back_end.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepo;

    @Override
    public List<Sensor> getAll() {
        return sensorRepo.findAll();
    }

    @Override
    public Sensor getById(Long id) {
        return sensorRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Sensor not found"));
    }

    @Override
    public Sensor save(Sensor sensor) {
        return sensorRepo.save(sensor);
    }
}
