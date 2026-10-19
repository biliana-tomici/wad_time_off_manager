package com.project.timeOffManager.dto;

import java.time.LocalDate;
import java.util.List;

public record CalendarResponseDto(LocalDate from,
                                  LocalDate to,
                                  List<CalendarAbsenceDto> absences,
                                  List<CalendarProjectCoverageDto> coverages) {
}
