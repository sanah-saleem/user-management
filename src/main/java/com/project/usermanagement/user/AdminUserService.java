package com.project.usermanagement.user;

import com.project.usermanagement.dto.UserFilterRequest;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;
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

    public Page<User> findUsers(UserFilterRequest filter, Pageable pageable) {

        var spec = UserSpecifications.and(
                UserSpecifications.emailOrNameLike(filter.q()),
                UserSpecifications.hasRole(filter.role()),
                UserSpecifications.hasStatus(filter.status()),
                UserSpecifications.createdFrom(filter.createdFrom()),
                UserSpecifications.createdTo(filter.createdTo())
        );
        return repo.findAll(spec, pageable);
    }

}
