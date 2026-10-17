package com.project.timeOffManager.dto.command;

import com.project.timeOffManager.enums.Role;

/** Partial update of a user: {@code null} fields are left unchanged. */
public record UpdateUserCommand(String firstName,
                                String lastName,
                                Role role,
                                Long managerId,
                                Boolean clearManager) {

    public UpdateUserCommand(String firstName, String lastName, Role role, Long managerId) {
        this(firstName, lastName, role, managerId, false);
    }
}
