package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Status;
import com.project.timeOffManager.enums.TimePeriod;
import com.project.timeOffManager.mapper.RequestMapper;
import com.project.timeOffManager.repository.RequestRepository;
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

    public RequestService(RequestRepository requestRepository, RequestMapper requestMapper) {
        this.requestRepository = requestRepository;
        this.requestMapper = requestMapper;
    }

    public Request getRequestById(Long requestId) {
        return requestRepository.findRequestById(requestId);
    }

    public Request createRequest(Request request) {
        return requestRepository.save(request);
    }

    public RequestDto updateRequest(RequestDto requestDto, Long requestId) {

        Request existingRequest = requestRepository.findRequestById(requestId);

        if (existingRequest == null) {
            return null;
        }

        requestMapper.updateRequestFromDto(requestDto, existingRequest);
        existingRequest.setModifiedDate(LocalDate.now());
        existingRequest.setStatus(Status.EDITED);

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
        return requestRepository.findPendingRequestsByManagerId(managerId, pageable);
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
