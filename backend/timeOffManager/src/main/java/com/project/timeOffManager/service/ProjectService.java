package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.mapper.ProjectMapper;
import com.project.timeOffManager.repository.ProjectRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
    }

    public ProjectDto getProjectById(Long projectId) {

        Project project = projectRepository.findProjectById(projectId);

        return projectMapper.toDto(project);
    }

    public ProjectDto createProject(ProjectDto projectDto) {

        Project project = projectMapper.toEntity(projectDto);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toDto(savedProject);
    }

    public ProjectDto updateProject(ProjectDto projectDto, Long projectId) {

        Project existingProject = projectRepository.findProjectById(projectId);

        if (existingProject == null) {
            return null;
        }

        projectMapper.updateProjectFromDto(projectDto, existingProject);

        return projectMapper.toDto(projectRepository.save(existingProject));
    }

    public boolean deleteProject(Long projectId) {

        if (projectRepository.existsById(projectId)) {
            projectRepository.deleteById(projectId);
            return true;
        }

        return false;
    }

    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

}
