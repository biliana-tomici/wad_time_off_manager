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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    public static final String DEFAULT_INITIAL_PASSWORD = "password123";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       ProjectRepository projectRepository,
                       AuthorizationService authorizationService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
        this.passwordEncoder = passwordEncoder;
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

        String firstName = command.firstName().trim();
        String lastName = command.lastName().trim();
        String email;
        if (command.email() != null && !command.email().isBlank()) {
            email = command.email().trim().toLowerCase();
        } else {
            email = generateDefaultEmail(firstName, lastName);
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessRuleException("A user with email '" + email + "' already exists");
        }

        String rawPassword = (command.password() != null && !command.password().isBlank())
                ? command.password()
                : DEFAULT_INITIAL_PASSWORD;
        if (rawPassword.length() < 6) {
            throw new BusinessRuleException("Password must be at least 6 characters");
        }

        User manager = command.managerId() != null ? findUser(command.managerId()) : null;
        User user = new User(firstName, lastName, email, passwordEncoder.encode(rawPassword), manager, command.role());
        User savedUser = userRepository.save(user);

        return userMapper.toDto(savedUser);
    }

    private String generateDefaultEmail(String firstName, String lastName) {
        String base = (firstName + "." + lastName).toLowerCase().replaceAll("[^a-z0-9.]", "");
        String candidate = base + "@timeoff.com";
        int suffix = 1;
        while (userRepository.existsByEmailIgnoreCase(candidate)) {
            candidate = base + suffix + "@timeoff.com";
            suffix++;
        }
        return candidate;
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
        if (command.email() != null && !command.email().isBlank()) {
            String newEmail = command.email().trim().toLowerCase();
            if (!newEmail.equalsIgnoreCase(existingUser.getEmail())
                    && userRepository.existsByEmailIgnoreCase(newEmail)) {
                throw new BusinessRuleException("A user with email '" + newEmail + "' already exists");
            }
            existingUser.setEmail(newEmail);
        }
        if (command.password() != null && !command.password().isBlank()) {
            if (command.password().length() < 6) {
                throw new BusinessRuleException("Password must be at least 6 characters");
            }
            existingUser.setPasswordHash(passwordEncoder.encode(command.password()));
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

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        authorizationService.checkCanManageUsers();
        if (newPassword == null || newPassword.length() < 6) {
            throw new BusinessRuleException("New password must be at least 6 characters");
        }
        User user = findUser(userId);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
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
