package com.project.usermanagement.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.usermanagement.dto.RegisterRequest;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public User register(RegisterRequest request) {
        if (repo.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(request.email().trim())
                .fullName(request.fullName().trim())
                .passwordHash(encoder.encode(request.password()))
                .role("USER")
                .status("ACTIVE")
                .build();
        return repo.save(user);
    }

}
