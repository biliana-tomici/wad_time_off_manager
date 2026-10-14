package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateUserCommand;
import com.project.timeOffManager.dto.command.UpdateUserCommand;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
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
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + customerId);
        }

        return userMapper.toDto(user);
    }

    public List<UserDto> getAllUsers() {
        return userMapper.toDtoList(userRepository.findAll());
    }

    @Transactional
    public UserDto createUser(CreateUserCommand command) {
        authorizationService.checkCanManageUsers();

        User manager = command.managerId() != null ? findUser(command.managerId()) : null;
        User savedUser = userRepository.save(new User(command.firstName(), command.lastName(), manager, command.role()));

        return userMapper.toDto(savedUser);
    }

    public UserDto updateUser(UpdateUserCommand command, Long customerId) {
        authorizationService.checkCanManageUsers();

        User existingUser = findUser(customerId);

        if (command.firstName() != null) {
            existingUser.setFirstName(command.firstName());
        }
        if (command.lastName() != null) {
            existingUser.setLastName(command.lastName());
        }
        if (command.role() != null) {
            existingUser.setRole(command.role());
        }
        if (command.managerId() != null) {
            existingUser.setManager(findUser(command.managerId()));
        }

        return userMapper.toDto(userRepository.save(existingUser));
    }

    private User findUser(Long userId) {
        User user = userRepository.findCustomerById(userId);
        if (user == null) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return user;
    }

    @Transactional
    public boolean deleteUser(Long customerId) {
        authorizationService.checkCanManageUsers();

        User user = userRepository.findCustomerById(customerId);
        if (user == null) {
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

        if (user.getProjects() != null) {
            user.getProjects().clear();
            userRepository.save(user);
        }

        userRepository.delete(user);
        return true;
    }

    public List<User> getUsersByProjectId(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }
}
