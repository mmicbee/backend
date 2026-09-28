package com.zone01kisumu.backend.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AppUserDetailsService {

    public UserDetails loadUserByUsername(String username) {
        return User.withUsername(username)
                .password("password")
                .roles("USER")
                .build();
    }
}
