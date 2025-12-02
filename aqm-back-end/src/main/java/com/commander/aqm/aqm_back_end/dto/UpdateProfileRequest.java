// src/main/java/com/commander/aqm/aqm_back_end/dto/UpdateProfileRequest.java
package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String email;
    private Boolean emailAlertsEnabled; // ✅ CRITICAL FIELD
}