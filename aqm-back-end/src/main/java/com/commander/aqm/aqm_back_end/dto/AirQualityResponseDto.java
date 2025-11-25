// src/main/java/com/commander/aqm/aqm_back_end/dto/AirQualityResponseDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.AirQualityData;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * ✅ Standardized Air Quality Response
 * Matches Frontend expectations exactly
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityResponseDto {

    private CurrentAQI current;
    private List<HistoryPoint> history;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentAQI {
        private Integer aqi;
        private Float pm25;
        private Float pm10;
        private Float no2;
        private Float co;
        private Float o3;
        private Float so2;
        private LocalDateTime timestamp;

        public static CurrentAQI from(AirQualityData data) {
            if (data == null) return null;

            return CurrentAQI.builder()
                    .aqi(data.getAqi())
                    .pm25(data.getPm25())
                    .pm10(data.getPm10())
                    .no2(data.getNo2())
                    .co(data.getCo())
                    .o3(data.getO3())
                    .so2(data.getSo2())
                    .timestamp(data.getTimestampUtc())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryPoint {
        private LocalDateTime ts;
        private Integer value; // AQI value

        public static HistoryPoint from(AirQualityData data) {
            if (data == null) return null;

            return HistoryPoint.builder()
                    .ts(data.getTimestampUtc())
                    .value(data.getAqi())
                    .build();
        }
    }

    // ✅ Factory method to create from data list
    public static AirQualityResponseDto from(List<AirQualityData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return AirQualityResponseDto.builder()
                    .current(null)
                    .history(List.of())
                    .build();
        }

        // Latest data point as current
        AirQualityData latest = dataList.get(dataList.size() - 1);

        // All data points as history
        List<HistoryPoint> history = dataList.stream()
                .map(HistoryPoint::from)
                .collect(Collectors.toList());

        return AirQualityResponseDto.builder()
                .current(CurrentAQI.from(latest))
                .history(history)
                .build();
    }
}