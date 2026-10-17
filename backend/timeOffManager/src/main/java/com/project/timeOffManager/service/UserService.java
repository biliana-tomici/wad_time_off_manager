package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateUserCommand;
import com.project.timeOffManager.dto.command.UpdateUserCommand;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       ProjectRepository projectRepository,
                       AuthorizationService authorizationService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
    }

    public UserDto getUserById(Long customerId) {
        authorizationService.checkCanViewUser(customerId);
        User user = userRepository.findCustomerById(customerId);
        if (user == null || !user.isActive()) {
            throw new ResourceNotFoundException("User not found with id: " + customerId);
        }

        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findByActiveTrue());
    }

    @Transactional
    public UserDto createUser(CreateUserCommand command) {
        authorizationService.checkCanManageUsers();

        User manager = command.managerId() != null ? findUser(command.managerId()) : null;
        User savedUser = userRepository.save(new User(command.firstName().trim(), command.lastName().trim(), manager, command.role()));

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto updateUser(UpdateUserCommand command, Long customerId) {
        authorizationService.checkCanManageUsers();

        User existingUser = findUser(customerId);

        if (command.firstName() != null) {
            if (command.firstName().isBlank()) {
                throw new BusinessRuleException("First name cannot be blank");
            }
            existingUser.setFirstName(command.firstName().trim());
        }
        if (command.lastName() != null) {
            if (command.lastName().isBlank()) {
                throw new BusinessRuleException("Last name cannot be blank");
            }
            existingUser.setLastName(command.lastName().trim());
        }
        if (command.role() != null) {
            existingUser.setRole(command.role());
        }
        if (Boolean.TRUE.equals(command.clearManager()) || (command.managerId() != null && command.managerId() <= 0)) {
            existingUser.setManager(null);
        } else if (command.managerId() != null) {
            if (command.managerId().equals(customerId)) {
                throw new BusinessRuleException("A user cannot be their own manager");
            }
            existingUser.setManager(findUser(command.managerId()));
        }

        return userMapper.toDto(userRepository.save(existingUser));
    }

    private User findUser(Long userId) {
        User user = userRepository.findCustomerById(userId);
        if (user == null || !user.isActive()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return user;
    }

    @Transactional
    public boolean deleteUser(Long customerId) {
        authorizationService.checkCanManageUsers();

        User user = userRepository.findCustomerById(customerId);
        if (user == null || !user.isActive()) {
            throw new ResourceNotFoundException("User not found with id: " + customerId);
        }

        List<User> subordinates = userRepository.findByManager_Id(customerId);
        for (User sub : subordinates) {
            sub.setManager(null);
            userRepository.save(sub);
        }

        List<Project> managedProjects = projectRepository.findByManagerId(customerId);
        for (Project proj : managedProjects) {
            proj.setManager(null);
            projectRepository.save(proj);
        }

        for (Project proj : projectRepository.findAll()) {
            if (proj.getEmployees() != null && proj.getEmployees().removeIf(u -> customerId.equals(u.getId()))) {
                projectRepository.save(proj);
            }
        }

        user.setActive(false);
        userRepository.save(user);
        return true;
    }

    public List<User> getUsersByProjectId(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }
}
