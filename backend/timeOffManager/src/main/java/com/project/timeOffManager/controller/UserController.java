package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customer")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Get all customers")
    @ApiResponse(responseCode = "200", description = "Users found")
    @GetMapping
    public ResponseEntity<List<UserDto>> getAllCustomers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @Operation(summary = "Get customer by ID")
    @ApiResponse(responseCode = "200", description = "User found")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getCustomerById(@Parameter(description = "ID of the customer to retrieve")
                                                   @PathVariable("id") Long id) {

        UserDto user = userService.getUserById(id);

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Create a new customer")
    @ApiResponse(responseCode = "201", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid customer data")
    @PostMapping
    public ResponseEntity<UserDto> createCustomer(@Parameter(description = "User object to be created")
                                                  @RequestBody UserDto userDto) {

        UserDto createdUser = userService.createUser(userDto);

        if (createdUser == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(201).body(createdUser);
    }

    @Operation(summary = "Update an existing customer")
    @ApiResponse(responseCode = "200", description = "User updated successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateCustomer(@Parameter(description = "ID of the customer to update")
                                                  @PathVariable("id") Long id,
                                                  @Parameter(description = "Updated customer object")
                                                  @RequestBody UserDto userDto) {

        UserDto updatedUser = userService.updateUser(userDto, id);

        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Delete a customer by ID")
    @ApiResponse(responseCode = "204", description = "User deleted successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@Parameter(description = "ID of the customer to delete")
                                               @PathVariable("id") Long id) {

        if (!userService.deleteUser(id)) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
