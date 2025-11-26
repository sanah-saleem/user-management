package com.project.usermanagement.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest (
    @NotBlank
    String currentpassword,

    @NotBlank
    @Size(min = 8, max = 100)
    String newPassword
) {}
