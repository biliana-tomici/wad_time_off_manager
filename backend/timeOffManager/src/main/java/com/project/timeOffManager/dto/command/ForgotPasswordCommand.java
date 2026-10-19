package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordCommand(@NotBlank(message = "Email is required")
                                    @Email(message = "Must be a valid email address")
                                    String email) {
}
