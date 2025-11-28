package com.project.usermanagement.dto.request;

public record NotificationOtpVerificationRequest(
   String userId,
   String purpose,
   String otp
) {}
