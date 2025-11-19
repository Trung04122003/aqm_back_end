package com.commander.aqm.aqm_back_end.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;   // login bằng username là OK trước đã
    private String password;
}
