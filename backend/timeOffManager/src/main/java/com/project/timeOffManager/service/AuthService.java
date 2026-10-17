package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.AuthResponseDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.ChangePasswordCommand;
import com.project.timeOffManager.dto.command.LoginCommand;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.security.CurrentUserProvider;
import com.project.timeOffManager.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       CurrentUserProvider currentUserProvider,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
        this.userMapper = userMapper;
    }

    public AuthResponseDto login(LoginCommand command) {
        if (command.email() == null || command.password() == null) {
            throw new BadCredentialsException("Invalid email or password");
        }
        User user = userRepository.findByEmailIgnoreCase(command.email().trim())
                .filter(User::isActive)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (user.getPasswordHash() == null || !passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);
        long expirationMs = jwtService.getExpirationMs();
        Instant expiresAt = Instant.now().plusMillis(expirationMs);

        return new AuthResponseDto(
                token,
                "Bearer",
                expirationMs / 1000,
                expiresAt,
                userMapper.toDto(user)
        );
    }

    public UserDto getCurrentUser() {
        User user = currentUserProvider.requireCurrentUser();
        return userMapper.toDto(user);
    }

    @Transactional
    public void changeOwnPassword(ChangePasswordCommand command) {
        User user = currentUserProvider.requireCurrentUser();
        if (user.getPasswordHash() == null || !passwordEncoder.matches(command.currentPassword(), user.getPasswordHash())) {
            throw new BusinessRuleException("Current password is incorrect");
        }
        if (command.newPassword() == null || command.newPassword().trim().length() < 6) {
            throw new BusinessRuleException("New password must be at least 6 characters");
        }
        user.setPasswordHash(passwordEncoder.encode(command.newPassword()));
        userRepository.save(user);
    }
}
