package com.project.timeOffManager.security;

import com.project.timeOffManager.entity.User;

import java.util.Optional;

public interface CurrentUserProvider {
    Optional<User> getCurrentUser();
    Long getCurrentUserId();
    User requireCurrentUser();
}
