package com.project.timeOffManager.controller;

import com.project.timeOffManager.dto.AuthResponseDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.ChangePasswordCommand;
import com.project.timeOffManager.dto.command.LoginCommand;
import com.project.timeOffManager.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Authenticate user with email and password and receive a signed JWT")
    @ApiResponse(responseCode = "200", description = "Authentication successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginCommand command) {
        return ResponseEntity.ok(authService.login(command));
    }

    @Operation(summary = "Get currently authenticated user profile and role")
    @ApiResponse(responseCode = "200", description = "Current user returned")
    @ApiResponse(responseCode = "401", description = "Not authenticated")
    @GetMapping("/me")
    public ResponseEntity<UserDto> me() {
        return ResponseEntity.ok(authService.getCurrentUser());
    }

    @Operation(summary = "Change password for the currently authenticated user")
    @ApiResponse(responseCode = "204", description = "Password changed successfully")
    @ApiResponse(responseCode = "409", description = "Current password mismatch or invalid new password")
    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordCommand command) {
        authService.changeOwnPassword(command);
        return ResponseEntity.noContent().build();
    }
}
