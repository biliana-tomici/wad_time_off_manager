package com.project.timeOffManager.security;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
public class PasswordGenerator {

    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private static final String SPECIAL = "!@#$%^&*-_=+";
    private static final String ALL = UPPER + LOWER + DIGITS + SPECIAL;

    private final SecureRandom secureRandom = new SecureRandom();

    public String generateSecurePassword() {
        return generateSecurePassword(12);
    }

    public String generateSecurePassword(int length) {
        int finalLength = Math.max(8, length);
        char[] chars = new char[finalLength];

        chars[0] = UPPER.charAt(secureRandom.nextInt(UPPER.length()));
        chars[1] = LOWER.charAt(secureRandom.nextInt(LOWER.length()));
        chars[2] = DIGITS.charAt(secureRandom.nextInt(DIGITS.length()));
        chars[3] = SPECIAL.charAt(secureRandom.nextInt(SPECIAL.length()));

        for (int i = 4; i < finalLength; i++) {
            chars[i] = ALL.charAt(secureRandom.nextInt(ALL.length()));
        }

        // Fisher-Yates shuffle
        for (int i = finalLength - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }

        return new String(chars);
    }

    public String generateResetToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
