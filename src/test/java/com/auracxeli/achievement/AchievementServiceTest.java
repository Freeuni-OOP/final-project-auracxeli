package com.auracxeli.achievement;

import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsSessionRepository;
import com.auracxeli.user.ConnectionsStatsService;
import com.auracxeli.user.User;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import com.auracxeli.user.dto.GuessBucket;
import com.auracxeli.user.dto.WordleStatsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AchievementServiceTest {

    private static final Long USER_ID = 1L;
    private static final AchievementContext NONE =
            new AchievementContext(0, 0, false, 0, 0, false, 0);

    @Mock private UserAchievementRepository userAchievementRepository;
    @Mock private UserRepository userRepository;
    @Mock private UserStatsService userStatsService;
    @Mock private ConnectionsStatsService connectionsStatsService;
    @Mock private ConnectionsSessionRepository connectionsSessionRepository;
    @InjectMocks private AchievementService achievementService;

    // ---- rule coverage (pure, one per achievement) ----

    @Test
    void emptyContextEarnsNothing() {
        assertThat(Achievement.earnedFor(NONE)).isEmpty();
    }

    @Test
    void firstWinEarnedWithEitherGameWon() {
        assertThat(Achievement.earnedFor(new AchievementContext(1, 0, false, 0, 0, false, 1)))
                .contains(Achievement.FIRST_WIN);
        assertThat(Achievement.earnedFor(new AchievementContext(0, 0, false, 1, 0, false, 1)))
                .contains(Achievement.FIRST_WIN);
    }

    @Test
    void hatTrickEarnedAtThreeDayStreak() {
        assertThat(Achievement.earnedFor(new AchievementContext(1, 3, false, 0, 0, false, 3)))
                .contains(Achievement.HAT_TRICK);
        assertThat(Achievement.earnedFor(new AchievementContext(1, 2, false, 0, 0, false, 2)))
                .doesNotContain(Achievement.HAT_TRICK);
    }

    @Test
    void onFireEarnedAtSevenDayStreak() {
        assertThat(Achievement.earnedFor(new AchievementContext(1, 7, false, 0, 0, false, 7)))
                .contains(Achievement.ON_FIRE);
        assertThat(Achievement.earnedFor(new AchievementContext(1, 6, false, 0, 0, false, 6)))
                .doesNotContain(Achievement.ON_FIRE);
    }

    @Test
    void flawlessEarnedWithZeroMistakeConnectionsWin() {
        assertThat(Achievement.earnedFor(new AchievementContext(0, 0, false, 1, 0, true, 1)))
                .contains(Achievement.FLAWLESS);
        assertThat(Achievement.earnedFor(NONE)).doesNotContain(Achievement.FLAWLESS);
    }

    @Test
    void sharpshooterEarnedWinningWordleInTwoOrFewer() {
        assertThat(Achievement.earnedFor(new AchievementContext(1, 0, true, 0, 0, false, 1)))
                .contains(Achievement.SHARPSHOOTER);
        assertThat(Achievement.earnedFor(NONE)).doesNotContain(Achievement.SHARPSHOOTER);
    }

    @Test
    void regularEarnedAtTwentyFiveGames() {
        assertThat(Achievement.earnedFor(new AchievementContext(0, 0, false, 0, 0, false, 25)))
                .contains(Achievement.REGULAR);
        assertThat(Achievement.earnedFor(new AchievementContext(0, 0, false, 0, 0, false, 24)))
                .doesNotContain(Achievement.REGULAR);
    }

    // ---- service persistence ----

    @Test
    void evaluateAndGrantSavesNewlyEarnedAchievement() {
        stubContextForSingleWordleWin();
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of());
        when(userRepository.getReferenceById(USER_ID)).thenReturn(new User("u", "u@example.com", "hash"));

        achievementService.evaluateAndGrant(USER_ID);

        ArgumentCaptor<UserAchievement> captor = ArgumentCaptor.forClass(UserAchievement.class);
        verify(userAchievementRepository).save(captor.capture());
        assertThat(captor.getValue().getAchievement()).isEqualTo(Achievement.FIRST_WIN);
    }

    @Test
    void evaluateAndGrantDoesNotRegrantAlreadyEarned() {
        stubContextForSingleWordleWin();
        UserAchievement existing = new UserAchievement(new User("u", "u@example.com", "hash"), Achievement.FIRST_WIN);
        when(userAchievementRepository.findByUserId(USER_ID)).thenReturn(List.of(existing));

        achievementService.evaluateAndGrant(USER_ID);

        verify(userAchievementRepository, never()).save(any());
    }

    /** A single Wordle win: qualifies for FIRST_WIN only. */
    private void stubContextForSingleWordleWin() {
        when(userStatsService.getWordleStats(USER_ID)).thenReturn(new WordleStatsDto(1, 1, 100, 1, 1));
        when(connectionsStatsService.getConnectionsStats(USER_ID)).thenReturn(new ConnectionsStatsDto(0, 0, 0, 0, 0,100));
        when(userStatsService.getWordleGuessDistribution(USER_ID))
                .thenReturn(List.of(new GuessBucket(3, 1, 100)));
        when(connectionsSessionRepository.existsByUserIdAndOutcomeAndMistakesCount(USER_ID, ConnectionsOutcome.WON, 0))
                .thenReturn(false);
    }
}
