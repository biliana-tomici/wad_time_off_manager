package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.dto.command.CreateRequestCommand;
import com.project.timeOffManager.dto.command.UpdateRequestCommand;
import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.mapper.RequestMapper;
import com.project.timeOffManager.repository.BalanceRepository;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.RequestRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;

    public RequestService(RequestRepository requestRepository,
                          RequestMapper requestMapper,
                          UserRepository userRepository,
                          BalanceRepository balanceRepository,
                          ProjectRepository projectRepository,
                          AuthorizationService authorizationService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
    }

    public RequestDto getRequestById(Long requestId) {
        Request request = requestRepository.findRequestById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Request not found with id: " + requestId);
        }
        authorizationService.checkCanViewRequest(request);
        return toDtoWithCoverageWarnings(request);
    }

    @Transactional
    public RequestDto createRequest(CreateRequestCommand command) {
        Long employeeId = command.employeeId() != null
                ? command.employeeId()
                : authorizationService.getCurrentUser().getId();

        authorizationService.checkCanCreateRequest(employeeId);

        User employee = userRepository.findCustomerById(employeeId);
        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        Balance balance = balanceRepository.findBalanceById(command.balanceId());
        if (balance == null) {
            throw new ResourceNotFoundException("Balance not found with id: " + command.balanceId());
        }
        if (balance.getEmployee() == null || !balance.getEmployee().getId().equals(employee.getId())) {
            throw new BusinessRuleException("Balance does not belong to employee id: " + employee.getId());
        }

        if (command.endDate().isBefore(command.startDate())) {
            throw new BusinessRuleException("End date cannot be before start date");
        }

        if (!authorizationService.isAdmin(authorizationService.getCurrentUser())
                && command.startDate().isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Start date cannot be in the past");
        }

        long requestedDays = calculateWorkingDays(command.startDate(), command.endDate());
        if (requestedDays <= 0) {
            throw new BusinessRuleException("Requested period contains no working days (Mon-Fri)");
        }

        List<Request> activeRequests = requestRepository.findByEmployee_IdAndStatusIn(
                employee.getId(),
                List.of(Status.PENDING, Status.EDITED, Status.APPROVED)
        );

        for (Request r : activeRequests) {
            if (!r.getEndDate().isBefore(command.startDate()) && !r.getStartDate().isAfter(command.endDate())) {
                throw new BusinessRuleException("Requested leave overlaps with existing request id " + r.getId() + " (" + r.getStartDate() + " to " + r.getEndDate() + ")");
            }
        }

        long pendingDays = 0;
        for (Request r : activeRequests) {
            if (r.getBalance() != null && r.getBalance().getId().equals(balance.getId())
                    && (r.getStatus() == Status.PENDING || r.getStatus() == Status.EDITED)) {
                pendingDays += calculateWorkingDays(r.getStartDate(), r.getEndDate());
            }
        }

        if (balance.getDaysLeft() - pendingDays < requestedDays) {
            throw new BusinessRuleException(String.format("Insufficient balance days: requested %d, available %d (%d remaining - %d reserved)",
                    requestedDays, balance.getDaysLeft() - pendingDays, balance.getDaysLeft(), pendingDays));
        }

        Request request = new Request();
        request.setEmployee(employee);
        request.setBalance(balance);
        request.setStartDate(command.startDate());
        request.setEndDate(command.endDate());
        request.setExplanation(command.explanation());
        request.setStatus(Status.PENDING);
        LocalDate today = LocalDate.now();
        request.setCreationDate(today);
        request.setModifiedDate(today);

        return requestMapper.toDto(requestRepository.save(request));
    }

    @Transactional
    public RequestDto updateRequest(UpdateRequestCommand command, Long requestId) {
        Request existingRequest = requestRepository.findRequestById(requestId);

        if (existingRequest == null) {
            throw new ResourceNotFoundException("Request not found with id: " + requestId);
        }

        authorizationService.checkCanEditRequest(existingRequest);

        if (existingRequest.getStatus() != Status.PENDING && existingRequest.getStatus() != Status.EDITED) {
            throw new BusinessRuleException("Cannot edit request with status " + existingRequest.getStatus() + "; only PENDING or EDITED requests can be edited");
        }

        LocalDate newStart = command.startDate() != null ? command.startDate() : existingRequest.getStartDate();
        LocalDate newEnd = command.endDate() != null ? command.endDate() : existingRequest.getEndDate();

        if (newEnd.isBefore(newStart)) {
            throw new BusinessRuleException("End date cannot be before start date");
        }

        if (!authorizationService.isAdmin(authorizationService.getCurrentUser())
                && newStart.isBefore(LocalDate.now())) {
            throw new BusinessRuleException("Start date cannot be in the past");
        }

        Balance newBalance = existingRequest.getBalance();
        Long newBalanceId = command.balanceId();
        if (newBalanceId != null) {
            newBalance = balanceRepository.findBalanceById(newBalanceId);
            if (newBalance == null) {
                throw new ResourceNotFoundException("Balance not found with id: " + newBalanceId);
            }
            if (newBalance.getEmployee() == null || !newBalance.getEmployee().getId().equals(existingRequest.getEmployee().getId())) {
                throw new BusinessRuleException("Balance does not belong to employee id: " + existingRequest.getEmployee().getId());
            }
        }

        long requestedDays = calculateWorkingDays(newStart, newEnd);
        if (requestedDays <= 0) {
            throw new BusinessRuleException("Requested period contains no working days (Mon-Fri)");
        }

        List<Request> activeRequests = requestRepository.findByEmployee_IdAndStatusIn(
                existingRequest.getEmployee().getId(),
                List.of(Status.PENDING, Status.EDITED, Status.APPROVED)
        );

        for (Request r : activeRequests) {
            if (r.getId().equals(requestId)) continue;
            if (!r.getEndDate().isBefore(newStart) && !r.getStartDate().isAfter(newEnd)) {
                throw new BusinessRuleException("Requested leave overlaps with existing request id " + r.getId() + " (" + r.getStartDate() + " to " + r.getEndDate() + ")");
            }
        }

        long pendingDays = 0;
        for (Request r : activeRequests) {
            if (r.getId().equals(requestId)) continue;
            if (r.getBalance() != null && r.getBalance().getId().equals(newBalance.getId())
                    && (r.getStatus() == Status.PENDING || r.getStatus() == Status.EDITED)) {
                pendingDays += calculateWorkingDays(r.getStartDate(), r.getEndDate());
            }
        }

        if (newBalance.getDaysLeft() - pendingDays < requestedDays) {
            throw new BusinessRuleException(String.format("Insufficient balance days: requested %d, available %d (%d remaining - %d reserved)",
                    requestedDays, newBalance.getDaysLeft() - pendingDays, newBalance.getDaysLeft(), pendingDays));
        }

        existingRequest.setStartDate(newStart);
        existingRequest.setEndDate(newEnd);
        existingRequest.setBalance(newBalance);
        if (command.explanation() != null) {
            existingRequest.setExplanation(command.explanation());
        }

        existingRequest.setModifiedDate(LocalDate.now());
        existingRequest.setStatus(Status.EDITED);

        return requestMapper.toDto(requestRepository.save(existingRequest));
    }

    @Transactional
    public RequestDto cancelRequest(Long requestId) {
        return updateStatus(requestId, Status.CANCELLED, null);
    }

    @Transactional
    public RequestDto approveRequest(Long requestId, String comment) {
        return updateStatus(requestId, Status.APPROVED, comment);
    }

    @Transactional
    public RequestDto rejectRequest(Long requestId, String comment) {
        return updateStatus(requestId, Status.REJECTED, comment);
    }

    @Transactional
    public RequestDto updateStatus(Long requestId, Status status) {
        return updateStatus(requestId, status, null);
    }

    @Transactional
    public RequestDto updateStatus(Long requestId, Status status, String comment) {
        Request existingRequest = requestRepository.findRequestById(requestId);

        if (existingRequest == null) {
            throw new ResourceNotFoundException("Request not found with id: " + requestId);
        }

        if (status == null) {
            throw new IllegalArgumentException("Status cannot be null");
        }

        authorizationService.checkCanChangeStatus(existingRequest, status);

        Status oldStatus = existingRequest.getStatus();

        if (!isValidStatusTransition(oldStatus, status)) {
            throw new BusinessRuleException("Invalid status transition from " + oldStatus + " to " + status);
        }

        if (oldStatus != Status.APPROVED && status == Status.APPROVED) {
            Balance balance = existingRequest.getBalance();
            if (balance != null) {
                long requestedDays = calculateWorkingDays(existingRequest.getStartDate(), existingRequest.getEndDate());
                if (balance.getDaysLeft() < requestedDays) {
                    throw new BusinessRuleException("Insufficient balance days to approve: requested " + requestedDays + ", remaining " + balance.getDaysLeft());
                }
                balance.setDaysLeft(balance.getDaysLeft() - requestedDays);
                balanceRepository.save(balance);
            }
        } else if (oldStatus == Status.APPROVED && status == Status.CANCELLED) {
            Balance balance = existingRequest.getBalance();
            if (balance != null) {
                long requestedDays = calculateWorkingDays(existingRequest.getStartDate(), existingRequest.getEndDate());
                balance.setDaysLeft(balance.getDaysLeft() + requestedDays);
                balanceRepository.save(balance);
            }
        }

        existingRequest.setDecidedBy(authorizationService.getCurrentUser());
        existingRequest.setDecidedAt(LocalDateTime.now());
        if (comment != null && !comment.isBlank()) {
            existingRequest.setDecisionComment(comment.trim());
        }
        existingRequest.setStatus(status);
        existingRequest.setModifiedDate(LocalDate.now());

        return requestMapper.toDto(requestRepository.save(existingRequest));
    }

    private boolean isValidStatusTransition(Status current, Status target) {
        return switch (current) {
            case PENDING, EDITED -> target == Status.APPROVED || target == Status.REJECTED || target == Status.CANCELLED;
            case APPROVED -> target == Status.CANCELLED;
            case REJECTED, CANCELLED -> false;
        };
    }

    public long calculateWorkingDays(LocalDate start, LocalDate end) {
        return LeaveDayCalculator.calculateWorkingDays(start, end);
    }

    @Transactional
    public boolean deleteRequest(Long requestId) {
        Request existingRequest = requestRepository.findRequestById(requestId);
        if (existingRequest == null) {
            return false;
        }
        authorizationService.checkCanDeleteRequest(existingRequest);
        if (!authorizationService.isAdmin(authorizationService.getCurrentUser())
                && existingRequest.getStatus() != Status.PENDING
                && existingRequest.getStatus() != Status.EDITED) {
            throw new BusinessRuleException("Cannot delete request with status " + existingRequest.getStatus() + "; only PENDING or EDITED requests can be deleted");
        }
        requestRepository.delete(existingRequest);
        return true;
    }

    public Page<RequestDto> getPendingRequestsByManagerId(Long managerId, Pageable pageable) {
        return getPendingRequestsByManagerId(managerId, false, pageable);
    }

    public Page<RequestDto> getPendingRequestsByManagerId(Long managerId, boolean includeUnassigned, Pageable pageable) {
        authorizationService.checkCanViewPendingQueue(managerId, includeUnassigned);
        boolean effectiveIncludeUnassigned = includeUnassigned || authorizationService.isAdmin(authorizationService.getCurrentUser());
        return requestRepository.findPendingRequestsByManagerId(managerId, effectiveIncludeUnassigned, List.of(Status.PENDING, Status.EDITED), pageable)
                .map(this::toDtoWithCoverageWarnings);
    }

    private RequestDto toDtoWithCoverageWarnings(Request request) {
        RequestDto dto = requestMapper.toDto(request);
        if (request.getStatus() == Status.PENDING || request.getStatus() == Status.EDITED) {
            return dto.withCoverageWarnings(computeCoverageWarnings(request));
        }
        return dto;
    }

    public List<String> computeCoverageWarnings(Request request) {
        if (request == null || request.getEmployee() == null || request.getStartDate() == null || request.getEndDate() == null) {
            return List.of();
        }
        Long empId = request.getEmployee().getId();
        List<Project> projects = projectRepository.findAllByEmployees_Id(empId);
        if (projects.isEmpty()) {
            return List.of();
        }

        List<String> warnings = new ArrayList<>();
        LocalDate start = request.getStartDate();
        LocalDate end = request.getEndDate();

        for (Project project : projects) {
            if (project.getMinEmp() == null || project.getMinEmp() <= 0) {
                continue;
            }
            List<User> members = project.getEmployees() != null
                    ? project.getEmployees().stream().filter(User::isActive).toList()
                    : List.of();
            int totalMembers = members.size();
            List<Long> otherMemberIds = members.stream()
                    .map(User::getId)
                    .filter(id -> !id.equals(empId))
                    .toList();

            List<Request> overlappingApproved = otherMemberIds.isEmpty()
                    ? List.of()
                    : requestRepository.findRequestsOverlappingWindow(otherMemberIds, start, end).stream()
                            .filter(r -> r.getStatus() == Status.APPROVED)
                            .toList();

            long maxAbsent = 1;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                if (d.getDayOfWeek() == java.time.DayOfWeek.SATURDAY || d.getDayOfWeek() == java.time.DayOfWeek.SUNDAY) {
                    continue;
                }
                final LocalDate day = d;
                long otherAbsent = overlappingApproved.stream()
                        .filter(r -> !r.getStartDate().isAfter(day) && !r.getEndDate().isBefore(day))
                        .map(r -> r.getEmployee().getId())
                        .distinct()
                        .count();
                maxAbsent = Math.max(maxAbsent, 1 + otherAbsent);
            }

            long minAvailable = Math.max(0, totalMembers - maxAbsent);
            if (minAvailable < project.getMinEmp()) {
                warnings.add(String.format("Approving leaves %s with %d of %d required",
                        project.getName(), minAvailable, project.getMinEmp()));
            }
        }
        return warnings;
    }

    public Page<RequestDto> getRequestsByEmployeeId(Long employeeId, Pageable pageable) {
        authorizationService.checkCanViewRequests(employeeId);
        return requestRepository.findAllByEmployee_Id_OrderByCreationDateDesc(employeeId, pageable)
                .map(requestMapper::toDto);
    }

    public List<Request> getRequestsByUsersAndTimePeriod(List<User> employees, TimePeriod timePeriod) {
        if (employees == null || employees.isEmpty()) {
            return List.of();
        }
        List<Long> employeeIds = employees.stream().map(User::getId).collect(Collectors.toList());
        LocalDate windowStart = timePeriod.getStartDate();
        LocalDate windowEnd = LocalDate.now();
        return requestRepository.findRequestsOverlappingWindow(employeeIds, windowStart, windowEnd);
    }

    public List<Request> getApprovedRequestsCoveringDate(List<User> employees, LocalDate date) {
        if (employees == null || employees.isEmpty()) {
            return List.of();
        }
        List<Long> employeeIds = employees.stream().map(User::getId).collect(Collectors.toList());
        return requestRepository.findApprovedRequestsCoveringDate(employeeIds, Status.APPROVED, date);
    }
}
