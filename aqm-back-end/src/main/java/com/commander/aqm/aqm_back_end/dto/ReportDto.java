package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ReportDto {
    private Long locationId;
    private String locationName;

    private LocalDateTime from;
    private LocalDateTime to;

    private Double avgPm25;
    private Double avgPm10;
    private Double avgAqi;
}
