package com.project.usermanagement.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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

    @NotBlank
    @Column(nullable=false, length=100)
    private String fullName;

    @Column(nullable=false, length=60)
    private String passwordHash;

    @Column(nullable=false, length=50)
    private String role;

    @Column(nullable=false, length=20)
    private String status;

    @Column(nullable=false, updatable=false)
    private Instant createdAt;

    @Column(nullable=false)
    private Instant updatedAt;

    @PrePersist void prePersist() {
        createdAt = Instant.now();
        updatedAt = createdAt;
        if (role == null) role = "USER";
        if (status == null) status = "ACTIVE";
    }

    @PreUpdate void preUpdate() {
        updatedAt = Instant.now();
    }

}
