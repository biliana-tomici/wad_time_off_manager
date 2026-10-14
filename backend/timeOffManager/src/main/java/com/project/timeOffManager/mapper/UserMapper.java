package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.entity.User;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, ManagerMapper.class})
public interface UserMapper {

    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);
}
