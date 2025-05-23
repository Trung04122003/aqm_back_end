package com.commander.aqm.aqm_back_end.service.impl;

import com.commander.aqm.aqm_back_end.model.*;
import com.commander.aqm.aqm_back_end.model.RequestStatus;
import com.commander.aqm.aqm_back_end.repository.SupportRequestRepository;
import com.commander.aqm.aqm_back_end.service.SupportRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SupportRequestServiceImpl implements SupportRequestService {

    private final SupportRequestRepository repo;

    @Override
    public SupportRequest submitRequest(User user, String subject, String message) {
        return repo.save(SupportRequest.builder()
                .user(user)
                .subject(subject)
                .message(message)
                .submittedAt(LocalDateTime.now())
                .status(RequestStatus.PENDING)
                .build());
    }

    @Override
    public List<SupportRequest> getMyRequests(User user) {
        return repo.findByUser(user);
    }
}
