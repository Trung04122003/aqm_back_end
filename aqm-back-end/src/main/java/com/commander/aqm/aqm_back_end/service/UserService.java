package com.commander.aqm.aqm_back_end.service;

import com.commander.aqm.aqm_back_end.model.User;

public interface UserService {
    User register(User user);
    User authenticate(String username, String password);
    User getByUsername(String username);
}
