package com.project.usermanagement.service;

import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import com.project.usermanagement.util.Role;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.project.usermanagement.dto.RegisterRequest;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.security.JwtService;
import com.project.usermanagement.security.UserDetailsServiceImpl;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final UserDetailsServiceImpl uds;
    private final JwtService jwtService;

    public User register(RegisterRequest request) {
        if (repo.existsByEmailAndDeletedFalse(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(request.email().trim())
                .fullName(request.fullName().trim())
                .passwordHash(encoder.encode(request.password()))
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();
        return repo.save(user);
    }

    public UserDetails loadUserDetails(String email) {
        return uds.loadUserByUsername(email);
    }

    public boolean isUserDeleted(String email) {
        return repo.findByEmailAndDeletedFalse(email.trim()).isEmpty();
    }

    public boolean isUserActive(String email) {
        User user = repo.findByEmailAndDeletedFalse(email.trim()).orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        return user.getStatus() == AccountStatus.ACTIVE;
    }



}
