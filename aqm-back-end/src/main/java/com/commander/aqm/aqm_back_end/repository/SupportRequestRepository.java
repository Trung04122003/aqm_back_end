package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.RequestStatus;
import com.commander.aqm.aqm_back_end.model.SupportRequest;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {

    // ✅ EXISTING: Find tickets by user
    List<SupportRequest> findByUser(User user);

    // ✅ NEW: Count by status (for admin statistics)
    long countByStatus(RequestStatus status);

    // ✅ NEW: Find by status
    List<SupportRequest> findByStatus(RequestStatus status);

    // ✅ NEW: Find all tickets ordered by submission date (newest first)
    List<SupportRequest> findAllByOrderBySubmittedAtDesc();

    // ✅ NEW: Find unresolved tickets (PENDING or IN_PROGRESS)
    @Query("SELECT s FROM SupportRequest s WHERE s.status != 'RESOLVED' ORDER BY s.submittedAt DESC")
    List<SupportRequest> findUnresolvedTickets();

    // ✅ NEW: Find user's recent tickets (last 10)
    List<SupportRequest> findTop10ByUserOrderBySubmittedAtDesc(User user);
}