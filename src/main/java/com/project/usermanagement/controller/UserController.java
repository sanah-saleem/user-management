package com.project.usermanagement.controller;

import com.project.usermanagement.dto.request.ChangePasswordRequest;
import com.project.usermanagement.dto.request.UpdateProfileRequest;
import com.project.usermanagement.dto.response.UpdateProfileResponse;
import com.project.usermanagement.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.project.usermanagement.dto.response.UserResponse;
import com.project.usermanagement.security.UserPrincipal;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService service;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(principal.getUser());
    }

    @PutMapping("/me")
    public UpdateProfileResponse updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody UpdateProfileRequest request) {
        return service.updateProfile(principal, request);
    }

    @PostMapping("/change-password")
    public Object changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(principal, request);
        return Map.of("Message", "Password updated successfully");
    }

}
