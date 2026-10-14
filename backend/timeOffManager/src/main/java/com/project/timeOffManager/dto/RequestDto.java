package com.project.timeOffManager.dto;

import com.project.timeOffManager.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Flat response for {@link com.project.timeOffManager.entity.Request}.
 */
public record RequestDto(Long id,
                         Long employeeId,
                         String employeeName,
                         Long balanceId,
                         String balanceName,
                         Status status,
                         LocalDate startDate,
                         LocalDate endDate,
                         String explanation,
                         LocalDate creationDate,
                         LocalDate modifiedDate,
                         Long decidedById,
                         String decidedByName,
                         LocalDateTime decidedAt,
                         String decisionComment) {
}