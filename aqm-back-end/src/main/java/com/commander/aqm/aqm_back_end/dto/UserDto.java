// src/main/java/com/commander/aqm/aqm_back_end/dto/UserDto.java
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role;
    private Boolean emailAlertsEnabled; // ✅ ADD THIS

    public static UserDto from(User user) {
        if (user == null) return null;

        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .emailAlertsEnabled(user.getEmailAlertsEnabled()) // ✅ ADD THIS
                .build();
    }
}