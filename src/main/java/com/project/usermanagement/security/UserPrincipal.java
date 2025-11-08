package com.project.usermanagement.security;

import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.project.usermanagement.entity.User;

public class UserPrincipal implements UserDetails {

    private final User user;
    public UserPrincipal(User user) { this.user = user;}
    public User getUser() { return user; }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole()));
    }

    @Override public String getPassword() { return user.getPasswordHash();}
    @Override public String getUsername() { return user.getEmail(); }
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !"LOCKED".equalsIgnoreCase(user.getStatus()); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "ACTIVE".equalsIgnoreCase(user.getStatus()); }

}