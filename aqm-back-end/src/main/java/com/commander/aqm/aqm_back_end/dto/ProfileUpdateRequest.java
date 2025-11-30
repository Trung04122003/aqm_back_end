// src/main/java/com/commander/aqm/aqm_back_end/dto/ProfileUpdateRequest.java
package com.commander.aqm.aqm_back_end.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProfileUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be 2-100 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    // Optional fields
    private String phone;

    // Password change (optional)
    @Size(min = 6, message = "New password must be at least 6 characters")
    private String newPassword;

    @Size(min = 6, message = "Current password must be at least 6 characters")
    private String currentPassword; // Required if changing password
}