package com.worldcup.service;

import com.worldcup.domain.Role;
import com.worldcup.domain.User;
import com.worldcup.dto.RegistrationDto;
import com.worldcup.exception.DuplicateEmailException;
import com.worldcup.exception.DuplicateUsernameException;
import com.worldcup.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .roles(user.getRole().name())
            .build();
    }

    public void register(RegistrationDto dto) {
        String normalizedEmail = dto.getEmail() == null ? null : dto.getEmail().trim().toLowerCase();
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new DuplicateUsernameException("Username already taken.");
        }
        if (normalizedEmail != null && userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new DuplicateEmailException("Email already in use.");
        }
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        log.info("Registered new user: {}", dto.getUsername());
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }
}
