package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * What a client may send to create a request. Status and dates of creation are set by the server.
 * {@code employeeId} is optional and defaults to the current user.
 */
public record CreateRequestCommand(Long employeeId,
                                   @NotNull Long balanceId,
                                   @NotNull LocalDate startDate,
                                   @NotNull LocalDate endDate,
                                   @Size(max = 255) String explanation) {
}
