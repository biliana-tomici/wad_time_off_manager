package com.project.timeOffManager.dto.command;

import com.project.timeOffManager.enums.Role;
import jakarta.validation.constraints.NotBlank;

/** {@code role} defaults to EMPLOYEE and {@code managerId} is optional. */
public record CreateUserCommand(@NotBlank String firstName,
                                @NotBlank String lastName,
                                Role role,
                                Long managerId) {
}
