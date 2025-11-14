package com.project.usermanagement.dto;

public record UpdateProfileResponse (
  UserResponse user,
  String newAccessToken,
  String tokenType
) {
    public static UpdateProfileResponse of(UserResponse user) {
        return new UpdateProfileResponse(user, null, null);
    }
    public static UpdateProfileResponse withToken(UserResponse user, String jwt) {
        return new UpdateProfileResponse(user, jwt, "Bearer");
    }
}
