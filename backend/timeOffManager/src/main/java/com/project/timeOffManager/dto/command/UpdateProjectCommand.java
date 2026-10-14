package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.PositiveOrZero;

/** Partial update of a project: {@code null} fields are left unchanged. */
public record UpdateProjectCommand(String name,
                                   @PositiveOrZero Long minEmp,
                                   Long managerId) {
}
