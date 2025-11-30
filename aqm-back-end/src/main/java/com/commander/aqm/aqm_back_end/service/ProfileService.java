// src/main/java/com/commander/aqm/aqm_back_end/service/ProfileService.java
package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.dto.ProfileUpdateRequest;
import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.User;

public interface ProfileService {
    UserDto updateProfile(User currentUser, ProfileUpdateRequest request);
    UserDto getProfile(User currentUser);
    void changePassword(User currentUser, String currentPassword, String newPassword);
}