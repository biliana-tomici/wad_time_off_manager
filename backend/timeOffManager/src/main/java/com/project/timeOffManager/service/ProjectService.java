package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.ProjectDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateProjectCommand;
import com.project.timeOffManager.dto.command.UpdateProjectCommand;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.ProjectMapper;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMapper projectMapper;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthorizationService authorizationService;

    @Autowired
    public ProjectService(ProjectRepository projectRepository,
                          ProjectMapper projectMapper,
                          UserRepository userRepository,
                          UserMapper userMapper,
                          AuthorizationService authorizationService) {
        this.projectRepository = projectRepository;
        this.projectMapper = projectMapper;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.authorizationService = authorizationService;
    }

    public ProjectDto getProjectById(Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanViewProject(project);

        return projectMapper.toDto(project);
    }

    public ProjectDto createProject(CreateProjectCommand command) {
        Project project = new Project(null, command.minEmp(), command.name(), findManager(command.managerId()));
        authorizationService.checkCanCreateProject(project);
        Project savedProject = projectRepository.save(project);

        return projectMapper.toDto(savedProject);
    }

    public ProjectDto updateProject(UpdateProjectCommand command, Long projectId) {
        Project existingProject = projectRepository.findProjectById(projectId);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanManageProject(existingProject);

        if (command.name() != null) {
            existingProject.setName(command.name());
        }
        if (command.minEmp() != null) {
            existingProject.setMinEmp(command.minEmp());
        }
        if (command.managerId() != null) {
            existingProject.setManager(findManager(command.managerId()));
        }

        return projectMapper.toDto(projectRepository.save(existingProject));
    }

    private User findManager(Long managerId) {
        User manager = userRepository.findCustomerById(managerId);
        if (manager == null) {
            throw new ResourceNotFoundException("Manager not found with id: " + managerId);
        }
        return manager;
    }

    public boolean deleteProject(Long projectId) {
        Project existingProject = projectRepository.findProjectById(projectId);
        if (existingProject == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanDeleteProject(existingProject);
        projectRepository.delete(existingProject);
        return true;
    }

    public Page<ProjectDto> getAllProjects(Pageable pageable) {
        return projectRepository.findAll(pageable).map(projectMapper::toDto);
    }

    @Transactional
    public List<UserDto> getProjectMembers(Long projectId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanViewProject(project);

        if (project.getEmployees() == null) {
            return List.of();
        }
        return userMapper.toDtoList(project.getEmployees());
    }

    @Transactional
    public boolean addMember(Long projectId, Long employeeId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanManageProject(project);

        User employee = userRepository.findCustomerById(employeeId);
        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        if (project.getEmployees() == null) {
            project.setEmployees(new ArrayList<>());
        }
        if (!project.getEmployees().contains(employee)) {
            project.getEmployees().add(employee);
            projectRepository.save(project);
        }
        return true;
    }

    @Transactional
    public boolean removeMember(Long projectId, Long employeeId) {
        Project project = projectRepository.findProjectById(projectId);
        if (project == null) {
            throw new ResourceNotFoundException("Project not found with id: " + projectId);
        }
        authorizationService.checkCanManageProject(project);

        User employee = userRepository.findCustomerById(employeeId);
        if (employee == null) {
            throw new ResourceNotFoundException("Employee not found with id: " + employeeId);
        }

        if (project.getEmployees() == null) {
            return false;
        }
        boolean removed = project.getEmployees().remove(employee);
        if (removed) {
            projectRepository.save(project);
        }
        return removed;
    }
}
