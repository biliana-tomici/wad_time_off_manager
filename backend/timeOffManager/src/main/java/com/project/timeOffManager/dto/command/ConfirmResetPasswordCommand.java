package com.project.timeOffManager.dto.command;

import jakarta.validation.constraints.NotBlank;

public record ConfirmResetPasswordCommand(@NotBlank(message = "Reset token is required")
                                          String token,
                                          String newPassword,
                                          Boolean generateRandomPassword) {

    public ConfirmResetPasswordCommand(String token, String newPassword) {
        this(token, newPassword, false);
    }
}
