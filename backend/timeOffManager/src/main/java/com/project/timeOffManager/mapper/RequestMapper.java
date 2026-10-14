package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.entity.Request;
import com.project.timeOffManager.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeName", source = "employee")
    @Mapping(target = "balanceId", source = "balance.id")
    @Mapping(target = "balanceName", source = "balance.name")
    @Mapping(target = "decidedById", source = "decidedBy.id")
    @Mapping(target = "decidedByName", source = "decidedBy")
    RequestDto toDto(Request request);

    default String fullName(User user) {
        return user == null ? null : user.getFirstName() + " " + user.getLastName();
    }
}
