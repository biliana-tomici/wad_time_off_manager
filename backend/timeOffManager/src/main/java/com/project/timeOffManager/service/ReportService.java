package com.project.timeOffManager.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.security.AuthorizationService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    private final RequestService requestService;
    private final ProjectService projectService;
    private final UserService userService;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;

    public ReportService(RequestService requestService,
                         ProjectService projectService,
                         UserService userService,
                         ProjectRepository projectRepository,
                         AuthorizationService authorizationService) {
        this.requestService = requestService;
        this.projectService = projectService;
        this.userService = userService;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
    }

    public byte[] getProjectReportPdf(Long projectId, TimePeriod timePeriod) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanDownloadReport(project);

        List<User> employees = userService.getUsersByProjectId(projectId);

        LocalDate today = LocalDate.now();
        List<Request> requests = requestService.getRequestsByUsersAndTimePeriod(employees, timePeriod);
        List<Request> approvedCoveringToday = requestService.getApprovedRequestsCoveringDate(employees, today);

        java.util.Set<Long> absentEmployeeIds = approvedCoveringToday.stream()
                .map(r -> r.getEmployee().getId())
                .collect(java.util.stream.Collectors.toSet());

        Map<User, Boolean> availableEmployees = new HashMap<>();
        for (User user : employees) {
            boolean isAvailable = !absentEmployeeIds.contains(user.getId());
            availableEmployees.put(user, isAvailable);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Project report for Project: " + project.getName() + " (ID: " + project.getId() + ")"));
            document.add(new Paragraph("Total Employees: " + employees.size()));
            document.add(new Paragraph("Total available employees as of today: " + availableEmployees.values().stream().filter(Boolean::booleanValue).count()));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(5);

            table.addCell("Request ID");
            table.addCell("Employee Name");
            table.addCell("Start Date");
            table.addCell("End Date");
            table.addCell("Status");

            for (Request request : requests) {
                table.addCell(request.getId().toString());
                table.addCell(request.getEmployee().getLastName() + " " + request.getEmployee().getFirstName());
                table.addCell(request.getStartDate().toString());
                table.addCell(request.getEndDate().toString());
                table.addCell(request.getStatus().toString());
            }

            document.add(table);
            document.close();
            return outputStream.toByteArray();
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
