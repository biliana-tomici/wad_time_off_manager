package com.project.timeOffManager.dto;

import java.time.LocalDate;

public record CalendarProjectCoverageDto(LocalDate date,
                                         Long projectId,
                                         String projectName,
                                         Long minEmp,
                                         int totalMembers,
                                         int availableCount,
                                         boolean understaffed) {
}
