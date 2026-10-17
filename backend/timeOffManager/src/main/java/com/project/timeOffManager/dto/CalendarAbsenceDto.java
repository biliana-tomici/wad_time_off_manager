package com.project.timeOffManager.dto;

import com.project.timeOffManager.enums.Status;

import java.time.LocalDate;
import java.util.List;

public record CalendarAbsenceDto(Long requestId,
                                 Long employeeId,
                                 String employeeName,
                                 Long balanceId,
                                 String balanceName,
                                 Status status,
                                 LocalDate startDate,
                                 LocalDate endDate,
                                 Long workingDays,
                                 String explanation,
                                 List<Long> projectIds) {
}
