package com.auracxeli.user;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserDetailsImplTest {

    @Test
    void authoritiesReflectUserRole() {
        User user = new User("axaliuseri", "new@mail.com", "HASHED");
        user.setRole(Role.USER);
        UserDetailsImpl details = new UserDetailsImpl(user);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_USER", authorities.iterator().next().getAuthority());
    }

    @Test
    void authoritiesReflectAdminRole() {
        User user = new User("admini", "admin@mail.com", "HASHED");
        user.setRole(Role.ADMIN);
        UserDetailsImpl details = new UserDetailsImpl(user);

        Collection<? extends GrantedAuthority> authorities = details.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void delegatesUsernameAndPasswordToUser() {
        User user = new User("axaliuseri", "new@mail.com", "HASHED");
        UserDetailsImpl details = new UserDetailsImpl(user);

        assertEquals("axaliuseri", details.getUsername());
        assertEquals("HASHED", details.getPassword());
    }

    @Test
    void enabledWhenUserIsActive() {
        User user = new User("axaliuseri", "new@mail.com", "HASHED");
        user.setActive(true);
        UserDetailsImpl details = new UserDetailsImpl(user);

        assertTrue(details.isEnabled());
    }

    @Test
    void disabledWhenUserIsNotActive() {
        User user = new User("axaliuseri", "new@mail.com", "HASHED");
        user.setActive(false);
        UserDetailsImpl details = new UserDetailsImpl(user);

        assertFalse(details.isEnabled());
    }
}
