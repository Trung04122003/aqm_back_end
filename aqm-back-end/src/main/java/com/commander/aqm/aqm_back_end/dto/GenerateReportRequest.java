package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;

@Data
public class GenerateReportRequest {
    private Long locationId;
    private String fromDate; // yyyy-MM-dd
    private String toDate;   // yyyy-MM-dd
}