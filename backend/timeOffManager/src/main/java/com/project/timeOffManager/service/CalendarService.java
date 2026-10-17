package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.CalendarAbsenceDto;
import com.project.timeOffManager.dto.CalendarProjectCoverageDto;
import com.project.timeOffManager.dto.CalendarResponseDto;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.exception.AccessDeniedException;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.RequestRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalendarService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RequestRepository requestRepository;
    private final AuthorizationService authorizationService;

    public CalendarService(ProjectRepository projectRepository,
                           UserRepository userRepository,
                           RequestRepository requestRepository,
                           AuthorizationService authorizationService) {
        this.projectRepository = projectRepository;
        this.userRepository = userRepository;
        this.requestRepository = requestRepository;
        this.authorizationService = authorizationService;
    }

    public CalendarResponseDto getCalendar(LocalDate from, LocalDate to, Long projectId) {
        LocalDate effectiveFrom = from != null ? from : LocalDate.now().withDayOfMonth(1);
        LocalDate effectiveTo = to != null
                ? to
                : effectiveFrom.plusMonths(1).withDayOfMonth(1).minusDays(1);

        if (effectiveTo.isBefore(effectiveFrom)) {
            throw new BusinessRuleException("'to' date cannot be before 'from' date");
        }

        User current = authorizationService.getCurrentUser();
        List<Project> visibleProjects = resolveVisibleProjects(current, projectId);
        Set<Long> relevantEmployeeIds = resolveRelevantEmployeeIds(current, visibleProjects, projectId);

        List<CalendarAbsenceDto> absences = new ArrayList<>();
        if (!relevantEmployeeIds.isEmpty()) {
            List<Request> requests = requestRepository.findRequestsOverlappingWindowWithStatuses(
                    new ArrayList<>(relevantEmployeeIds),
                    List.of(Status.APPROVED, Status.PENDING, Status.EDITED),
                    effectiveFrom,
                    effectiveTo
            );

            for (Request r : requests) {
                User emp = r.getEmployee();
                String empName = emp != null ? emp.getFirstName() + " " + emp.getLastName() : "Unknown";
                String balanceName = r.getBalance() != null ? r.getBalance().getName() : "Leave";
                Long balanceId = r.getBalance() != null ? r.getBalance().getId() : null;
                Long workingDays = LeaveDayCalculator.calculateWorkingDays(r.getStartDate(), r.getEndDate());

                List<Long> empProjectIds = visibleProjects.stream()
                        .filter(p -> p.getEmployees() != null && emp != null && p.getEmployees().stream().anyMatch(e -> e.getId().equals(emp.getId())))
                        .map(Project::getId)
                        .toList();

                absences.add(new CalendarAbsenceDto(
                        r.getId(),
                        emp != null ? emp.getId() : null,
                        empName,
                        balanceId,
                        balanceName,
                        r.getStatus(),
                        r.getStartDate(),
                        r.getEndDate(),
                        workingDays,
                        r.getExplanation(),
                        empProjectIds
                ));
            }
        }

        List<CalendarProjectCoverageDto> coverages = calculateProjectCoverages(visibleProjects, effectiveFrom, effectiveTo);

        return new CalendarResponseDto(effectiveFrom, effectiveTo, absences, coverages);
    }

    private List<Project> resolveVisibleProjects(User current, Long projectId) {
        if (authorizationService.isAdmin(current)) {
            if (projectId != null) {
                Project p = projectRepository.findProjectById(projectId);
                if (p == null) {
                    throw new ResourceNotFoundException("Project not found with id: " + projectId);
                }
                return List.of(p);
            }
            return projectRepository.findAll();
        }

        if (authorizationService.isManager(current)) {
            List<Project> managed = projectRepository.findByManagerId(current.getId());
            List<Project> assigned = projectRepository.findAllByEmployees_Id(current.getId());
            Set<Project> combined = new LinkedHashSet<>(managed);
            combined.addAll(assigned);

            if (projectId != null) {
                Project p = projectRepository.findProjectById(projectId);
                if (p == null) {
                    throw new ResourceNotFoundException("Project not found with id: " + projectId);
                }
                boolean hasAccess = combined.stream().anyMatch(proj -> proj.getId().equals(projectId));
                if (!hasAccess) {
                    throw new AccessDeniedException("Not authorized to view calendar for project ID " + projectId);
                }
                return List.of(p);
            }
            return new ArrayList<>(combined);
        }

        // Employee role: only assigned projects
        List<Project> assigned = projectRepository.findAllByEmployees_Id(current.getId());
        if (projectId != null) {
            Project p = projectRepository.findProjectById(projectId);
            if (p == null) {
                throw new ResourceNotFoundException("Project not found with id: " + projectId);
            }
            boolean isMember = assigned.stream().anyMatch(proj -> proj.getId().equals(projectId));
            if (!isMember) {
                throw new AccessDeniedException("Not authorized to view calendar for project ID " + projectId);
            }
            return List.of(p);
        }
        return assigned;
    }

    private Set<Long> resolveRelevantEmployeeIds(User current, List<Project> visibleProjects, Long projectId) {
        Set<Long> ids = new HashSet<>();

        if (authorizationService.isAdmin(current) && projectId == null) {
            userRepository.findByActiveTrue().forEach(u -> ids.add(u.getId()));
            return ids;
        }

        for (Project p : visibleProjects) {
            if (p.getEmployees() != null) {
                p.getEmployees().stream()
                        .filter(User::isActive)
                        .forEach(u -> ids.add(u.getId()));
            }
        }

        if (authorizationService.isManager(current) && projectId == null) {
            userRepository.findByManager_Id(current.getId()).stream()
                    .filter(User::isActive)
                    .forEach(u -> ids.add(u.getId()));
        }

        ids.add(current.getId());
        return ids;
    }

    private List<CalendarProjectCoverageDto> calculateProjectCoverages(List<Project> projects,
                                                                       LocalDate from,
                                                                       LocalDate to) {
        List<CalendarProjectCoverageDto> coverages = new ArrayList<>();

        for (Project project : projects) {
            Long minEmp = project.getMinEmp();
            if (minEmp == null || minEmp <= 0) {
                continue;
            }
            List<User> members = project.getEmployees() != null
                    ? project.getEmployees().stream().filter(User::isActive).toList()
                    : List.of();
            int totalMembers = members.size();
            if (totalMembers == 0) {
                continue;
            }
            List<Long> memberIds = members.stream().map(User::getId).toList();

            List<Request> approvedRequests = requestRepository.findRequestsOverlappingWindowWithStatuses(
                    memberIds,
                    List.of(Status.APPROVED),
                    from,
                    to
            );

            for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
                final LocalDate currentDay = d;
                boolean isWeekend = currentDay.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                        || currentDay.getDayOfWeek() == java.time.DayOfWeek.SUNDAY;

                if (isWeekend) {
                    // System rule: Weekends are non-working days; absences and staffing thresholds do not apply
                    coverages.add(new CalendarProjectCoverageDto(
                            currentDay,
                            project.getId(),
                            project.getName(),
                            minEmp,
                            totalMembers,
                            totalMembers,
                            false
                    ));
                    continue;
                }

                long absentCount = approvedRequests.stream()
                        .filter(r -> !r.getStartDate().isAfter(currentDay) && !r.getEndDate().isBefore(currentDay))
                        .map(r -> r.getEmployee().getId())
                        .distinct()
                        .count();

                int available = Math.max(0, (int) (totalMembers - absentCount));
                boolean understaffed = available < minEmp;

                coverages.add(new CalendarProjectCoverageDto(
                        currentDay,
                        project.getId(),
                        project.getName(),
                        minEmp,
                        totalMembers,
                        available,
                        understaffed
                ));
            }
        }

        return coverages;
    }
}
