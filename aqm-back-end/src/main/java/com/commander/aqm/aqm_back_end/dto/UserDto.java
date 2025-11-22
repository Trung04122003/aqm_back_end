// aqm-back-end/src/main/java/.../dto/UserDto.java (UPDATED)
package com.commander.aqm.aqm_back_end.dto;

import com.commander.aqm.aqm_back_end.model.Role;
import com.commander.aqm.aqm_back_end.model.Status;
import com.commander.aqm.aqm_back_end.model.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String role; // ✅ ADD role field
    private String status; // ✅ ADD status field
    private LocalDateTime createdAt; // ✅ ADD createdAt

    public static UserDto from(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFullName(user.getFullName());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null); // ✅ Convert enum to string
        dto.setStatus(user.getStatus() != null ? user.getStatus().name() : null);
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }
}