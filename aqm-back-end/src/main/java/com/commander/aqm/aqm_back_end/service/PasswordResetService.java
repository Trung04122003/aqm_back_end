package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.User;

public interface PasswordResetService {
    String createToken(User user);
    boolean validateAndResetPassword(String token, String newPassword);
}
