package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // ✅ EXISTING: Find by username
    Optional<User> findByUsername(String username);

    // ✅ EXISTING: Find by email
    Optional<User> findByEmail(String email);

    // ✅ NEW: Find by username OR email (CRITICAL for login)
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);

    // ✅ EXISTING: Check if username exists
    boolean existsByUsername(String username);

    // ✅ EXISTING: Check if email exists
    boolean existsByEmail(String email);
}