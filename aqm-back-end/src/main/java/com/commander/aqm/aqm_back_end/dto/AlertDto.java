// src/main/java/com/commander/aqm/aqm_back_end/dto/AlertDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Alert;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 🔔 Alert Data Transfer Object
 * Enhanced with location information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertDto {

    private Long id;
    private String pollutant;
    private Float value;
    private String locationName;      // ✅ Location name for display
    private Long locationId;          // ✅ Location ID
    private String triggeredAt;
    private boolean isRead;
    private String status;

    // Additional info
    private Integer aqi;
    private Float pm25;
    private Float pm10;

    /**
     * 🏭 Convert Alert entity to DTO
     */
    public static AlertDto from(Alert alert) {
        if (alert == null) return null;

        return AlertDto.builder()
                .id(alert.getId())
                .pollutant(alert.getPollutant())
                .value(alert.getValue())
                .locationName(alert.getAqData() != null && alert.getAqData().getLocation() != null
                        ? alert.getAqData().getLocation().getName()
                        : "Unknown")
                .locationId(alert.getAqData() != null && alert.getAqData().getLocation() != null
                        ? alert.getAqData().getLocation().getId()
                        : null)
                .triggeredAt(alert.getTriggeredAt() != null
                        ? alert.getTriggeredAt().toString()
                        : LocalDateTime.now().toString())
                .isRead(alert.getIsRead() != null ? alert.getIsRead() : false)
                .status(alert.getStatus() != null ? alert.getStatus().name() : "SENT")
                .aqi(alert.getAqData() != null ? alert.getAqData().getAqi() : null)
                .pm25(alert.getAqData() != null ? alert.getAqData().getPm25() : null)
                .pm10(alert.getAqData() != null ? alert.getAqData().getPm10() : null)
                .build();
    }
}