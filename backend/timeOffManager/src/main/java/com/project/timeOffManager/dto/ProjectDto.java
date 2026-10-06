package com.project.timeOffManager.dto;

import java.io.Serializable;

/**
 * DTO for {@link com.project.timeOffManager.entity.Project}
 */
public record ProjectDto(Long id,
                         Long minEmp,
                         String name,
                         ManagerDto manager
                         // Removed List<UserDto> employees to avoid recursion
) implements Serializable {
}
