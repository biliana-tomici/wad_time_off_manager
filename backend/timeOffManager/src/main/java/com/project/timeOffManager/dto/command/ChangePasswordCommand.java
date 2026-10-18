package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotBlank;

public record ChangePasswordCommand(@NotBlank(message = "Current password is required")
                                    String currentPassword,
                                    String newPassword,
                                    Boolean generateRandomPassword) {

    public ChangePasswordCommand(String currentPassword, String newPassword) {
        this(currentPassword, newPassword, false);
    }
}
