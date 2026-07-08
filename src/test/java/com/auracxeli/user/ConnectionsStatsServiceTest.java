package com.auracxeli.user;

import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsSession;
import com.auracxeli.connections.ConnectionsSessionRepository;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static com.auracxeli.connections.ConnectionsOutcome.IN_PROGRESS;
import static com.auracxeli.connections.ConnectionsOutcome.LOST;
import static com.auracxeli.connections.ConnectionsOutcome.WON;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionsStatsServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 23);

    @Mock private ConnectionsSessionRepository sessionRepository;
    @InjectMocks private ConnectionsStatsService connectionsStatsService;

    @Test
    void noStatsWhenNoGamesPlayedTest() {
        givenHistory(List.of());

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isZero();
        assertThat(stats.gamesWon()).isZero();
        assertThat(stats.averageMistakesOnWin()).isZero();
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isZero();
    }

    @Test
    void countsPlayedAndWonAfterWinAndLossTest() {
        // a loss yesterday and a win today: both are "played", one is "won".
        givenHistory(List.of(loss(1, 4), win(0, 2)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(2);
        assertThat(stats.gamesWon()).isEqualTo(1);
    }

    @Test
    void averageMistakesCountsOnlyWinsTest() {
        // wins with 0 and 3 mistakes -> avg 1.5; the loss's 4 mistakes are ignored.
        givenHistory(List.of(win(2, 0), win(1, 3), loss(0, 4)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.gamesWon()).isEqualTo(2);
        assertThat(stats.averageMistakesOnWin()).isEqualTo(1.5);
    }

    @Test
    void averageMistakesIsZeroWhenNoWinsTest() {
        givenHistory(List.of(loss(1, 4), loss(0, 4)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(2);
        assertThat(stats.gamesWon()).isZero();
        assertThat(stats.averageMistakesOnWin()).isZero();
    }

    @Test
    void currentAndMaxStreakCountConsecutiveWinsTest() {
        givenHistory(List.of(win(2, 1), win(1, 0), win(0, 2)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(3);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void lossBreaksCurrentStreakButMaxKeepsLongestRunTest() {
        // WON WON WON LOST WON(today)
        givenHistory(List.of(win(4, 1), win(3, 1), win(2, 1), loss(1, 4), win(0, 0)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void currentStreakZeroWhenLastGameOlderThanYesterdayTest() {
        givenHistory(List.of(win(13, 1), win(12, 0)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void missingCalendarDayBreaksCurrentStreakTest() {
        // won two days ago, none yesterday, won today.
        givenHistory(List.of(win(2, 0), win(0, 0)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(1);
    }

    @Test
    void inProgressExcludedFromStatsTest() {
        givenHistory(List.of(win(1, 1), inProgress(0)));

        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(1);
        assertThat(stats.gamesWon()).isEqualTo(1);
        assertThat(stats.currentStreak()).isEqualTo(1);
    }

    private ConnectionsSession session(LocalDate date, ConnectionsOutcome outcome, int mistakes) {
        ConnectionsSession session = new ConnectionsSession(null, date);
        session.setOutcome(outcome);
        for (int i = 0; i < mistakes; i++) {
            session.incrementMistakes();
        }
        return session;
    }

    private ConnectionsSession win(int daysAgo, int mistakes)  { return session(TODAY.minusDays(daysAgo), WON, mistakes); }
    private ConnectionsSession loss(int daysAgo, int mistakes) { return session(TODAY.minusDays(daysAgo), LOST, mistakes); }
    private ConnectionsSession inProgress(int daysAgo)         { return session(TODAY.minusDays(daysAgo), IN_PROGRESS, 0); }

    private void givenHistory(List<ConnectionsSession> oldestFirst) {
        when(sessionRepository.findByUserIdOrderByPuzzleDateAsc(USER_ID)).thenReturn(oldestFirst);
    }
}
