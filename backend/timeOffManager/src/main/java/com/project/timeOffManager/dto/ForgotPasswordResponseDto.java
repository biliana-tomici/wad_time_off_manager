package com.project.timeOffManager.dto;

public record ForgotPasswordResponseDto(String message,
                                        String resetToken,
                                        String resetLink) {
}
