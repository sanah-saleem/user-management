package com.project.usermanagement.security;

import com.project.usermanagement.util.MessageConstants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.project.usermanagement.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var user = repository.findByEmail(email.trim())
                    .orElseThrow(() -> new UsernameNotFoundException(MessageConstants.USER_NOT_FOUND));
        return new UserPrincipal(user);

    }

}
