package com.auracxeli.admin;

import com.auracxeli.admin.dto.UserRow;
import com.auracxeli.user.Role;
import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void listUsersReturnsUsersOrderedByUsernameTest() {
        when(userRepository.findAllByOrderByUsernameAsc())
                .thenReturn(List.of(user(2L, "ana", Role.USER, true)));

        List<UserRow> rows = adminUserService.listUsers();

        assertEquals(1, rows.size());
        assertEquals("ana", rows.getFirst().username());
        assertEquals("ana@example.com", rows.getFirst().email());
        verify(userRepository).findAllByOrderByUsernameAsc();
    }

    @Test
    void listUsersWithBlankQueryReturnsUsersOrderedByUsernameTest() {
        when(userRepository.findAllByOrderByUsernameAsc())
                .thenReturn(List.of(user(2L, "ana", Role.USER, true)));

        List<UserRow> rows = adminUserService.listUsers("   ");

        assertEquals(1, rows.size());
        assertEquals("ana", rows.getFirst().username());
        verify(userRepository).findAllByOrderByUsernameAsc();
    }

    @Test
    void listUsersWithQuerySearchesUsernameAndEmailTest() {
        when(userRepository.findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
                "gio", "gio")).thenReturn(List.of(user(2L, "gio", Role.USER, true)));

        List<UserRow> rows = adminUserService.listUsers(" gio ");

        assertEquals(1, rows.size());
        assertEquals("gio", rows.getFirst().username());
        verify(userRepository).findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrderByUsernameAsc(
                "gio", "gio");
    }

    @Test
    void toggleActiveBlocksActiveUserTest() {
        User target = user(2L, "gio", Role.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        UserRow result = adminUserService.toggleActive(2L, 1L);

        assertFalse(result.active());
        assertFalse(target.isActive());
        verify(userRepository).save(target);
    }

    @Test
    void toggleActiveUnblocksBlockedUserTest() {
        User target = user(2L, "gio", Role.USER, false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));
        when(userRepository.save(target)).thenReturn(target);

        adminUserService.toggleActive(2L, 1L);

        assertTrue(target.isActive());
        verify(userRepository).save(target);
    }

    @Test
    void toggleActiveRejectsCurrentAdminTest() {
        User target = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));

        assertThrows(AdminUserActionDeniedException.class,
                () -> adminUserService.toggleActive(1L, 1L));
        verify(userRepository, never()).save(target);
    }

    @Test
    void toggleActiveRejectsOtherAdminTest() {
        User target = user(2L, "admin2", Role.ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(AdminUserActionDeniedException.class,
                () -> adminUserService.toggleActive(2L, 1L));
        verify(userRepository, never()).save(target);
    }

    @Test
    void deleteUserDeletesRegularUserTest() {
        User target = user(2L, "gio", Role.USER, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        adminUserService.deleteUser(2L, 1L);

        verify(userRepository).delete(target);
    }

    @Test
    void deleteUserRejectsCurrentAdminTest() {
        User target = user(1L, "admin", Role.ADMIN, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(target));

        assertThrows(AdminUserActionDeniedException.class,
                () -> adminUserService.deleteUser(1L, 1L));
        verify(userRepository, never()).delete(target);
    }

    @Test
    void deleteUserRejectsOtherAdminTest() {
        User target = user(2L, "admin2", Role.ADMIN, true);
        when(userRepository.findById(2L)).thenReturn(Optional.of(target));

        assertThrows(AdminUserActionDeniedException.class,
                () -> adminUserService.deleteUser(2L, 1L));
        verify(userRepository, never()).delete(target);
    }

    @Test
    void missingUserThrowsTest() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(AdminUserActionDeniedException.class,
                () -> adminUserService.toggleActive(404L, 1L));
    }

    private User user(Long id, String username, Role role, boolean active) {
        User user = new User(username, username + "@example.com", "password");
        user.setId(id);
        user.setRole(role);
        user.setActive(active);
        return user;
    }
}
