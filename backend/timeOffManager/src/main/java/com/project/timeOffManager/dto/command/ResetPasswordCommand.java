package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordCommand(@NotBlank(message = "New password is required")
                                   @Size(min = 6, message = "New password must be at least 6 characters")
                                   String newPassword) {
}
