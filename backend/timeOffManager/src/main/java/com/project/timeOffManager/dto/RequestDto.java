package com.project.timeOffManager.dto;

import com.project.timeOffManager.enums.Status;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link com.project.timeOffManager.entity.Request}
 */
public record RequestDto(Long id,
                         UserDto employee,
                         BalanceDto balance,
                         Status status,
                         LocalDate startDate,
                         LocalDate endDate,
                         String explanation,
                         LocalDate creationDate,
                         LocalDate modifiedDate) implements Serializable {
}