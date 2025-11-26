package com.project.usermanagement.controller;

import com.project.usermanagement.util.MessageConstants;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.project.usermanagement.dto.LoginRequest;
import com.project.usermanagement.dto.RegisterRequest;
import com.project.usermanagement.dto.TokenResponse;
import com.project.usermanagement.dto.UserResponse;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;

import com.project.usermanagement.security.JwtService;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService service;

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
    
}
