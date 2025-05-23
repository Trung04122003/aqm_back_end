package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Forecast;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ForecastDto {
    private Float predictedPm25;
    private Float predictedPm10;
    private Float predictedAqi;
    private LocalDateTime timestampUtc;
    private String modelVersion;

    public static ForecastDto from(Forecast f) {
        ForecastDto dto = new ForecastDto();
        dto.setPredictedPm25(f.getPredictedPm25());
        dto.setPredictedPm10(f.getPredictedPm10());
        dto.setPredictedAqi(f.getPredictedAqi());
        dto.setTimestampUtc(f.getTimestampUtc());
        dto.setModelVersion(f.getModelVersion());
        return dto;
    }
}
