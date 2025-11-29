package com.project.usermanagement.dto.request;

public record EmailNotificationRequest (
        String to,
        String subject,
        String body
) {}
