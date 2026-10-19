package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.CreateUserCommand;
import com.project.timeOffManager.dto.command.ResetPasswordCommand;
import com.project.timeOffManager.dto.command.UpdateUserCommand;
import com.project.timeOffManager.entity.PasswordResetToken;
import com.project.timeOffManager.entity.Project;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.PasswordResetTokenRepository;
import com.project.timeOffManager.repository.ProjectRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.exception.ResourceNotFoundException;
import com.project.timeOffManager.security.AuthorizationService;
import com.project.timeOffManager.security.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    public static final String DEFAULT_INITIAL_PASSWORD = "password123";
    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(24);

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final ProjectRepository projectRepository;
    private final AuthorizationService authorizationService;
    private final PasswordEncoder passwordEncoder;
    private final PasswordGenerator passwordGenerator;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       ProjectRepository projectRepository,
                       AuthorizationService authorizationService,
                       PasswordEncoder passwordEncoder,
                       PasswordGenerator passwordGenerator,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.projectRepository = projectRepository;
        this.authorizationService = authorizationService;
        this.passwordEncoder = passwordEncoder;
        this.passwordGenerator = passwordGenerator;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
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

        String rawPassword;
        if (Boolean.TRUE.equals(command.generateRandomPassword())) {
            rawPassword = passwordGenerator.generateSecurePassword();
        } else if (command.password() != null && !command.password().isBlank()) {
            rawPassword = command.password();
        } else {
            rawPassword = DEFAULT_INITIAL_PASSWORD;
        }
        if (rawPassword.length() < 6) {
            throw new BusinessRuleException("Password must be at least 6 characters");
        }

        User manager = command.managerId() != null ? findUser(command.managerId()) : null;
        User user = new User(firstName, lastName, email, passwordEncoder.encode(rawPassword), manager, command.role());
        User savedUser = userRepository.save(user);

        String setupToken = issueResetToken(savedUser);
        emailService.sendWelcomeEmail(savedUser, rawPassword, setupToken);

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

        String updatedRawPassword = null;
        if (Boolean.TRUE.equals(command.generateRandomPassword())) {
            updatedRawPassword = passwordGenerator.generateSecurePassword();
            existingUser.setPasswordHash(passwordEncoder.encode(updatedRawPassword));
        } else if (command.password() != null && !command.password().isBlank()) {
            if (command.password().length() < 6) {
                throw new BusinessRuleException("Password must be at least 6 characters");
            }
            updatedRawPassword = command.password();
            existingUser.setPasswordHash(passwordEncoder.encode(updatedRawPassword));
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

        User saved = userRepository.save(existingUser);
        if (updatedRawPassword != null) {
            String token = issueResetToken(saved);
            emailService.sendAdminPasswordResetEmail(saved, updatedRawPassword, token);
        }

        return userMapper.toDto(saved);
    }

    @Transactional
    public void resetUserPassword(Long userId, String newPassword) {
        resetUserPassword(userId, new ResetPasswordCommand(newPassword, false));
    }

    @Transactional
    public void resetUserPassword(Long userId, ResetPasswordCommand command) {
        authorizationService.checkCanManageUsers();

        String resolvedPassword;
        if (command != null && Boolean.TRUE.equals(command.generateRandomPassword())) {
            resolvedPassword = passwordGenerator.generateSecurePassword();
        } else {
            resolvedPassword = command != null ? command.newPassword() : null;
            if (resolvedPassword == null || resolvedPassword.length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }
        }

        User user = findUser(userId);
        user.setPasswordHash(passwordEncoder.encode(resolvedPassword));
        User saved = userRepository.save(user);

        String resetToken = issueResetToken(saved);
        emailService.sendAdminPasswordResetEmail(saved, resolvedPassword, resetToken);
    }

    private String issueResetToken(User user) {
        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUser_IdAndUsedFalse(user.getId());
        for (PasswordResetToken existing : activeTokens) {
            existing.setUsed(true);
        }
        if (!activeTokens.isEmpty()) {
            passwordResetTokenRepository.saveAll(activeTokens);
        }

        Instant now = Instant.now();
        String tokenValue = passwordGenerator.generateResetToken();
        PasswordResetToken resetToken = new PasswordResetToken(tokenValue, user, now.plus(RESET_TOKEN_TTL), now);
        passwordResetTokenRepository.save(resetToken);
        return tokenValue;
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

        List<PasswordResetToken> activeTokens = passwordResetTokenRepository.findByUser_IdAndUsedFalse(customerId);
        for (PasswordResetToken token : activeTokens) {
            token.setUsed(true);
        }
        if (!activeTokens.isEmpty()) {
            passwordResetTokenRepository.saveAll(activeTokens);
        }

        user.setActive(false);
        userRepository.save(user);
        return true;
    }

    public List<User> getUsersByProjectId(Long projectId) {
        return userRepository.findUsersByProjectId(projectId);
    }
}
