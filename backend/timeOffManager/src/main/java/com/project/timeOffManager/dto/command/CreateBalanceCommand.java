package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateBalanceCommand(@NotNull Long employeeId,
                                   @NotBlank String name,
                                   @NotNull @PositiveOrZero Long daysLeft,
                                   @NotNull @PositiveOrZero Long maxDays) {
}
