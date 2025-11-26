package com.project.usermanagement.dto.response;

public record ForgotPasswordResponse(String message, String resetToken) {
    public static ForgotPasswordResponse generic () {
        return new ForgotPasswordResponse("If the email exists, a reset token has been sent.", null);
    }
    public static ForgotPasswordResponse withToken(String token) {
        return new ForgotPasswordResponse(
                "Reset token generated (dev mode). In production, this would be emailed.",
                token);
    }
}
