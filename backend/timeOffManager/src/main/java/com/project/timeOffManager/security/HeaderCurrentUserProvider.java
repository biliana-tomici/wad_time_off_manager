package com.project.timeOffManager.security;

import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Profile({"local", "test"})
public class HeaderCurrentUserProvider implements CurrentUserProvider {

    public static final String USER_ID_HEADER = "X-User-Id";

    private final HttpServletRequest request;
    private final UserRepository userRepository;

    public HeaderCurrentUserProvider(HttpServletRequest request, UserRepository userRepository) {
        this.request = request;
        this.userRepository = userRepository;
    }

    @Override
    public Long getCurrentUserId() {
        if (request == null) {
            return null;
        }
        String headerValue = request.getHeader(USER_ID_HEADER);
        if (headerValue == null || headerValue.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(headerValue.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public Optional<User> getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(userRepository.findCustomerById(userId));
    }

    @Override
    public User requireCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new com.project.timeOffManager.exception.AccessDeniedException("Missing or invalid " + USER_ID_HEADER + " header");
        }
        User user = userRepository.findCustomerById(userId);
        if (user == null) {
            throw new com.project.timeOffManager.exception.AccessDeniedException("User not found for " + USER_ID_HEADER + ": " + userId);
        }
        return user;
    }
}
