package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.ProjectMapper;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;

    @Autowired
    public ProjectService(ProjectRepository projectRepository, ProjectMapper projectMapper, UserRepository userRepository) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
    }

    public ProjectDto getProjectById(Long projectId) {

        Project project = projectRepository.findProjectById(projectId);

        return projectMapper.toDto(project);
    }

    public ProjectDto createProject(ProjectDto projectDto) {

        Project project = projectMapper.toEntity(projectDto);
        if (!attachManager(project, projectDto)) {
            return null;
        }
        Project savedProject = projectRepository.save(project);

        return projectMapper.toDto(savedProject);
    }

    public ProjectDto updateProject(ProjectDto projectDto, Long projectId) {

        Project existingProject = projectRepository.findProjectById(projectId);

        if (existingProject == null) {
            return null;
        }

        if (projectDto.name() != null) {
            existingProject.setName(projectDto.name());
        }
        if (projectDto.minEmp() != null) {
            existingProject.setMinEmp(projectDto.minEmp());
        }
        if (projectDto.manager() != null && !attachManager(existingProject, projectDto)) {
            return null;
        }

        return projectMapper.toDto(projectRepository.save(existingProject));
    }

    private boolean attachManager(Project project, ProjectDto projectDto) {
        if (projectDto.manager() == null || projectDto.manager().id() == null) {
            return project.getManager() != null;
        }
        User manager = userRepository.findCustomerById(projectDto.manager().id());
        if (manager == null) {
            return false;
        }
        project.setManager(manager);
        return true;
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
