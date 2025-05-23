package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AirQualityDataDto {
    private Long id;
    private Float pm25;
    private Float pm10;
    private Float aqi;
    private LocalDateTime timestampUtc;

    public static AirQualityDataDto from(AirQualityData d) {
        AirQualityDataDto dto = new AirQualityDataDto();
        dto.setId(d.getId());
        dto.setPm25(d.getPm25());
        dto.setPm10(d.getPm10());
        dto.setAqi(d.getAqi() != null ? Float.valueOf(d.getAqi()) : null);
        dto.setTimestampUtc(d.getTimestampUtc());
        return dto;
    }
}
