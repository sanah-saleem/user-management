package com.project.usermanagement.dto.request;

import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;

import java.time.Instant;

public record UserFilterRequest(
   String q,
   Role role,
   AccountStatus status,
   Instant createdFrom,
   Instant createdTo,
   Boolean includeDeleted
) {}
