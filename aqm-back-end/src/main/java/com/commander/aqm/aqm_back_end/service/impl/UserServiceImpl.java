package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.exception.AuthException;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(User user) {
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepo.save(user);
    }

    @Override
    public User authenticate(String username, String rawPassword) {
        return userRepo.findByUsername(username)
                .filter(user -> passwordEncoder.matches(rawPassword, user.getPasswordHash()))
                .orElseThrow(() -> new AuthException("Invalid username or password"));
    }

    @Override
    public User getByUsername(String username) {
        return userRepo.findByUsername(username)
                .orElseThrow(() -> new AuthException("User not found"));
    }
}
