package com.worldcup.service;

import com.worldcup.domain.Role;
import com.worldcup.domain.User;
import com.worldcup.dto.RegistrationDto;
import com.worldcup.exception.DuplicateEmailException;
import com.worldcup.exception.DuplicateUsernameException;
import com.worldcup.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService userService;

    private User existingUser;

    @BeforeEach
    void setUp() {
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("alice");
        existingUser.setEmail("alice@example.com");
        existingUser.setPassword("encoded");
        existingUser.setRole(Role.USER);
    }

    @Test
    void findByUsername_whenUserExists_shouldReturnUser() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));
        User result = userService.findByUsername("alice");
        assertThat(result.getUsername()).isEqualTo("alice");
    }

    @Test
    void findByUsername_whenUserNotFound_shouldThrow() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.findByUsername("ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void loadUserByUsername_whenUserExists_shouldReturnUserDetails() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));
        UserDetails details = userService.loadUserByUsername("alice");
        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getAuthorities()).anyMatch(a -> a.getAuthority().equals("ROLE_USER"));
    }

    @Test
    void loadUserByUsername_whenUserNotFound_shouldThrow() {
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.loadUserByUsername("ghost"))
            .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void register_whenValid_shouldSaveUser() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("bob@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");

        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("bob");
        dto.setEmail("bob@example.com");
        dto.setPassword("secret123");

        userService.register(dto);
        verify(userRepository).save(argThat(u -> u.getUsername().equals("bob") && u.getRole() == Role.USER));
    }

    @Test
    void register_whenDuplicateUsername_shouldThrow() {
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(existingUser));

        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("alice");
        dto.setEmail("other@example.com");
        dto.setPassword("password123");

        assertThatThrownBy(() -> userService.register(dto))
            .isInstanceOf(DuplicateUsernameException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenDuplicateEmail_shouldThrow() {
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(existingUser));

        RegistrationDto dto = new RegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("alice@example.com");
        dto.setPassword("password123");

        assertThatThrownBy(() -> userService.register(dto))
            .isInstanceOf(DuplicateEmailException.class);
        verify(userRepository, never()).save(any());
    }
}
