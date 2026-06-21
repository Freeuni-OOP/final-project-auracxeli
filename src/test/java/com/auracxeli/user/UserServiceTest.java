package com.auracxeli.user;

import com.auracxeli.user.dto.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock  private UserRepository userRepository;
    @Mock  private PasswordEncoder passwordEncoder;
    @InjectMocks private UserService userService;
    @Test
    void validRegisterTest() {
        RegisterRequest request = new RegisterRequest("axaliuseri", "new@mail.com", "paroli123");
        when(userRepository.existsByUsername("axaliuseri")).thenReturn(false);
        when(userRepository.existsByEmail("new@mail.com")).thenReturn(false);
        when(passwordEncoder.encode("paroli123")).thenReturn("HASHED");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userService.register(request);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("axaliuseri", saved.getUsername());
        assertEquals("new@mail.com", saved.getEmail());
        assertTrue(saved.getPassword().equals("HASHED"));
        assertFalse(saved.getPassword().equals("paroli123"));
        assertEquals(Role.USER, saved.getRole());
    }

    @Test
    void duplicateUsernameRegisterTest() {
        RegisterRequest request = new RegisterRequest("taken", "new@mail.com", "paroli123");
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        DuplicateUserException ex = assertThrows(DuplicateUserException.class,
                () -> userService.register(request));
        assertEquals("username", ex.getField());
        verify(userRepository, never()).save(any());
    }

    @Test
    void duplicateEmailRegisterTEST() {
        RegisterRequest request = new RegisterRequest("axaliuseri", "taken@mail.com", "paroli123");
        when(userRepository.existsByUsername("axaliuseri")).thenReturn(false);
        when(userRepository.existsByEmail("taken@mail.com")).thenReturn(true);

        DuplicateUserException ex = assertThrows(DuplicateUserException.class,
                () -> userService.register(request));

        assertEquals("email", ex.getField());
        verify(userRepository, never()).save(any());
    }
}
