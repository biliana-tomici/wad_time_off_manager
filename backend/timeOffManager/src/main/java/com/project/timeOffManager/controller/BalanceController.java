package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.dto.command.CreateBalanceCommand;
import com.project.timeOffManager.dto.command.UpdateBalanceCommand;
import jakarta.validation.Valid;
import com.project.timeOffManager.service.BalanceService;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/balance", "/api/balances"})
public class BalanceController {

    private final BalanceService balanceService;

    @Autowired
    public BalanceController(BalanceService balanceService) {
        this.balanceService = balanceService;
    }

    @Operation(summary = "Get balance by ID")
    @ApiResponse(responseCode = "200", description = "Balance found")
    @ApiResponse(responseCode = "404", description = "Balance not found")
    @GetMapping("/{id}")
    public ResponseEntity<BalanceDto> getBalanceById(
            @Parameter(description = "ID of the balance to retrieve", required = true)
            @PathVariable("id") Long id) {
        return ResponseEntity.ok(balanceService.getBalanceById(id));
    }

    @Operation(summary = "Create a new balance")
    @ApiResponse(responseCode = "201", description = "Balance created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid balance data")
    @PostMapping
    public ResponseEntity<BalanceDto> createBalance(
            @Parameter(description = "The balance object to be created", required = true)
            @Valid @RequestBody CreateBalanceCommand command) {
        BalanceDto createdBalance = balanceService.createBalance(command);
        return ResponseEntity.status(201).body(createdBalance);
    }

    @Operation(summary = "Update an existing balance")
    @ApiResponse(responseCode = "200", description = "Balance updated successfully")
    @ApiResponse(responseCode = "400", description = "Invalid balance data")
    @ApiResponse(responseCode = "404", description = "Balance not found")
    @PutMapping("/{id}")
    public ResponseEntity<BalanceDto> updateBalance(
            @Parameter(description = "ID of the balance to update", required = true)
            @PathVariable("id") Long id,
            @Parameter(description = "Updated balance object", required = true)
            @Valid @RequestBody UpdateBalanceCommand command) {
        BalanceDto updatedBalance = balanceService.updateBalance(id, command);
        return ResponseEntity.ok(updatedBalance);
    }

    @Operation(summary = "Delete a balance by ID")
    @ApiResponse(responseCode = "204", description = "Balance deleted successfully")
    @ApiResponse(responseCode = "404", description = "Balance not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBalance(
            @Parameter(description = "ID of the balance to delete", required = true)
            @PathVariable("id") Long id) {
        if (!balanceService.deleteBalance(id)) {
            throw new ResourceNotFoundException("Balance not found with id: " + id);
        }
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all balances by employee ID")
    @ApiResponse(responseCode = "200", description = "Balances found")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<Page<BalanceDto>> getBalancesByEmployeeId(
            @Parameter(description = "ID of the employee to retrieve balances for", required = true)
            @PathVariable("employeeId") Long employeeId,
            @Parameter(description = "Pagination information", required = false)
            Pageable pageable) {
        Page<BalanceDto> balances = balanceService.getBalancesByEmployeeId(employeeId, pageable);
        return ResponseEntity.ok(balances);
    }
}
