package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.mapper.RequestMapper;
import com.project.timeOffManager.repository.BalanceRepository;
import com.project.timeOffManager.repository.RequestRepository;
import com.project.timeOffManager.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final RequestRepository requestRepository;
    private final RequestMapper requestMapper;
    private final UserRepository userRepository;
    private final BalanceRepository balanceRepository;

    public RequestService(RequestRepository requestRepository,
                          RequestMapper requestMapper,
                          UserRepository userRepository,
                          BalanceRepository balanceRepository) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
        this.userRepository = userRepository;
        this.balanceRepository = balanceRepository;
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findRequestById(requestId);
    }

    public Request createRequest(Request request) {
        if (request.getEmployee() == null || request.getEmployee().getId() == null) {
            return null;
        }

        User employee = userRepository.findCustomerById(request.getEmployee().getId());
        if (employee == null) {
            return null;
        }
        request.setEmployee(employee);

        if (request.getBalance() == null || request.getBalance().getId() == null) {
            return null;
        }
        Balance balance = balanceRepository.findBalanceById(request.getBalance().getId());
        if (balance == null) {
            return null;
        }
        request.setBalance(balance);

        if (request.getStatus() == null) {
            request.setStatus(Status.PENDING);
        }
        LocalDate today = LocalDate.now();
        if (request.getCreationDate() == null) {
            request.setCreationDate(today);
        }
        request.setModifiedDate(today);

        return requestRepository.save(request);
    }

    public RequestDto updateRequest(RequestDto requestDto, Long requestId) {

        Request existingRequest = requestRepository.findRequestById(requestId);

        if (existingRequest == null) {
            return null;
        }

        if (requestDto.startDate() != null) {
            existingRequest.setStartDate(requestDto.startDate());
        }
        if (requestDto.endDate() != null) {
            existingRequest.setEndDate(requestDto.endDate());
        }
        if (requestDto.explanation() != null) {
            existingRequest.setExplanation(requestDto.explanation());
        }
        if (requestDto.balance() != null && requestDto.balance().id() != null) {
            Balance balance = balanceRepository.findBalanceById(requestDto.balance().id());
            if (balance == null) {
                return null;
            }
            existingRequest.setBalance(balance);
        }

        existingRequest.setModifiedDate(LocalDate.now());
        existingRequest.setStatus(Status.EDITED);

        return requestMapper.toDto(requestRepository.save(existingRequest));
    }

    public RequestDto updateStatus(Long requestId, Status status) {
        Request existingRequest = requestRepository.findRequestById(requestId);

        if (existingRequest == null || status == null) {
            return null;
        }

        existingRequest.setStatus(status);
        existingRequest.setModifiedDate(LocalDate.now());

        return requestMapper.toDto(requestRepository.save(existingRequest));
    }

    public boolean deleteRequest(Long requestId) {

        if (requestRepository.existsById(requestId)) {
            requestRepository.deleteById(requestId);
            return true;
        }

        return false;
    }

    public Page<Request> getPendingRequestsByManagerId(Long managerId, Pageable pageable) {
        return requestRepository.findPendingRequestsByManagerId(managerId, Status.PENDING, pageable);
    }

    public Page<Request> getRequestsByEmployeeId(Long employeeId, Pageable pageable) {
        return requestRepository.findAllByEmployee_Id_OrderByCreationDateDesc(employeeId, pageable);
    }

    public List<Request> getRequestsByUsersAndTimePeriod(List<User> employees, TimePeriod timePeriod) {
        return employees.stream()
                .flatMap(employee -> requestRepository.findAllByEmployee_IdAndCreationDateAfter(
                        employee.getId(), timePeriod.getStartDate())
                        .stream())
                .collect(Collectors.toList());
    }
}
