package com.project.timeOffManager.dto.command;

import com.project.timeOffManager.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/** {@code role} defaults to EMPLOYEE and {@code managerId} is optional. */
public record CreateUserCommand(@NotBlank String firstName,
                                @NotBlank String lastName,
                                @Email(message = "Must be a valid email address") String email,
                                String password,
                                Role role,
                                Long managerId) {

    public CreateUserCommand(String firstName, String lastName, Role role, Long managerId) {
        this(firstName, lastName, null, null, role, managerId);
    }
}
