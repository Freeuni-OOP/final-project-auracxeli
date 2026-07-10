package com.auracxeli.user;

import com.auracxeli.achievement.AchievementService;
import com.auracxeli.user.dto.ConnectionsHistoryItem;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import com.auracxeli.user.dto.GuessBucket;
import com.auracxeli.user.dto.ProfileView;
import com.auracxeli.user.dto.WordleHistoryItem;
import com.auracxeli.user.dto.WordleStatsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceTest {

    private static final Long USER_ID = 1L;

    @Mock
    private UserService userService;

    @Mock
    private UserStatsService userStatsService;

    @Mock
    private ConnectionsStatsService connectionsStatsService;

    @Mock
    private AchievementService achievementService;

    @InjectMocks
    private ProfileService profileService;

    @Test
    void getProfileIncludesGameHistoriesTest() {
        User user = new User("player", "player@example.com", "hash");
        user.setId(USER_ID);
        user.setCreatedAt(LocalDateTime.of(2026, 6, 20, 12, 0));
        WordleStatsDto wordleStats = new WordleStatsDto(1, 1, 100, 1, 1);
        ConnectionsStatsDto connectionsStats = new ConnectionsStatsDto(1, 1, 0.0, 1, 1);
        List<GuessBucket> distribution = List.of(new GuessBucket(1, 1, 100));
        List<WordleHistoryItem> wordleHistory = List.of(
                new WordleHistoryItem(LocalDate.of(2026, 6, 23), "მოგებული", 3));
        List<ConnectionsHistoryItem> connectionsHistory = List.of(
                new ConnectionsHistoryItem(LocalDate.of(2026, 6, 22), "წაგებული", 4, 4));

        when(userService.getByUsername("player")).thenReturn(user);
        when(userStatsService.getWordleStats(USER_ID)).thenReturn(wordleStats);
        when(userStatsService.getWordleGuessDistribution(USER_ID)).thenReturn(distribution);
        when(userStatsService.getWordleHistory(USER_ID)).thenReturn(wordleHistory);
        when(connectionsStatsService.getConnectionsStats(USER_ID)).thenReturn(connectionsStats);
        when(connectionsStatsService.getConnectionsHistory(USER_ID)).thenReturn(connectionsHistory);

        ProfileView profile = profileService.getProfile("player");

        assertThat(profile.username()).isEqualTo("player");
        assertThat(profile.joinDate()).isEqualTo(LocalDate.of(2026, 6, 20));
        assertThat(profile.wordleHistory()).isSameAs(wordleHistory);
        assertThat(profile.connectionsHistory()).isSameAs(connectionsHistory);
    }
}
