package com.project.usermanagement.service;

import com.project.usermanagement.dto.ChangePasswordRequest;
import com.project.usermanagement.dto.UpdateProfileRequest;
import com.project.usermanagement.dto.UpdateProfileResponse;
import com.project.usermanagement.dto.UserResponse;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.security.JwtService;
import com.project.usermanagement.security.UserPrincipal;
import com.project.usermanagement.util.MessageConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class SelfService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    @Transactional
    public UpdateProfileResponse updateProfile(UserPrincipal principal, UpdateProfileRequest request) {
        var user = repo.findById(principal.getUser().getId())
                .orElseThrow( () -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        boolean emailChanged = false;
        if(request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            if(!newEmail.equals(user.getEmail())) {
                if(repo.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException(MessageConstants.EMAIL_ALREADY_REGISTERED);
                }
                user.setEmail(newEmail);
                emailChanged = true;
            }
        }

        user.setFullName(request.fullName().trim());
        if(request.phone() != null)
            user.setPhone(request.phone().trim());

        var saved = repo.save(user);
        var response = UserResponse.from(user);

        // If email changed, issue a fresh JWT to keep the client authenticated
        if(emailChanged) {
            var token = jwt.generate(saved.getEmail());
            return UpdateProfileResponse.withToken(response, token);
        }
        return UpdateProfileResponse.of(response);
    }

    public void changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        var user = repo.findById(principal.getUser().getId())
                .orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        if (!encoder.matches(request.currentpassword(), user.getPasswordHash()))
            throw new IllegalArgumentException(MessageConstants.CURRENT_PASSWORD_IS_INCORRECT);
        if (encoder.matches(request.newPassword(), user.getPasswordHash()))
            throw  new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_DIFFERENT_FROM_CURRENT_PASSWORD);
        // Basic strength checks (optional; enforce with @Pattern if you prefer)
        if (request.newPassword().length() < 8)
            throw new IllegalArgumentException(MessageConstants.NEW_PASSWORD_MUST_BE_ATLEAST_8_CHARACTERS);
        user.setPasswordHash(encoder.encode(request.newPassword()));
        repo.save(user);
    }

}
