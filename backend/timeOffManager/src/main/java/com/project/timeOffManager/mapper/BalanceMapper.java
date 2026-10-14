package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.BalanceDto;
import com.project.timeOffManager.entity.Balance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BalanceMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    BalanceDto toDto(Balance balance);
}
