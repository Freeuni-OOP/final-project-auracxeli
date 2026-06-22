package com.auracxeli.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsernameReturnsWrappedUserWhenFound() {
        User user = new User("axaliuseri", "new@mail.com", "HASHED");
        when(userRepository.findByUsername("axaliuseri")).thenReturn(Optional.of(user));

        UserDetails details = userDetailsService.loadUserByUsername("axaliuseri");

        assertInstanceOf(UserDetailsImpl.class, details);
        assertEquals("axaliuseri", details.getUsername());
        assertEquals("HASHED", details.getPassword());
    }

    @Test
    void loadUserByUsernameThrowsWhenNotFound() {
        when(userRepository.findByUsername("userivar")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("userivar"));
    }
}
