package com.project.usermanagement.helper;

import com.project.usermanagement.dto.IProfileUpdatePayload;
import com.project.usermanagement.entity.User;
import com.project.usermanagement.repository.UserRepository;
import com.project.usermanagement.security.UserDetailsServiceImpl;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class HelperService {

    private final UserRepository repo;
    private final UserDetailsServiceImpl uds;
    private static final SecureRandom random = new SecureRandom();

    public User userMustExist(long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
    }

    public boolean applyProfileUpdates(User user, IProfileUpdatePayload request) {
        boolean emailChanged = false;

        if (request.email() != null && !request.email().isBlank()) {
            String newEmail = request.email().trim();
            if (!newEmail.equals(user.getEmail())) {
                if (repo.existsByEmailAndDeletedFalse(newEmail)) {
                    throw new IllegalArgumentException(MessageConstants.EMAIL_ALREADY_REGISTERED);
                }
                user.setEmail(newEmail);
                emailChanged = true;
            }
        }

        if (request.fullName() != null && !request.fullName().isBlank()) {
            user.setFullName(request.fullName().trim());
        }

        if (request.phone() != null && !request.phone().isBlank()) {
            user.setPhone(request.phone().trim());
        }

        return emailChanged;
    }

    public UserDetails loadUserDetails(String email) {
        return uds.loadUserByUsername(email);
    }

    public boolean isUserDeleted(String email) {
        return repo.findByEmailAndDeletedFalse(email.trim()).isEmpty();
    }

    public boolean isUserActive(String email) {
        User user = repo.findByEmailAndDeletedFalse(email.trim()).orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
        return user.getStatus() == AccountStatus.ACTIVE;
    }

    public String generateToken() {
        byte[] bytes = new byte[32]; // 256 bits
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

}
