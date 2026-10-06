package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.RequestDto;
import com.project.timeOffManager.entity.Request;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RequestMapper {

    Request toEntity(RequestDto requestDto);

    RequestDto toDto(Request request);

    List<RequestDto> toDtoList(List<Request> requests);

    List<Request> toEntityList(List<RequestDto> requestDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateRequestFromDto(RequestDto requestDto, @MappingTarget Request request);
}
