// src/main/java/com/commander/aqm/aqm_back_end/dto/AlertDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Alert;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AlertDto {
    private Long id;
    private String pollutant;
    private Float value;
    private String locationName; // ✅ Derived from aqData.location.name
    private LocalDateTime triggeredAt;
    private Boolean isRead;
    private String status;

    // ✅ Static factory method to map from Entity
    public static AlertDto from(Alert alert) {
        if (alert == null) return null;

        AlertDto dto = new AlertDto();
        dto.setId(alert.getId());
        dto.setPollutant(alert.getPollutant());
        dto.setValue(alert.getValue());
        dto.setTriggeredAt(alert.getTriggeredAt());
        dto.setIsRead(alert.getIsRead());
        dto.setStatus(alert.getStatus() != null ? alert.getStatus().name() : "SENT");

        // ✅ Get location name from aqData relationship
        if (alert.getAqData() != null && alert.getAqData().getLocation() != null) {
            dto.setLocationName(alert.getAqData().getLocation().getName());
        } else {
            dto.setLocationName("Unknown Location");
        }

        return dto;
    }
}