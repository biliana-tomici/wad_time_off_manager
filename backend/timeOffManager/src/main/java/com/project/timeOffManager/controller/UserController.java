package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateUserCommand;
import com.project.timeOffManager.dto.command.UpdateUserCommand;
import jakarta.validation.Valid;
import com.project.timeOffManager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/users", "/api/customer"})
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all users")
    @ApiResponse(responseCode = "200", description = "Users found")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getCustomerById(@Parameter(description = "ID of the user to retrieve")
                                                   @PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @Operation(summary = "Create a new user")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @PostMapping
    public ResponseEntity<UserDto> createCustomer(@Parameter(description = "User object to be created")
                                                  @Valid @RequestBody CreateUserCommand command) {
        return ResponseEntity.status(201).body(userService.createUser(command));
    }

    @Operation(summary = "Update an existing user")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateCustomer(@Parameter(description = "ID of the user to update")
                                                  @PathVariable("id") Long id,
                                                  @Parameter(description = "Updated user object")
                                                  @Valid @RequestBody UpdateUserCommand command) {
        return ResponseEntity.ok(userService.updateUser(command, id));
    }

    @Operation(summary = "Delete a user by ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@Parameter(description = "ID of the user to delete")
                                               @PathVariable("id") Long id) {

        if (!userService.deleteUser(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }

        return ResponseEntity.noContent().build();
    }
}
