package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.dto.command.CreateRequestCommand;
import com.project.timeOffManager.dto.command.UpdateRequestCommand;
import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.mapper.RequestMapper;
import com.project.timeOffManager.repository.BalanceRepository;
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
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;
    private final AuthorizationService authorizationService;

    public RequestService(RequestRepository requestRepository,
                          RequestMapper requestMapper,
                          UserRepository userRepository,
                          BalanceRepository balanceRepository,
                          AuthorizationService authorizationService) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
        this.authorizationService = authorizationService;
    }

    public RequestDto getRequestById(Long requestId) {
        Request request = requestRepository.findRequestById(requestId);
        if (request == null) {
            throw new ResourceNotFoundException("Request not found with id: " + requestId);
        }
        authorizationService.checkCanViewRequest(request);
        return requestMapper.toDto(request);
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
    public RequestDto updateStatus(Long requestId, Status status) {
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
        existingRequest.setStatus(status);
        existingRequest.setModifiedDate(LocalDate.now());

        return requestMapper.toDto(requestRepository.save(existingRequest));
    }

    private boolean isValidStatusTransition(Status current, Status target) {
        if (current == target) {
            return true;
        }
        return switch (current) {
            case PENDING, EDITED -> target == Status.APPROVED || target == Status.REJECTED || target == Status.CANCELLED;
            case APPROVED -> target == Status.CANCELLED;
            case REJECTED, CANCELLED -> false;
        };
    }

    public long calculateWorkingDays(LocalDate start, LocalDate end) {
        if (start == null || end == null || end.isBefore(start)) {
            return 0;
        }
        long days = 0;
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dow = current.getDayOfWeek();
            if (dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY) {
                days++;
            }
            current = current.plusDays(1);
        }
        return days;
    }

    public boolean deleteRequest(Long requestId) {
        Request existingRequest = requestRepository.findRequestById(requestId);
        if (existingRequest == null) {
            return false;
        }
        authorizationService.checkCanDeleteRequest(existingRequest);
        requestRepository.delete(existingRequest);
        return true;
    }

    public Page<RequestDto> getPendingRequestsByManagerId(Long managerId, Pageable pageable) {
        return getPendingRequestsByManagerId(managerId, false, pageable);
    }

    public Page<RequestDto> getPendingRequestsByManagerId(Long managerId, boolean includeUnassigned, Pageable pageable) {
        authorizationService.checkCanViewPendingQueue(managerId, includeUnassigned);
        return requestRepository.findPendingRequestsByManagerId(managerId, includeUnassigned, List.of(Status.PENDING, Status.EDITED), pageable)
                .map(requestMapper::toDto);
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
