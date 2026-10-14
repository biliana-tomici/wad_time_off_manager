package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.PositiveOrZero;

/** Partial update of a balance: {@code null} fields are left unchanged. */
public record UpdateBalanceCommand(String name,
                                   @PositiveOrZero Long daysLeft,
                                   @PositiveOrZero Long maxDays) {
}
