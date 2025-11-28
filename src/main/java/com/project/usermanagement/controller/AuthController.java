package com.project.usermanagement.controller;

import com.project.usermanagement.dto.NotificationOtpVerificationStatus;
import com.project.usermanagement.dto.request.ForgotPasswordRequest;
import com.project.usermanagement.dto.request.ResetPasswordRequest;
import com.project.usermanagement.dto.response.ForgotPasswordResponse;
import com.project.usermanagement.service.PasswordResetService;
import com.project.usermanagement.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.project.usermanagement.dto.request.LoginRequest;
import com.project.usermanagement.dto.request.RegisterRequest;
import com.project.usermanagement.dto.response.TokenResponse;
import com.project.usermanagement.dto.response.UserResponse;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;

import java.util.Map;

import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Register, login and password reset")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        User saved = service.register(request);
        return UserResponse.from(saved);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        var token = service.login(request);
        return TokenResponse.of(token);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.createResetToken(request.email(), "RESET_PASSWORD");
        return ResponseEntity.ok("If email is valid, otp sent successfully");
    }

    @PostMapping("/reset-password")
    public Map<String, String> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        NotificationOtpVerificationStatus status = passwordResetService.resetPassword(request);
        if (status == NotificationOtpVerificationStatus.VALID)
            return Map.of(MessageConstants.MESSAGE, MessageConstants.PASSWORD_RESET_SUCCESSFULLY);
        if (status == NotificationOtpVerificationStatus.INVALID)
            return Map.of(MessageConstants.MESSAGE, "Otp invalid");
        if (status == NotificationOtpVerificationStatus.EXPIRED_OR_NOT_FOUND)
            return Map.of(MessageConstants.MESSAGE, "Expired or wrong otp");
        if (status == NotificationOtpVerificationStatus.TOO_MANY_ATTEMPTS)
            return Map.of(MessageConstants.MESSAGE, "Too many attempts");
        return Map.of(MessageConstants.MESSAGE, "unable to verify otp");

    }
    
}
