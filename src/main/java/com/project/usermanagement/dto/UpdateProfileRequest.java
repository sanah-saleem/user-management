package com.project.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest (
    @NotBlank
    @Size(min = 2, max = 100)
    String fullName,

    @Email
    String email,

    @Size(max = 20)
    String phone
) implements IProfileUpdatePayload {}
