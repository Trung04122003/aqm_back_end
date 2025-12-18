// src/main/java/com/commander/aqm/aqm_back_end/dto/ExplainAQIRequest.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * DTO for requesting AI explanation of current AQI
 * Used in: Dashboard.tsx - "AI Explain" button
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExplainAQIRequest {

    private Long locationId;

    private Integer aqi;

    private Float pm25;

    private Float pm10;

    private Float no2;

    private Float so2;

    private Float co;

    private Float o3;

    private String locationName;

    private String timestamp;
}