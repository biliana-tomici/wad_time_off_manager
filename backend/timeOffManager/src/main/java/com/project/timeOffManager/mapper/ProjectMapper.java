package com.project.timeOffManager.mapper;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Project;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = ManagerMapper.class)
public interface ProjectMapper {

    ProjectDto toDto(Project project);
}
