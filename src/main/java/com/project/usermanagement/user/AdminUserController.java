package com.project.usermanagement.user;

import java.time.Instant;

import com.project.usermanagement.dto.UserFilterRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

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
    
}
