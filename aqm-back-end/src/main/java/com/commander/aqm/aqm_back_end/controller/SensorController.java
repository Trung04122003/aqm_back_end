package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.SensorDto;
import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.service.SensorService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
@Tag(name = "Sensor APIs", description = "Manage sensor devices and info")
@RequiredArgsConstructor
public class SensorController {

    private final SensorService sensorService;

    @GetMapping
    public List<SensorDto> getAllSensors() {
        return sensorService.getAll().stream()
                .map(SensorDto::from)
                .toList();
    }

    @PostMapping
    public Sensor addSensor(@Valid @RequestBody SensorDto dto) {
        Sensor sensor = dto.toEntity();
        return sensorService.save(sensor);
    }
}
