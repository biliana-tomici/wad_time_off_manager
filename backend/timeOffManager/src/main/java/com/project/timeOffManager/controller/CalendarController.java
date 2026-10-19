package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.CalendarResponseDto;
import com.project.timeOffManager.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping({"/api/calendar", "/api/calendars"})
public class CalendarController {

    private final CalendarService calendarService;

    public CalendarController(CalendarService calendarService) {
        this.calendarService = calendarService;
    }

    @Operation(summary = "Get team calendar absences and project coverage")
    @ApiResponse(responseCode = "200", description = "Calendar absences retrieved")
    @GetMapping({"", "/"})
    public ResponseEntity<CalendarResponseDto> getCalendar(
            @Parameter(description = "Start date of the calendar window (ISO format)")
            @RequestParam(value = "from", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @Parameter(description = "End date of the calendar window (ISO format)")
            @RequestParam(value = "to", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @Parameter(description = "Optional project ID to filter by")
            @RequestParam(value = "projectId", required = false) Long projectId) {

        return ResponseEntity.ok(calendarService.getCalendar(from, to, projectId));
    }
}
