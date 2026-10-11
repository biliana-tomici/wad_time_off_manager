package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.mapper.RequestMapper;
import com.project.timeOffManager.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/request")
public class RequestController {

    private final RequestService requestService;
    private final RequestMapper requestMapper;

    @Autowired
    public RequestController(RequestService requestService, RequestMapper requestMapper) {
        this.requestService = requestService;
        this.requestMapper = requestMapper;
    }

    @Operation(summary = "Get request by ID")
    @ApiResponse(responseCode = "200", description = "Request found")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @GetMapping("/{id}")
    public ResponseEntity<RequestDto> getRequestById(@Parameter(description = "ID of the request to retrieve", required = true)
                                                     @PathVariable("id") Long id) {

        Request request = requestService.getRequestById(id);

        if (request == null) {
            return ResponseEntity.notFound().build();
        }

       return ResponseEntity.ok(requestMapper.toDto(request));
    }

    @Operation(summary = "Create a new request")
    @ApiResponse(responseCode = "201", description = "Request created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    public ResponseEntity<RequestDto> createRequest(@Parameter(description = "The Request object to be created", required = true)
                                                    @RequestBody RequestDto requestDto) {

        Request createdRequest = requestService.createRequest(requestMapper.toEntity(requestDto));

        if (createdRequest == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(requestMapper.toDto(createdRequest));
    }

    @Operation(summary = "Update an existing request")
    @ApiResponse(responseCode = "200", description = "Request updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PutMapping("/{id}")
    public ResponseEntity<RequestDto> updateRequest(@Parameter(description = "ID of the request to update", required = true)
                                                    @PathVariable("id") Long id,
                                                    @Parameter(description = "Updated Request object", required = true)
                                                    @RequestBody RequestDto requestDto) {

        RequestDto updatedRequest = requestService.updateRequest(requestDto, id);

        if (updatedRequest == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedRequest);
    }

    @Operation(summary = "Update the status of a request")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestDto> updateStatus(@Parameter(description = "ID of the request", required = true)
                                                   @PathVariable("id") Long id,
                                                   @Parameter(description = "New status", required = true)
                                                   @RequestParam("status") com.project.timeOffManager.enums.Status status) {

        RequestDto updatedRequest = requestService.updateStatus(id, status);

        if (updatedRequest == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedRequest);
    }

    @Operation(summary = "Delete a request by ID")
    @ApiResponse(responseCode = "204", description = "Request deleted successfully")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@Parameter(description = "ID of the request to delete", required = true)
                                              @PathVariable("id") Long id) {

        if (!requestService.deleteRequest(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all pending requests by manager ID")
    @ApiResponse(responseCode = "200", description = "Pending requests found")
    @ApiResponse(responseCode = "404", description = "No pending requests found")
    @GetMapping("/pending/{managerId}")
    public ResponseEntity<Page<RequestDto>> getPendingRequestsByManagerId(
            @Parameter(description = "ID of the manager to retrieve pending requests for", required = true)
            @PathVariable("managerId") Long managerId,
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<Request> pendingRequests = requestService.getPendingRequestsByManagerId(managerId, pageable);

        if (pendingRequests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<RequestDto> requestDtos = pendingRequests.map(requestMapper::toDto);

        return ResponseEntity.ok(requestDtos);

    }

    @Operation(summary = "Get all requests by employee ID")
    @ApiResponse(responseCode = "200", description = "Requests found")
    @ApiResponse(responseCode = "404", description = "No requests found for the employee")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<RequestDto>> getRequestsByEmployeeId(
            @Parameter(description = "ID of the employee to retrieve requests for", required = true)
            @PathVariable("employeeId") Long employeeId,
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<Request> requests = requestService.getRequestsByEmployeeId(employeeId, pageable);

        if (requests.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Page<RequestDto> requestDtos = requests.map(requestMapper::toDto);

        return ResponseEntity.ok(requestDtos);
    }
}
