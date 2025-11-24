package com.project.usermanagement.dto;

import com.project.usermanagement.entity.User;

import java.time.Instant;

public record UserResponse(
    Long id,
    String email,
    String fullName,
    String phone,
    String role,
    String status,
    Boolean deleted,
    Instant deletedAt
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getPhone(), u.getRole().name(), u.getStatus().name(), u.isDeleted(), u.getDeletedAt());
    }
}
