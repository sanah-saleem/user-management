package com.project.usermanagement.entity;

import java.time.Instant;

import com.project.usermanagement.util.AccountStatus;
import com.project.usermanagement.util.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users", uniqueConstraints= {
    @UniqueConstraint(name = "uk_users_email", columnNames="email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Email
    @NotBlank
    @Column(nullable=false, length=255)
    private String email;

    @Column(length = 20)
    private String phone;

    @NotBlank
    @Column(nullable=false, length=100)
    private String fullName;

    @Column(nullable=false, length=60)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private AccountStatus status;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    @PrePersist void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (role == null) role = Role.USER;
        if (status == null) status = AccountStatus.ACTIVE;
    }

    @PreUpdate void preUpdate() {
        updatedAt = Instant.now();
    }

}
