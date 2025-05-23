package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.PasswordResetToken;
import com.commander.aqm.aqm_back_end.model.User;
import com.commander.aqm.aqm_back_end.repository.PasswordResetTokenRepository;
import com.commander.aqm.aqm_back_end.repository.UserRepository;
import com.commander.aqm.aqm_back_end.service.PasswordResetService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final PasswordResetTokenRepository tokenRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;

    @Override
    public String createToken(User user) {
        tokenRepo.deleteByUser(user); // invalidate old tokens
        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiry(LocalDateTime.now().plusMinutes(15))
                .build();
        tokenRepo.save(resetToken);
        return token;
    }

    @Override
    public boolean validateAndResetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> opt = tokenRepo.findByToken(token);
        if (opt.isEmpty()) return false;

        PasswordResetToken reset = opt.get();
        if (reset.getExpiry().isBefore(LocalDateTime.now())) return false;

        User user = reset.getUser();
        user.setPasswordHash(encoder.encode(newPassword));
        userRepo.save(user);
        tokenRepo.delete(reset); // one-time use
        return true;
    }
}
