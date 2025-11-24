package com.project.usermanagement.user;

import com.project.usermanagement.dto.UserFilterRequest;
import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.MessageConstants;
import com.project.usermanagement.util.Role;
import jakarta.transaction.Transactional;
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
                UserSpecifications.createdTo(filter.createdTo()),
                UserSpecifications.excludeDeleted(filter.includeDeleted())
        );
        return repo.findAll(spec, pageable);
    }

    private User mustExist(long id) {
        return repo.findById(id).orElseThrow(() -> new IllegalArgumentException(MessageConstants.USER_NOT_FOUND));
    }

    @Transactional
    public User deactivate(long id) {
        var u = mustExist(id);
        if (u.isDeleted()) throw new IllegalArgumentException(MessageConstants.USER_IS_DELETED);
        u.setStatus(AccountStatus.INACTIVE);
        return repo.save(u);
    }

    @Transactional
    public User reactivate(long id) {
        var u = mustExist(id);
        if (u.isDeleted()) throw new IllegalArgumentException(MessageConstants.USER_IS_DELETED);
        u.setStatus(AccountStatus.ACTIVE);
        return repo.save(u);
    }

    @Transactional
    public User softDelete(long id) {
        var u = mustExist(id);
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
        var u = mustExist(id);
        if (u.isDeleted()) {
            u.setDeleted(false);
            u.setDeletedAt(null);
            repo.save(u);
        }
        return u;
    }

}
