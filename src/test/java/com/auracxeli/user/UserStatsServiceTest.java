package com.auracxeli.user;

import com.auracxeli.connections.ConnectionsSessionRepository;
import com.auracxeli.user.dto.GuessBucket;
import com.auracxeli.user.dto.WordleHistoryItem;
import com.auracxeli.user.dto.WordleStatsDto;
import com.auracxeli.wordle.WordleGuess;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatsServiceTest {

    private static final Long USER_ID = 1L;
    private static final LocalDate TODAY = LocalDate.of(2026, 6, 23);

    @Mock private WordleSessionRepository sessionRepository;
    @Mock private ConnectionsSessionRepository connectionsSessionRepository;
    @InjectMocks private UserStatsService userStatsService;


    @Test
    void wordleNoStatsWhenNoGamesPlayedTest() {
        givenHistory(List.of());

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isZero();
        assertThat(stats.wins()).isZero();
        assertThat(stats.winPercent()).isZero();
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isZero();
    }

    @Test
    void wordleAllWinsBeforeTodayTest() {
        givenHistory(List.of(win(2), win(1), win(0)));

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(3);
        assertThat(stats.wins()).isEqualTo(3);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isEqualTo(3);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void wordleStreakBreakButMaxKeepsLongestRunAndCurrKeepsLastRunTest() {
        // WON WON WON LOST WON(today)
        givenHistory(List.of(win(4), win(3), win(2), loss(1), win(0)));

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(5);
        assertThat(stats.wins()).isEqualTo(4);
        assertThat(stats.winPercent()).isEqualTo(80);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(3);
    }

    @Test
    void wordleLossTodayResetsCurrentStreakTest() {
        givenHistory(List.of(win(2), win(1), loss(0)));

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);

        assertThat(stats.gamesPlayed()).isEqualTo(3);
        assertThat(stats.wins()).isEqualTo(2);
        assertThat(stats.winPercent()).isEqualTo(67); // 2/3 rounded
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void wordleLastWinOlderThanYesterdayTest() {
        givenHistory(List.of(win(13), win(12)));

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(2);
        assertThat(stats.wins()).isEqualTo(2);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isZero();
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void wordleMissingCalendarDayBreaksCurrentStreakTest() {
        // WON two days ago, no game yesterday, WON today.
        givenHistory(List.of(win(2), win(0)));

        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(1);
    }

    @Test
    void wordleWinYesterdayAndNoWinTodayTest() {
        givenHistory(List.of(win(2), win(1)));
        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.currentStreak()).isEqualTo(2);
        assertThat(stats.maxStreak()).isEqualTo(2);
    }

    @Test
    void wordleInProgressNotIncludedAndDoesNotBreakStreakTest() {
        givenHistory(List.of(win(1), inProgress(0)));
        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.gamesPlayed()).isEqualTo(1);
        assertThat(stats.wins()).isEqualTo(1);
        assertThat(stats.winPercent()).isEqualTo(100);
        assertThat(stats.currentStreak()).isEqualTo(1);
        assertThat(stats.maxStreak()).isEqualTo(1);
    }

    @Test
    void wordleWinPercentageRoundedTest() {
        givenHistory(List.of(win(2), loss(1), loss(0)));
        WordleStatsDto stats = userStatsService.getWordleStats(USER_ID, TODAY);
        assertThat(stats.winPercent()).isEqualTo(33); // 1/3 rounded
    }

    @Test
    void guessDistributionNormalizesLongestBarTo100PercentTest() {
        when(sessionRepository.findGuessDistribution(USER_ID))
                .thenReturn(List.of(new Object[]{3, 4L}, new Object[]{4, 2L}));

        List<GuessBucket> dist = userStatsService.getWordleGuessDistribution(USER_ID);

        assertThat(dist).hasSize(6);
        assertThat(dist.get(2).guesses()).isEqualTo(3);
        assertThat(dist.get(2).count()).isEqualTo(4);    // tallest bar
        assertThat(dist.get(2).percent()).isEqualTo(100);
        assertThat(dist.get(3).count()).isEqualTo(2);
        assertThat(dist.get(3).percent()).isEqualTo(50); // 2 of 4
        assertThat(dist.get(0).count()).isZero();
        assertThat(dist.get(0).percent()).isZero();
    }

    @Test
    void guessDistributionAllZeroWhenNoWinsTest() {
        // findGuessDistribution returns empty by default -> all six bars zero
        List<GuessBucket> dist = userStatsService.getWordleGuessDistribution(USER_ID);

        assertThat(dist).hasSize(6);
        assertThat(dist).allSatisfy(bar -> {
            assertThat(bar.count()).isZero();
            assertThat(bar.percent()).isZero();
        });
    }

    @Test
    void wordleHistoryReturnsNewestGamesFirstTest() {
        WordleSession newest = winWithGuesses(0, 3);
        WordleSession older = lossWithGuesses(1, 6);
        when(sessionRepository.findByUserIdOrderByPuzzleDateDesc(USER_ID))
                .thenReturn(List.of(newest, older));

        List<WordleHistoryItem> history = userStatsService.getWordleHistory(USER_ID);

        assertThat(history).hasSize(2);
        assertThat(history.get(0).puzzleDate()).isEqualTo(TODAY);
        assertThat(history.get(0).result()).isEqualTo("მოგებული");
        assertThat(history.get(0).attemptsUsed()).isEqualTo(3);
        assertThat(history.get(1).puzzleDate()).isEqualTo(TODAY.minusDays(1));
        assertThat(history.get(1).result()).isEqualTo("წაგებული");
        assertThat(history.get(1).attemptsUsed()).isEqualTo(6);
    }

    @Test
    void calculateLevel_zeroGamesIsLevelOne() {
        assertThat(UserStatsService.calculateLevel(0))
                .isEqualTo(new com.auracxeli.user.dto.LevelDto(1, 5, 0));
    }

    @Test
    void calculateLevel_midwayThroughLevelOne() {
        assertThat(UserStatsService.calculateLevel(3))
                .isEqualTo(new com.auracxeli.user.dto.LevelDto(1, 2, 60));
    }

    @Test
    void calculateLevel_exactlyAtLevelUpBoundary() {
        assertThat(UserStatsService.calculateLevel(5))
                .isEqualTo(new com.auracxeli.user.dto.LevelDto(2, 5, 0));
    }

    @Test
    void calculateLevel_higherLevelThreshold() {
        assertThat(UserStatsService.calculateLevel(27))
                .isEqualTo(new com.auracxeli.user.dto.LevelDto(6, 3, 40));
    }

    @Test
    void calculateLevel_rejectsNegativeInput() {
        assertThatThrownBy(() -> UserStatsService.calculateLevel(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void getLevelInfo_sumsFinishedGamesAcrossBothGames() {
        when(sessionRepository.countByUserIdAndOutcomeNot(USER_ID, IN_PROGRESS)).thenReturn(3L);
        when(connectionsSessionRepository.countByUserIdAndOutcomeNot(USER_ID, com.auracxeli.connections.ConnectionsOutcome.IN_PROGRESS))
                .thenReturn(2L);

        var result = userStatsService.getLevelInfo(USER_ID);

        assertThat(result.level()).isEqualTo(2);
        assertThat(result.gamesRemaining()).isEqualTo(5);
        assertThat(result.percentComplete()).isZero();
    }


    private WordleSession session(LocalDate date, com.auracxeli.wordle.WordleOutcome outcome) {
        WordleSession session = new WordleSession(null, date);
        session.setOutcome(outcome);
        return session;
    }

    private WordleSession win(int daysAgo)        { return session(TODAY.minusDays(daysAgo), WON); }
    private WordleSession loss(int daysAgo)       { return session(TODAY.minusDays(daysAgo), LOST); }
    private WordleSession inProgress(int daysAgo) { return session(TODAY.minusDays(daysAgo), IN_PROGRESS); }

    private WordleSession winWithGuesses(int daysAgo, int guesses) {
        return sessionWithGuesses(daysAgo, WON, guesses);
    }

    private WordleSession lossWithGuesses(int daysAgo, int guesses) {
        return sessionWithGuesses(daysAgo, LOST, guesses);
    }

    private WordleSession sessionWithGuesses(int daysAgo, com.auracxeli.wordle.WordleOutcome outcome, int guesses) {
        WordleSession session = session(TODAY.minusDays(daysAgo), outcome);
        for (int i = 1; i <= guesses; i++) {
            session.getGuesses().add(new WordleGuess(session, "ვარდი", i));
        }
        return session;
    }

    private void givenHistory(List<WordleSession> oldestFirst) {
        when(sessionRepository.findByUserIdOrderByPuzzleDateAsc(USER_ID)).thenReturn(oldestFirst);
    }
}
