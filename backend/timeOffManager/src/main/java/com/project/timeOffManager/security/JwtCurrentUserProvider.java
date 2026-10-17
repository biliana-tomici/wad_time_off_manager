package com.project.timeOffManager.security;

import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.exception.AccessDeniedException;
import com.project.timeOffManager.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class JwtCurrentUserProvider implements CurrentUserProvider {

    private final UserRepository userRepository;

    public JwtCurrentUserProvider(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof AuthenticatedUser authenticatedUser) {
            return authenticatedUser.id();
        }
        if (principal instanceof String subject) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Optional<User> getCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return Optional.empty();
        }
        User user = userRepository.findCustomerById(userId);
        if (user == null || !user.isActive()) {
            return Optional.empty();
        }
        return Optional.of(user);
    }

    @Override
    public User requireCurrentUser() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            throw new AccessDeniedException("Authentication is required");
        }
        User user = userRepository.findCustomerById(userId);
        if (user == null || !user.isActive()) {
            throw new AccessDeniedException("Authenticated user not found or inactive");
        }
        return user;
    }
}
