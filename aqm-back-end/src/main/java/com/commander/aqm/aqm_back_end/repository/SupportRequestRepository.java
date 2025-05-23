package com.commander.aqm.aqm_back_end.repository;

import com.commander.aqm.aqm_back_end.model.SupportRequest;
import com.commander.aqm.aqm_back_end.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SupportRequestRepository extends JpaRepository<SupportRequest, Long> {
    List<SupportRequest> findByUser(User user);
}
