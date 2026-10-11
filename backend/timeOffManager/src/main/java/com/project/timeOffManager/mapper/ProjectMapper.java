package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Project;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(componentModel = "spring", uses = ManagerMapper.class)
public interface ProjectMapper {

    Project toEntity(ProjectDto project);

    ProjectDto toDto(Project project);

    List<ProjectDto> toDtoList(List<Project> projects);

    List<Project> toEntityList(List<ProjectDto> projectDtos);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateProjectFromDto(ProjectDto projectDto, @MappingTarget Project project);
}
