package com.project.usermanagement.service;

import com.project.usermanagement.dto.request.*;
import com.project.usermanagement.dto.response.UpdateProfileResponse;
import com.project.usermanagement.dto.response.UserResponse;
import com.project.usermanagement.helper.HelperService;
import com.project.usermanagement.security.UserPrincipal;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import com.project.usermanagement.util.Role;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.security.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final HelperService helper;
    private final NotificationClientService notificationClientService;

    public User register(RegisterRequest request) {
        if (repo.existsByEmailAndDeletedFalse(request.email())) {
            throw new IllegalArgumentException(MessageConstants.EMAIL_ALREADY_REGISTERED);
        }
        User user = User.builder()
                .email(request.email().trim())
                .fullName(request.fullName().trim())
                .passwordHash(encoder.encode(request.password()))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();
        repo.save(user);
        sendWelcomeEmail(user);
        return user;
    }

    public String login(LoginRequest request) {
        var userDetails = helper.loadUserDetails(request.email());
        if (helper.isUserDeleted(request.email())) {
            throw new IllegalArgumentException(MessageConstants.INVALID_CREDENTIALS);
        }
        if (!encoder.matches(request.password(), userDetails.getPassword())) {
            throw new IllegalArgumentException(MessageConstants.INVALID_CREDENTIALS);
        }
        if (!helper.isUserActive(request.email())) {
            throw new IllegalArgumentException(MessageConstants.ACCOUNT_IS_NOT_ACTIVE);
        }
        return jwtService.generate(userDetails.getUsername());
    }

    @Transactional
    public UpdateProfileResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        var user = helper.userMustExist(principal.getUser().getId());
        boolean emailChanged = helper.applyProfileUpdates(principal.getUser(), request);

        var saved = repo.save(user);
        var response = UserResponse.from(user);

        // If email changed, issue a fresh JWT to keep the client authenticated
        if(emailChanged) {
            var token = jwtService.generate(saved.getEmail());
            return UpdateProfileResponse.withToken(response, token);
        }
        return UpdateProfileResponse.of(response);
    }

    @Transactional
    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        var user = repo.findById(principal.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        if (!encoder.matches(request.currentpassword(), user.getPasswordHash()))
            throw new IllegalArgumentException(MessageConstants.CURRENT_PASSWORD_IS_INCORRECT);
        if (encoder.matches(request.newPassword(), user.getPasswordHash()))
            throw  new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD);
        // Basic strength checks (optional; enforce with @Pattern if you prefer)
        if (request.newPassword().length() < 8)
            throw new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS);
        user.setPasswordHash(encoder.encode(request.newPassword()));
        repo.save(user);
        helper.sendPasswordChangeAlert(user);
    }

    private void sendWelcomeEmail(User user) {
        String subject = "Welcome to Our Platform!";
        String body = String.format(
                "Hi %s,\n\n" +
                        "Welcome to our platform. Your account has been created successfully with email: %s.\n\n" +
                        "If you did not sign up, please contact support immediately.\n\n" +
                        "Best regards,\nThe Team",
                user.getFullName() != null ? user.getFullName() : "there",
                user.getEmail()
        );
        EmailNotificationRequest emailRequest = new EmailNotificationRequest( user.getEmail(), subject, body );
        notificationClientService.sendEmailNotification(emailRequest);
    }

}
