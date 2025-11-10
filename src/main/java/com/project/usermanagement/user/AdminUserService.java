package com.project.usermanagement.user;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;

import static com.project.usermanagement.user.UserSpecifications.*;

import com.project.usermanagement.entity.User;

import com.project.usermanagement.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    
    private final UserRepository repo;

    public Page<User> findUsers(String q, String role, String status, Instant createdFrom, Instant createdTo, Pageable pageable) {

        Specification<User> spec = and(
            emailOrNameLike(q),
            hasRole(role),
            hasStatus(status),
            createdFrom(createdFrom),
            createdTo(createdTo)
        );
        return repo.findAll(spec, pageable);
    }

}
