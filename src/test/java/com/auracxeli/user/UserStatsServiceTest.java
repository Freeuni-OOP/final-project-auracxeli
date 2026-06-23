package com.auracxeli.user;

import com.auracxeli.user.dto.UserStatsDto;
import com.auracxeli.wordle.WordleSession;
import com.auracxeli.wordle.WordleSessionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static com.auracxeli.wordle.WordleOutcome.IN_PROGRESS;
import static com.auracxeli.wordle.WordleOutcome.LOST;
import static com.auracxeli.wordle.WordleOutcome.WON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 23);

    @Mock private WordleSessionRepository sessionRepository;
    @InjectMocks private UserStatsService userStatsService;


    @Test
    void noStatsWhenNoGamesPlayedTest() {
        givenHistory(List.of());

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isZero();
        assertThat(stats.wins()).isZero();
        assertThat(stats.winPercent()).isZero();
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isZero();
    }

    @Test
    void allWinsBeforeTodayTest() {
        givenHistory(List.of(win(2), win(1), win(0)));

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(3);
        assertThat(stats.wins()).isEqualTo(3);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isEqualTo(3);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void streakBreak_maxKeepsLongestRun_currentReflectsLatestRun() {
        // WON WON WON LOST WON(today)
        givenHistory(List.of(win(4), win(3), win(2), loss(1), win(0)));

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(5);
        assertThat(stats.wins()).isEqualTo(4);
        assertThat(stats.winPercent()).isEqualTo(80);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void lossTodayResetsCurrentStreakTest() {
        givenHistory(List.of(win(2), win(1), loss(0)));

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);

        assertThat(stats.gamesPlayed()).isEqualTo(3);
        assertThat(stats.wins()).isEqualTo(2);
        assertThat(stats.winPercent()).isEqualTo(67); // 2/3 rounded
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void lastWinOlderThanYesterdayTest() {
        givenHistory(List.of(win(13), win(12)));

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(2);
        assertThat(stats.wins()).isEqualTo(2);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void missingCalendarDayBreaksCurrentStreakTest() {
        // WON two days ago, no game yesterday, WON today.
        givenHistory(List.of(win(2), win(0)));

        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(1);
    }

    @Test
    void winYesterdayAndNoWinTodayTest() {
        givenHistory(List.of(win(2), win(1)));
        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(2);
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void inProgressNotIncludedAndDoesNotBreakStreakTest() {
        givenHistory(List.of(win(1), inProgress(0)));
        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(1);
        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(1);
    }

    @Test
    void winPercentageRoundedTest() {
        givenHistory(List.of(win(2), loss(1), loss(0)));
        UserStatsDto stats = userStatsService.getStats(USER_ID, TODAY);
        assertThat(stats.winPercent()).isEqualTo(33); // 1/3 rounded
    }


    private WordleSession session(LocalDate date, com.auracxeli.wordle.WordleOutcome outcome) {
        WordleSession session = new WordleSession(null, date);
        session.setOutcome(outcome);
        return session;
    }

    private WordleSession win(int daysAgo)        { return session(TODAY.minusDays(daysAgo), WON); }
    private WordleSession loss(int daysAgo)       { return session(TODAY.minusDays(daysAgo), LOST); }
    private WordleSession inProgress(int daysAgo) { return session(TODAY.minusDays(daysAgo), IN_PROGRESS); }

    private void givenHistory(List<WordleSession> oldestFirst) {
        when(sessionRepository.findByUserIdOrderByPuzzleDateAsc(USER_ID)).thenReturn(oldestFirst);
    }
}
