package com.example.hoccode.security;

import com.example.hoccode.advice.exception.BadRequestException;
import com.example.hoccode.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Optional;
import org.springframework.security.core.userdetails.User;
@Component("userDetailsService")
public class UserDetailsCustom implements UserDetailsService {

    private final UserRepository userRepository;

    public UserDetailsCustom(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws BadRequestException {
        Optional<com.example.hoccode.entity.User> optionalUser = this.userRepository.findByEmail(username);
        if (optionalUser.isEmpty()) {
            throw new BadRequestException("User not existed");
        }
        return new User(
                optionalUser.get().getEmail(),
                optionalUser.get().getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(optionalUser.get().getRole())));
    }
}