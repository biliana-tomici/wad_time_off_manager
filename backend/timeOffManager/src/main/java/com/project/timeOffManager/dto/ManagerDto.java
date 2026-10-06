package com.project.timeOffManager.dto;

import com.project.timeOffManager.entity.User;

import java.io.Serializable;

/**
 * DTO for {@link User}
 */
public record ManagerDto(Long id,
                         String firstName,
                         String lastName) implements Serializable {
}