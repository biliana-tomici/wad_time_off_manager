package com.project.timeOffManager.service;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfTable;
import com.lowagie.text.pdf.PdfWriter;
import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
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

    public ReportService(RequestService requestService, ProjectService projectService, UserService userService) {
        this.requestService = requestService;
        this.projectService = projectService;
        this.userService = userService;
    }

    public byte[] getProjectReportPdf(Long projectId, TimePeriod timePeriod) {

        ProjectDto project = projectService.getProjectById(projectId);

        if (project == null) {
            return null;
        }

        List<User> employees = userService.getUsersByProjectId(projectId);

        List<Request> requests = requestService.getRequestsByUsersAndTimePeriod(employees, timePeriod);

        Map<User, Boolean> availableEmployees = new HashMap<>();
        LocalDate today = LocalDate.now();

        for (User user : employees) {
            boolean isAvailable = requests.stream()
                    .filter(request -> request.getEmployee().getId().equals(user.getId()))
                    .noneMatch(request ->
                            (request.getStartDate().isBefore(today) || request.getStartDate().isEqual(today)) &&
                                    (request.getEndDate().isAfter(today) || request.getEndDate().isEqual(today)) &&
                                    request.getStatus().equals(Status.APPROVED)
                    );

            availableEmployees.put(user, isAvailable);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, outputStream);
            document.open();

            document.add(new Paragraph("Project report for Project: " + project.name() + " (ID: " + project.id() + ")"));
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
