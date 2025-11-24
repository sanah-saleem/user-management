package com.project.usermanagement.user;

import java.time.Instant;

import com.project.usermanagement.dto.UserFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.project.usermanagement.dto.UserResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {
    
    private final AdminUserService service;

    @GetMapping()
    public Page<UserResponse> List(UserFilterRequest filter,
                                   @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return service.findUsers(filter, pageable)
            .map(UserResponse::from);
    }

    @PostMapping("/{id}/deactivate")
    public UserResponse deactivate(@PathVariable long id) {
        return UserResponse.from(service.deactivate(id));
    }

    @PostMapping("/{id}/reactivate")
    public UserResponse reactivate(@PathVariable long id) {
        return UserResponse.from(service.reactivate(id));
    }

    @DeleteMapping("/{id}")
    public UserResponse softDelete(@PathVariable long id) {
        return UserResponse.from(service.softDelete(id));
    }

    @PostMapping("/{id}/restore")
    public UserResponse restore(@PathVariable long id) {
        return UserResponse.from(service.restore(id));
    }
    
}
