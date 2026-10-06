package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.ManagerDto;
import com.project.timeOffManager.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    User toEntity(ManagerDto manager);

    ManagerDto toDto(User user);
}
