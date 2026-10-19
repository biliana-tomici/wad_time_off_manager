package com.project.timeOffManager.dto;

import java.io.Serializable;
import java.time.Instant;

public record AuthResponseDto(String token,
                              String tokenType,
                              long expiresIn,
                              Instant expiresAt,
                              UserDto user) implements Serializable {
}
