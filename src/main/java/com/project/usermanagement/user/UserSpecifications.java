package com.project.usermanagement.user;

import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;
import org.springframework.data.jpa.domain.Specification;

import com.project.usermanagement.entity.User;

import java.time.Instant;

public class UserSpecifications {
    
    public static Specification<User> emailOrNameLike(String q) {
        if( q == null || q.isBlank()) return null;
        String like = "%" + q.toLowerCase().trim() + "%";
        return (root, cq, cb) -> cb.or(
            cb.like(cb.lower(root.get("email")), like),
            cb.like(cb.lower(root.get("fullName")), like)
        );
    }

    public static Specification<User> hasRole(Role role) {
        if (role == null) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("role")), role);
    }

    public static Specification<User> hasStatus(AccountStatus status) {
        if (status == null) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("status")), status);
    }

    public static Specification<User> createdFrom(Instant from) {
        if (from == null) return null;
        return (root, cq, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from);
    }

    public static Specification<User> createdTo(Instant to) {
        if (to == null) return null;
        return (root, cq, cb) -> cb.lessThan(root.get("createdAt"), to);
    }

    public static Specification<User> excludeDeleted(Boolean includeDeleted) {
        if (Boolean.TRUE.equals(includeDeleted)) return null;
        return (root, cq, cb) -> cb.isFalse(root.get("deleted"));
    }

    @SafeVarargs
    public static Specification<User> and(Specification<User>... specs) {
        Specification<User> result = null;
        for (var s : specs) {
            if (s == null) continue;
            result = (result == null) ?s : result.and(s);
        }
        return result;
    }

}
