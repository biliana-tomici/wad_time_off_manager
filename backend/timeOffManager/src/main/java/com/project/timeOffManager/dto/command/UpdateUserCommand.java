package com.project.timeOffManager.dto.command;

import com.project.timeOffManager.enums.Role;
import jakarta.validation.constraints.Email;

/** Partial update of a user: {@code null} fields are left unchanged. */
public record UpdateUserCommand(String firstName,
                                String lastName,
                                @Email(message = "Must be a valid email address") String email,
                                String password,
                                Role role,
                                Long managerId,
                                Boolean clearManager) {

    public UpdateUserCommand(String firstName, String lastName, Role role, Long managerId, Boolean clearManager) {
        this(firstName, lastName, null, null, role, managerId, clearManager);
    }

    public UpdateUserCommand(String firstName, String lastName, Role role, Long managerId) {
        this(firstName, lastName, null, null, role, managerId, false);
    }
}
