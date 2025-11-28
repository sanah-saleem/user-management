package com.project.usermanagement.service;

import com.project.usermanagement.dto.NotificationOtpVerificationStatus;
import com.project.usermanagement.dto.NotificationServicePurpose;
import com.project.usermanagement.dto.request.NotificationOtpRequest;
import com.project.usermanagement.dto.request.NotificationOtpVerificationRequest;
import com.project.usermanagement.dto.request.ResetPasswordRequest;
import com.project.usermanagement.dto.response.NotificationOtpVerificationResponse;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final NotificationClientService notificationClientService;

    @Transactional
    public void createResetToken(String email, String purpose) {
        var userOpt = userRepo.findByEmailAndDeletedFalse(email.trim());
        if (userOpt.isEmpty()) {
            return;
        }
        User user = userOpt.get();
        if (!AccountStatus.ACTIVE.equals(user.getStatus())) {
            return;
        }
        NotificationOtpRequest request = new NotificationOtpRequest(user.getId().toString(), purpose, email);
        notificationClientService.requestOtp(request);

    }
    @Transactional
    public NotificationOtpVerificationStatus resetPassword(ResetPasswordRequest request) {
        var userOpt = userRepo.findByEmailAndDeletedFalse(request.email().trim());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException(MessageConstants.USER_NOT_FOUND);
        }
        User user = userOpt.get();
        if (user.isDeleted() || !AccountStatus.ACTIVE.equals(user.getStatus())) {
            throw new IllegalArgumentException(MessageConstants.ACCOUNT_IS_NOT_ACTIVE);
        }
        if (request.newPassword().length() < 8) {
            throw new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS);
        }
        NotificationOtpVerificationRequest request1 = new NotificationOtpVerificationRequest(
                user.getId().toString(), NotificationServicePurpose.RESET_PASSWORD.name(), request.token());
        NotificationOtpVerificationResponse response = notificationClientService.verifyOtp(request1);
        if (response.status() == NotificationOtpVerificationStatus.VALID) {
            user.setPasswordHash(encoder.encode(request.newPassword()));
        }
        return response.status();
    }

}
