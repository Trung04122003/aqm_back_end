package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.PasswordResetToken;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
