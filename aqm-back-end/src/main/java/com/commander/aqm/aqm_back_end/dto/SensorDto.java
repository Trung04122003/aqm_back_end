package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Sensor;
import com.commander.aqm.aqm_back_end.model.SensorStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SensorDto {

    private Long id;  // ✅ THÊM ID FIELD

    @NotBlank
    private String serialNumber;

    @NotBlank
    private String sensorType;

    private String model;

    @NotNull
    private Long locationId;

    private LocalDate installationDate;  // ✅ THÊM INSTALLATION DATE

    private SensorStatus status = SensorStatus.ACTIVE;

    public static SensorDto from(Sensor s) {
        SensorDto dto = new SensorDto();
        dto.setId(s.getId());
        dto.setSerialNumber(s.getSerialNumber());
        dto.setModel(s.getModel());
        dto.setSensorType(s.getSensorType());
        dto.setStatus(s.getStatus());
        dto.setInstallationDate(s.getInstallationDate());
        dto.setLocationId(s.getLocation().getId());  // ⚠️ Add field in DTO
        return dto;
    }

    private void setInstallationDate(LocalDate installationDate) {

    }

    private void setId(Long id) {
    }

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
