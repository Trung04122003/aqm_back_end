package com.commander.aqm.aqm_back_end.controller;

import com.commander.aqm.aqm_back_end.dto.UserDto;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@Tag(name = "User APIs", description = "Endpoints for user profile and identity")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Get current logged-in user")
    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal UserDetails principal) {
        User user = userService.getByUsername(principal.getUsername());
        return UserDto.from(user);
    }
}
