package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.WeatherData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WeatherDataDto {
    private Float temperatureC;
    private Float humidityPct;
    private Float pressureHpa;
    private Float windSpeedMps;
    private Integer windDirDeg;
    private LocalDateTime timestampUtc;

    public static WeatherDataDto from(WeatherData w) {
        WeatherDataDto dto = new WeatherDataDto();
        dto.setTemperatureC(w.getTemperatureC());
        dto.setHumidityPct(w.getHumidityPct());
        dto.setPressureHpa(w.getPressureHpa());
        dto.setWindSpeedMps(w.getWindSpeedMps());
        dto.setWindDirDeg(w.getWindDirDeg());
        dto.setTimestampUtc(w.getTimestampUtc());
        return dto;
    }
}
