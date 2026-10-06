package com.project.timeOffManager.controller;

import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @Operation(summary = "Generate a report for a project")
    @ApiResponse(responseCode = "200", description = "Report generated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Project not found")
    @RequestMapping("/project/{projectId}/pdf")
    public ResponseEntity<byte[]> generateProjectReport(@Parameter(description = "ID of the project to generate a report for", required = true)
                                                        @PathVariable("projectId") Long projectId,
                                                        @Parameter(description = "Time period", required = true)
                                                        @RequestParam("timePeriod") TimePeriod timePeriod) {

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=project_report_" + projectId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(reportService.getProjectReportPdf(projectId, timePeriod));

    }
}
