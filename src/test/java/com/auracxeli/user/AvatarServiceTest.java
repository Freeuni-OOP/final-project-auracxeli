package com.auracxeli.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AvatarServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AvatarService avatarService;

    @Test
    void availableAvatars_returnsElevenPresetFilenames() {
        List<String> avatars = avatarService.availableAvatars();

        assertEquals(11, avatars.size());
        assertTrue(avatars.contains("avatar-1.png"));
        assertTrue(avatars.contains("avatar-11.png"));
    }


    @Test
    void chooseAvatar_validAvatar_savesItOnTheUser() {
        User user = new User("tester", "tester@example.com", "hash");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        avatarService.chooseAvatar(1L, "avatar-3.png");

        assertEquals("avatar-3.png", user.getAvatar());
    }


    @Test
    void chooseAvatar_avatarNotInAvailableSet_throwsAndDoesNotTouchUser() {
        assertThrows(IllegalArgumentException.class,
                () -> avatarService.chooseAvatar(1L, "racxa-rendom.png"));

        verify(userRepository, never()).findById(any());
    }


    @Test
    void chooseAvatar_userNotFound_throwsUserNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> avatarService.chooseAvatar(99L, "avatar-1.png"));
    }
}