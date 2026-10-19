package com.project.timeOffManager.security;

import com.project.timeOffManager.enums.Role;

public record AuthenticatedUser(Long id, String email, Role role) {
}
