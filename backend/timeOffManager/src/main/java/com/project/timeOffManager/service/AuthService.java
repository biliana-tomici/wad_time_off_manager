package com.project.timeOffManager.service;

import com.project.timeOffManager.dto.AuthResponseDto;
import com.project.timeOffManager.dto.ForgotPasswordResponseDto;
import com.project.timeOffManager.dto.UserDto;
import com.project.timeOffManager.dto.command.ChangePasswordCommand;
import com.project.timeOffManager.dto.command.ConfirmResetPasswordCommand;
import com.project.timeOffManager.dto.command.LoginCommand;
import com.project.timeOffManager.entity.PasswordResetToken;
import com.project.timeOffManager.entity.User;
import com.project.timeOffManager.exception.BusinessRuleException;
import com.project.timeOffManager.mapper.UserMapper;
import com.project.timeOffManager.repository.PasswordResetTokenRepository;
import com.project.timeOffManager.repository.UserRepository;
import com.project.timeOffManager.security.CurrentUserProvider;
import com.project.timeOffManager.security.JwtService;
import com.project.timeOffManager.security.PasswordGenerator;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Duration RESET_TOKEN_TTL = Duration.ofHours(24);
    private static final String FORGOT_PASSWORD_NOTICE =
            "If an account exists for that email, a password reset link has been sent.";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CurrentUserProvider currentUserProvider;
    private final UserMapper userMapper;
    private final PasswordGenerator passwordGenerator;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       CurrentUserProvider currentUserProvider,
                       UserMapper userMapper,
                       PasswordGenerator passwordGenerator,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.currentUserProvider = currentUserProvider;
        this.userMapper = userMapper;
        this.passwordGenerator = passwordGenerator;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.emailService = emailService;
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

        boolean generated = Boolean.TRUE.equals(command.generateRandomPassword());
        String resolvedPassword;
        if (generated) {
            resolvedPassword = passwordGenerator.generateSecurePassword();
        } else {
            if (command.newPassword() == null || command.newPassword().trim().length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }
            resolvedPassword = command.newPassword();
        }

        user.setPasswordHash(passwordEncoder.encode(resolvedPassword));
        User saved = userRepository.save(user);
        emailService.sendPasswordChangedEmail(saved, generated ? resolvedPassword : null);
    }

    @Transactional
    public ForgotPasswordResponseDto requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            throw new BusinessRuleException("Email is required");
        }

        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email.trim())
                .filter(User::isActive);

        if (userOpt.isEmpty()) {
            return new ForgotPasswordResponseDto(FORGOT_PASSWORD_NOTICE, null, null);
        }

        User user = userOpt.get();
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

        emailService.sendForgotPasswordEmail(user, tokenValue);

        return new ForgotPasswordResponseDto(
                FORGOT_PASSWORD_NOTICE,
                null,
                null
        );
    }

    @Transactional
    public void resetPasswordWithToken(ConfirmResetPasswordCommand command) {
        if (command == null || command.token() == null || command.token().isBlank()) {
            throw new BusinessRuleException("Reset token is required");
        }

        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(command.token().trim())
                .orElseThrow(() -> new BusinessRuleException("Invalid or expired password reset token"));

        if (resetToken.isUsed() || resetToken.getExpiresAt().isBefore(Instant.now())) {
            throw new BusinessRuleException("Invalid or expired password reset token");
        }

        User user = resetToken.getUser();
        if (user == null || !user.isActive()) {
            throw new BusinessRuleException("Invalid or expired password reset token");
        }

        boolean generated = Boolean.TRUE.equals(command.generateRandomPassword());
        String resolvedPassword;
        if (generated) {
            resolvedPassword = passwordGenerator.generateSecurePassword();
        } else {
            if (command.newPassword() == null || command.newPassword().trim().length() < 6) {
                throw new BusinessRuleException("New password must be at least 6 characters");
            }
            resolvedPassword = command.newPassword();
        }

        user.setPasswordHash(passwordEncoder.encode(resolvedPassword));
        resetToken.setUsed(true);
        userRepository.save(user);
        passwordResetTokenRepository.save(resetToken);

        emailService.sendPasswordChangedEmail(user, generated ? resolvedPassword : null);
    }

    public String generateRandomPassword() {
        return passwordGenerator.generateSecurePassword();
    }
}
