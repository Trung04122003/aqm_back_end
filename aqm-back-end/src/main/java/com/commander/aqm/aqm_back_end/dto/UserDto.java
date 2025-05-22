package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        return dto;
    }
}
