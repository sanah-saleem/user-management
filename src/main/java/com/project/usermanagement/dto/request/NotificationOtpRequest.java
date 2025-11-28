package com.project.usermanagement.dto.request;

public record NotificationOtpRequest (
        String userId,
        String purpose,
        String email
) {}
