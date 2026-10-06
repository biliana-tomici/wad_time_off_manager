package com.project.timeOffManager.dto;

import com.project.timeOffManager.entity.Balance;
import com.project.timeOffManager.entity.User;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link User}
 */
public record UserDto(Long id,
                      String firstName,
                      String lastName,
                      List<ProjectDto> projects,
                      ManagerDto manager,
                      List<BalanceDto> balances,
                      List<RequestDto> requests) implements Serializable {
}
