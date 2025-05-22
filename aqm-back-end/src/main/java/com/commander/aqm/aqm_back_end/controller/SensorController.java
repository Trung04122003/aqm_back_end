package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.SensorDto;
import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.service.SensorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @PostMapping
    public Sensor addSensor(@Valid @RequestBody SensorDto dto) {
        Sensor sensor = dto.toEntity();
        return sensorService.save(sensor);
    }
}
