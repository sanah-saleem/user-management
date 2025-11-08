package com.project.usermanagement.dto;

public record TokenResponse(String accessToken, String tokenType) {
    public static TokenResponse of(String jwt) { return new TokenResponse(jwt, "Bearer ");}
}
