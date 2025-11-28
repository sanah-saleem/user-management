package com.project.usermanagement.dto.response;

import com.project.usermanagement.dto.NotificationOtpVerificationStatus;

public record NotificationOtpVerificationResponse(
   NotificationOtpVerificationStatus status,
   String message
) {}
