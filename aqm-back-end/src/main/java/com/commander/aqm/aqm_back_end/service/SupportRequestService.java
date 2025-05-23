package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.SupportRequest;
import com.commander.aqm.aqm_back_end.model.User;

import java.util.List;

public interface SupportRequestService {
    SupportRequest submitRequest(User user, String subject, String message);
    List<SupportRequest> getMyRequests(User user);
}
