package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProjectCommand(@NotBlank String name,
                                   @NotNull @PositiveOrZero Long minEmp,
                                   @NotNull Long managerId) {
}
