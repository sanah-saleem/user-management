package com.project.usermanagement.service;

import com.project.usermanagement.dto.AdminUpdateUserRequest;
import com.project.usermanagement.dto.UserFilterRequest;
import com.project.usermanagement.helper.HelperService;
import com.project.usermanagement.user.UserSpecifications;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;

import com.project.usermanagement.entity.User;

import com.project.usermanagement.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AdminUserService {
    
    private final UserRepository repo;
    private final HelperService helper;

    public Page<User> findUsers(UserFilterRequest filter, Pageable pageable) {
        var spec = UserSpecifications.and(
                UserSpecifications.emailOrNameLike(filter.q()),
                UserSpecifications.hasRole(filter.role()),
                UserSpecifications.hasStatus(filter.status()),
                UserSpecifications.createdFrom(filter.createdFrom()),
                UserSpecifications.createdTo(filter.createdTo()),
                UserSpecifications.excludeDeleted(filter.includeDeleted())
        );
        return repo.findAll(spec, pageable);
    }

    @Transactional
    public User deactivate(long id) {
        var u = helper.userMustExist(id);
        if (u.isDeleted()) throw new IllegalArgumentException(MessageConstants.USER_IS_DELETED);
        u.setStatus(AccountStatus.INACTIVE);
        return repo.save(u);
    }

    @Transactional
    public User reactivate(long id) {
        var u = helper.userMustExist(id);
        if (u.isDeleted()) throw new IllegalArgumentException(MessageConstants.USER_IS_DELETED);
        u.setStatus(AccountStatus.ACTIVE);
        return repo.save(u);
    }

    @Transactional
    public User softDelete(long id) {
        var u = helper.userMustExist(id);
        if (!u.isDeleted()) {
            u.setDeleted(true);
            u.setDeletedAt(Instant.now());
            u.setStatus(AccountStatus.INACTIVE);
            repo.save(u);
        }
        return u;
    }

    @Transactional
    public User restore(long id) {
        var u = helper.userMustExist(id);
        if (u.isDeleted()) {
            u.setDeleted(false);
            u.setDeletedAt(null);
            repo.save(u);
        }
        return u;
    }

    public User updateUser(long id, AdminUpdateUserRequest req) {
        var u = helper.userMustExist(id);
        helper.applyProfileUpdates(u, req);
        if (req.role() != null) {
            u.setRole(req.role());
        }
        if (req.status() != null) {
            if (u.isDeleted()) {
                throw new IllegalArgumentException(MessageConstants.USER_IS_DELETED);
            }
            u.setStatus(req.status());
        }
        return repo.save(u);
    }

}
