package com.project.timeOffManager.dto;

import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.enums.Role;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link User}
 */
public record UserDto(Long id,
                      String firstName,
                      String lastName,
                      String email,
                      Role role,
                      List<ProjectDto> projects,
                      ManagerDto manager) implements Serializable {
}
