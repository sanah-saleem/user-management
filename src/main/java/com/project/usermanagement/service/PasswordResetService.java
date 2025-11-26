package com.project.usermanagement.service;

import com.project.usermanagement.entity.PasswordResetToken;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.helper.HelperService;
import com.project.usermanagement.repository.PasswordResetTokenRepository;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordResetTokenRepository tokenRepo;
    private final PasswordEncoder encoder;
    private final HelperService helper;

    @Transactional
    public Optional<PasswordResetToken> createResetToken(String email) {
        var userOpt = userRepo.findByEmailAndDeletedFalse(email.trim());
        if (userOpt.isEmpty()) {
            return Optional.empty();
        }
        User user = userOpt.get();
        if (!AccountStatus.ACTIVE.equals(user.getStatus())) {
            return Optional.empty();
        }
        String token = helper.generateToken();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(15, ChronoUnit.MINUTES);
        var prt = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .createdAt(now)
                .expiresAt(expiresAt)
                .used(false)
                .build();
        return Optional.of(tokenRepo.save(prt));
    }
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        var token = tokenRepo.findByToken(tokenValue)
                .orElseThrow(() -> new IllegalArgumentException(MessageConstants.INVALID_OR_EXPIRED_TOKEN));
        if (token.isUsed() || token.isExpired()) {
            throw new IllegalArgumentException(MessageConstants.INVALID_OR_EXPIRED_TOKEN);
        }
        var user = token.getUser();
        if (user.isDeleted() || !AccountStatus.ACTIVE.equals(user.getStatus())) {
            throw new IllegalArgumentException(MessageConstants.ACCOUNT_IS_NOT_ACTIVE);
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS);
        }
        if (encoder.matches(newPassword, user.getPasswordHash())) {
            throw new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD);
        }
        user.setPasswordHash(encoder.encode(newPassword));
        token.setUsed(true);
    }

}
