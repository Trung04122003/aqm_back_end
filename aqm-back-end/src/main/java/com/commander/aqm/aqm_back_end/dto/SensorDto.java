package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.model.SensorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SensorDto {

    @NotBlank
    private String serialNumber;

    @NotBlank
    private String sensorType;

    private String model;

    @NotNull
    private Long locationId;

    private SensorStatus status = SensorStatus.ACTIVE;

    public Sensor toEntity() {
        Sensor s = new Sensor();
        s.setSerialNumber(serialNumber);
        s.setSensorType(sensorType);
        s.setModel(model);
        s.setStatus(status);
        // ❗ Inject location from service in controller before saving
        return s;
    }
}
