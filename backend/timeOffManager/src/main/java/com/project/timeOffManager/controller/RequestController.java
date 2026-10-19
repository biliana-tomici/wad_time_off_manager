package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.dto.command.CreateRequestCommand;
import com.project.timeOffManager.dto.command.UpdateRequestCommand;
import jakarta.validation.Valid;
import com.project.timeOffManager.service.RequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping({"/api/request", "/api/requests"})
public class RequestController {

    private final RequestService requestService;

    @Autowired
    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @Operation(summary = "Get request by ID")
    @ApiResponse(responseCode = "200", description = "Request found")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @GetMapping("/{id}")
    public ResponseEntity<RequestDto> getRequestById(@Parameter(description = "ID of the request to retrieve", required = true)
                                                     @PathVariable("id") Long id) {
        return ResponseEntity.ok(requestService.getRequestById(id));
    }

    @Operation(summary = "Create a new request")
    @ApiResponse(responseCode = "201", description = "Request created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @PostMapping
    public ResponseEntity<RequestDto> createRequest(@Parameter(description = "The request object", required = true)
                                                    @Valid @RequestBody CreateRequestCommand command) {
        RequestDto created = requestService.createRequest(command);
        return ResponseEntity.status(201).body(created);
    }

    @Operation(summary = "Update an existing request")
    @ApiResponse(responseCode = "200", description = "Request updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid request data")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PutMapping("/{id}")
    public ResponseEntity<RequestDto> updateRequest(@Parameter(description = "ID of the request to update", required = true)
                                                    @PathVariable("id") Long id,
                                                    @Parameter(description = "Updated request object", required = true)
                                                    @Valid @RequestBody UpdateRequestCommand command) {
        return ResponseEntity.ok(requestService.updateRequest(command, id));
    }

    @Operation(summary = "Cancel a request")
    @ApiResponse(responseCode = "200", description = "Request cancelled")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<RequestDto> cancelRequest(@PathVariable("id") Long id) {
        return ResponseEntity.ok(requestService.cancelRequest(id));
    }

    @Operation(summary = "Approve a request")
    @ApiResponse(responseCode = "200", description = "Request approved")
    @PostMapping("/{id}/approve")
    public ResponseEntity<RequestDto> approveRequest(@PathVariable("id") Long id,
                                                     @RequestParam(value = "comment", required = false) String comment,
                                                     @RequestBody(required = false) Map<String, String> body) {
        String effectiveComment = comment != null ? comment : (body != null ? body.get("comment") : null);
        return ResponseEntity.ok(requestService.approveRequest(id, effectiveComment));
    }

    @Operation(summary = "Reject a request")
    @ApiResponse(responseCode = "200", description = "Request rejected")
    @PostMapping("/{id}/reject")
    public ResponseEntity<RequestDto> rejectRequest(@PathVariable("id") Long id,
                                                    @RequestParam(value = "comment", required = false) String comment,
                                                    @RequestBody(required = false) Map<String, String> body) {
        String effectiveComment = comment != null ? comment : (body != null ? body.get("comment") : null);
        return ResponseEntity.ok(requestService.rejectRequest(id, effectiveComment));
    }

    @Operation(summary = "Update the status of a request")
    @ApiResponse(responseCode = "200", description = "Status updated")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @PatchMapping("/{id}/status")
    public ResponseEntity<RequestDto> updateStatus(@Parameter(description = "ID of the request", required = true)
                                                   @PathVariable("id") Long id,
                                                   @Parameter(description = "New status", required = true)
                                                   @RequestParam("status") com.project.timeOffManager.enums.Status status) {
        return ResponseEntity.ok(requestService.updateStatus(id, status));
    }

    @Operation(summary = "Delete a request by ID")
    @ApiResponse(responseCode = "204", description = "Request deleted successfully")
    @ApiResponse(responseCode = "404", description = "Request not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRequest(@Parameter(description = "ID of the request to delete", required = true)
                                              @PathVariable("id") Long id) {
        if (!requestService.deleteRequest(id)) {
            throw new ResourceNotFoundException("Request not found with id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all pending requests by manager ID")
    @ApiResponse(responseCode = "200", description = "Pending requests found")
    @GetMapping("/pending/{managerId}")
    public ResponseEntity<Page<RequestDto>> getPendingRequestsByManagerId(
            @Parameter(description = "ID of the manager to retrieve pending requests for", required = true)
            @PathVariable("managerId") Long managerId,
            @Parameter(description = "Whether to include pending requests for employees without a manager", required = false)
            @RequestParam(value = "includeUnassigned", defaultValue = "false") boolean includeUnassigned,
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<RequestDto> pendingRequests = requestService.getPendingRequestsByManagerId(managerId, includeUnassigned, pageable);
        return ResponseEntity.ok(pendingRequests);
    }

    @Operation(summary = "Get all requests by employee ID")
    @ApiResponse(responseCode = "200", description = "Requests found")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<RequestDto>> getRequestsByEmployeeId(
            @Parameter(description = "ID of the employee to retrieve requests for", required = true)
            @PathVariable("employeeId") Long employeeId,
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {

        Page<RequestDto> requests = requestService.getRequestsByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(requests);
    }
}
