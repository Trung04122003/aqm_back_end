package com.commander.aqm.aqm_back_end.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Username or email is required")
    private String usernameOrEmail;  // ✅ CHANGED: accept both username or email

    @NotBlank(message = "Password is required")  // ✅ THÊM
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
