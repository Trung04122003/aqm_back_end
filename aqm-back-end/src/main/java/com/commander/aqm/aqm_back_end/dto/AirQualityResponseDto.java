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
 * 📊 Response DTO for Dashboard Air Quality Data
 * Includes current reading + 24h history
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AirQualityResponseDto {

    private CurrentDataDto current;
    private List<HistoryDataDto> history;

    /**
     * 🏭 Factory method to create from database entities
     */
    public static AirQualityResponseDto from(List<AirQualityData> dataList) {
        if (dataList == null || dataList.isEmpty()) {
            return AirQualityResponseDto.builder()
                    .current(null)
                    .history(List.of())
                    .build();
        }

        // Sort by timestamp descending (newest first)
        List<AirQualityData> sorted = dataList.stream()
                .sorted((a, b) -> b.getTimestampUtc().compareTo(a.getTimestampUtc()))
                .collect(Collectors.toList());

        // Current = most recent reading
        AirQualityData latest = sorted.get(0);

        // History = all readings
        List<HistoryDataDto> historyList = sorted.stream()
                .map(HistoryDataDto::from)
                .collect(Collectors.toList());

        return AirQualityResponseDto.builder()
                .current(CurrentDataDto.from(latest))
                .history(historyList)
                .build();
    }

    // ==================== NESTED DTOs ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentDataDto {
        private Long id;
        private Long locationId;
        private Float pm25;
        private Float pm10;
        private Integer aqi;
        private Float no2;
        private Float so2;
        private Float co;
        private Float o3;
        private String timestampUtc;

        public static CurrentDataDto from(AirQualityData data) {
            if (data == null) return null;

            return CurrentDataDto.builder()
                    .id(data.getId())
                    .locationId(data.getLocation().getId())
                    .pm25(data.getPm25())
                    .pm10(data.getPm10())
                    .aqi(data.getAqi())
                    .no2(data.getNO2())
                    .so2(data.getSo2())
                    .co(data.getCo())
                    .o3(data.getO3())
                    .timestampUtc(data.getTimestampUtc().toString())
                    .build();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HistoryDataDto {
        private Long id;
        private Long locationId;
        private Float pm25;
        private Float pm10;
        private Integer aqi;
        private String timestampUtc;

        public static HistoryDataDto from(AirQualityData data) {
            return HistoryDataDto.builder()
                    .id(data.getId())
                    .locationId(data.getLocation().getId())
                    .pm25(data.getPm25())
                    .pm10(data.getPm10())
                    .aqi(data.getAqi())
                    .timestampUtc(data.getTimestampUtc().toString())
                    .build();
        }
    }
}