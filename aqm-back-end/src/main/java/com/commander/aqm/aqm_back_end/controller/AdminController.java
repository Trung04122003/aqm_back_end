package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.SensorDto;
import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.service.SensorService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// aqm-back-end/src/main/java/.../controller/AdminController.java
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final SensorService sensorService;

    @GetMapping("/sensors")
    @PreAuthorize("hasRole('ADMIN')")
    public List<SensorDto> getAllSensors() {
        return sensorService.getAll().stream()
                .map(SensorDto::from)
                .toList();
    }

    @PostMapping("/sensors")
    @PreAuthorize("hasRole('ADMIN')")
    public Sensor createSensor(@RequestBody SensorDto dto) {
        return sensorService.save(dto.toEntity());
    }

    @PutMapping("/sensors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Sensor updateSensor(@PathVariable Long id, @RequestBody SensorDto dto) {
        Sensor sensor = dto.toEntity();
        sensor.setId(id);
        return sensorService.save(sensor);
    }

    @DeleteMapping("/sensors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSensor(@PathVariable Long id) {
        sensorService.delete(id);
    }
}