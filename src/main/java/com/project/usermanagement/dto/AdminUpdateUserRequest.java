package com.project.usermanagement.dto;

import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record AdminUpdateUserRequest(
        @Size(min = 2, max = 100) String fullName,
        @Email String email,
        @Size(max = 20) String phone,
        Role role,
        AccountStatus status
) implements IProfileUpdatePayload {}

