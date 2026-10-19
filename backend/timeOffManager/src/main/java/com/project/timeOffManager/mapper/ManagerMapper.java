package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.ManagerDto;
import com.project.timeOffManager.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ManagerMapper {

    ManagerDto toDto(User user);
}
