package com.project.timeOffManager.dto;

import com.project.timeOffManager.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
                         Long workingDays,
                         String explanation,
                         LocalDate creationDate,
                         LocalDate modifiedDate,
                         Long decidedById,
                         String decidedByName,
                         LocalDateTime decidedAt,
                         String decisionComment,
                         List<String> coverageWarnings) {

    public RequestDto {
        if (coverageWarnings == null) {
            coverageWarnings = List.of();
        }
    }

    public RequestDto withCoverageWarnings(List<String> warnings) {
        return new RequestDto(id, employeeId, employeeName, balanceId, balanceName, status,
                startDate, endDate, workingDays, explanation, creationDate, modifiedDate,
                decidedById, decidedByName, decidedAt, decisionComment, warnings);
    }
}