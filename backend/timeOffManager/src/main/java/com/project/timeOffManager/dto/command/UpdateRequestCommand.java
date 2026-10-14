package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/** Partial update of a request: {@code null} fields are left unchanged. */
public record UpdateRequestCommand(Long balanceId,
                                   LocalDate startDate,
                                   LocalDate endDate,
                                   @Size(max = 255) String explanation) {
}
