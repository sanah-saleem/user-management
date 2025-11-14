package com.project.usermanagement.controller;

import com.project.usermanagement.dto.ChangePasswordRequest;
import com.project.usermanagement.dto.UpdateProfileRequest;
import com.project.usermanagement.dto.UpdateProfileResponse;
import com.project.usermanagement.service.SelfService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.project.usermanagement.dto.UserResponse;
import com.project.usermanagement.security.UserPrincipal;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final SelfService selfService;

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal UserPrincipal principal) {
        return UserResponse.from(principal.getUser());
    }

    @PutMapping("/me")
    public UpdateProfileResponse updateMe(@AuthenticationPrincipal UserPrincipal principal,
                                          @Valid @RequestBody UpdateProfileRequest request) {
        return selfService.updateProfile(principal, request);
    }

    @PostMapping("/change-password")
    public Object changePassword(@AuthenticationPrincipal UserPrincipal principal,
                                 @Valid @RequestBody ChangePasswordRequest request) {
        selfService.changePassword(principal, request);
        return Map.of("Message", "Password updated successfully");
    }

}
