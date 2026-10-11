package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProjectMapper.class, ManagerMapper.class})
public interface UserMapper {

    @Mapping(target = "projects", ignore = true)
    User toEntity(UserDto customer);

    @Mapping(target = "requests", ignore = true)
    @Mapping(target = "balances", ignore = true)
    UserDto toDto(User user);

    List<UserDto> toDtoList(List<User> users);

    List<User> toEntityList(List<UserDto> userDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "projects", ignore = true)
    void updateUserFromDto(UserDto userDto, @MappingTarget User user);
}
