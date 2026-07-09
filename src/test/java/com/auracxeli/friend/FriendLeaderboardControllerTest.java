package com.auracxeli.friend;

import com.auracxeli.config.SecurityConfig;
import com.auracxeli.friend.dto.LeaderboardEntry;
import com.auracxeli.user.Role;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.dto.WordleStatsDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(FriendLeaderboardController.class)
@Import(SecurityConfig.class)
class FriendLeaderboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FriendLeaderboardService friendLeaderboardService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Autowired
    private FriendLeaderboardController friendLeaderboardController;

    @Test
    void wordleLeaderboardLinksUsernamesToProfilesTest() throws Exception {
        User currentUser = user(1L, "me");
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(friendLeaderboardService.buildWordleLeaderboard(currentUser)).thenReturn(List.of(
                entry(1L, "me", true),
                entry(2L, "friend", false)
        ));

        mockMvc.perform(get("/friends/leaderboard/wordle").with(authentication(auth(currentUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("wordle-leaderboard"))
                .andExpect(content().string(containsString("/profile/me")))
                .andExpect(content().string(containsString("/profile/friend")))
                .andExpect(content().string(containsString("/friends/leaderboard/wordle")));
    }

    @Test
    void wordleLeaderboardWithoutFriendsShowsEmptyStateTest() throws Exception {
        User currentUser = user(1L, "me");
        when(userRepository.findById(1L)).thenReturn(Optional.of(currentUser));
        when(friendLeaderboardService.buildWordleLeaderboard(currentUser)).thenReturn(List.of(
                entry(1L, "me", true)
        ));

        mockMvc.perform(get("/friends/leaderboard/wordle").with(authentication(auth(currentUser))))
                .andExpect(status().isOk())
                .andExpect(view().name("wordle-leaderboard"))
                .andExpect(content().string(containsString("დაიმატე მეგობრები")));
    }

    @Test
    void wordleLeaderboardMissingAuthenticatedUserThrowsTest() {
        User currentUser = user(1L, "me");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        Model model = new ExtendedModelMap();

        assertThrows(IllegalStateException.class,
                () -> friendLeaderboardController.showWordleLeaderboard(auth(currentUser), model));
    }

    private LeaderboardEntry entry(Long userId, String username, boolean currentUser) {
        return new LeaderboardEntry(userId, username, new WordleStatsDto(1, 1, 100, 1, 1), currentUser);
    }

    private UsernamePasswordAuthenticationToken auth(User user) {
        UserDetailsImpl principal = new UserDetailsImpl(user);
        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    private User user(Long id, String username) {
        User user = new User(username, username + "@example.com", "hash");
        user.setId(id);
        user.setRole(Role.USER);
        return user;
    }
}
