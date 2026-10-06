package com.project.timeOffManager.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.project.timeOffManager.entity.Balance}
 */
public record BalanceDto(Long id,
                         UserDto employee,
                         Long daysLeft,
                         String name,
                         Long maxDays) implements Serializable {
}