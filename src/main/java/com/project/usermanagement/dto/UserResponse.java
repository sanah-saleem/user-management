package com.project.usermanagement.dto;

import com.project.usermanagement.entity.User;

public record UserResponse(
    Long id,
    String email,
    String fullName, 
    String role,
    String status
) {
    public static UserResponse from(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(), u.getStatus());
    }
}
