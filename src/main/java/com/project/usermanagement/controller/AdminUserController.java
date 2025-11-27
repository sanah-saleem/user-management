package com.project.usermanagement.controller;

import com.project.usermanagement.dto.request.AdminUpdateUserRequest;
import com.project.usermanagement.dto.request.UserFilterRequest;
import com.project.usermanagement.dto.response.PageResponse;
import com.project.usermanagement.service.AdminUserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import com.project.usermanagement.dto.response.UserResponse;

import lombok.RequiredArgsConstructor;

@Tag(name = "Admin Users", description = "Admin operations on users")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/users")
public class AdminUserController {
    
    private final AdminUserService service;

    @GetMapping()
    public PageResponse<UserResponse> List(UserFilterRequest filter,
                                   @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        var page = service.findUsers(filter, pageable);
        return PageResponse.from(page, UserResponse::from);
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
    public UserResponse restore(@PathVariable long id) { return UserResponse.from(service.restore(id)); }

    @PutMapping("/{id}")
    public UserResponse updateUser(@PathVariable long id, @Valid @RequestBody AdminUpdateUserRequest req) {
        return UserResponse.from(service.updateUser(id, req));
    }
    
}
