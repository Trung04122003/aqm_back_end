// src/main/java/com/commander/aqm/aqm_back_end/repository/AlertRepository.java
package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.Alert;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {

    /**
     * Get all alerts for a user, ordered by newest first
     */
    List<Alert> findByUserOrderByTriggeredAtDesc(User user);

    /**
     * Get unread alerts for a user
     */
    List<Alert> findByUserAndIsReadFalseOrderByTriggeredAtDesc(User user);

    /**
     * ✅ NEW: Find recent alerts for specific pollutant (anti-spam)
     */
    List<Alert> findByUserAndPollutantAndTriggeredAtAfter(
            User user,
            String pollutant,
            LocalDateTime after
    );

    /**
     * ✅ NEW: Count unread alerts for user
     */
    long countByUserAndIsReadFalse(User user);

    /**
     * ✅ NEW: Get alerts by location
     */
    @Query("SELECT a FROM Alert a WHERE a.aqData.location.id = :locationId ORDER BY a.triggeredAt DESC")
    List<Alert> findByLocationId(Long locationId);

    /**
     * ✅ NEW: Get recent alerts (last 24h) for all users
     */
    @Query("SELECT a FROM Alert a WHERE a.triggeredAt > :after ORDER BY a.triggeredAt DESC")
    List<Alert> findRecentAlerts(LocalDateTime after);

    /**
     * ✅ NEW: Delete old read alerts (cleanup)
     */
    @Query("DELETE FROM Alert a WHERE a.isRead = true AND a.triggeredAt < :before")
    void deleteOldReadAlerts(LocalDateTime before);
}