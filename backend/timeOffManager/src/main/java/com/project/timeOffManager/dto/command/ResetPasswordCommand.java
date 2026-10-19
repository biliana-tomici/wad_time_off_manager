package com.project.timeOffManager.dto.command;

public record ResetPasswordCommand(String newPassword,
                                   Boolean generateRandomPassword) {

    public ResetPasswordCommand(String newPassword) {
        this(newPassword, false);
    }
}
