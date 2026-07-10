package com.auracxeli.user;

import com.auracxeli.config.UtcDate;
import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsSession;
import com.auracxeli.connections.ConnectionsSessionRepository;
import com.auracxeli.user.dto.ConnectionsHistoryItem;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Computes a user's Connections statistics: games played, games won, average
 * mistakes on a win, and current/longest daily win streaks. Mirrors
 * {@link UserStatsService}; kept as its own class so each game's stats stand alone.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionsStatsService {

    private final ConnectionsSessionRepository sessionRepository;

    public ConnectionsStatsDto getConnectionsStats(Long userId) {
        //puzzle dates are stored in UTC, so compute "today" in UTC too.
        return getConnectionsStats(userId, UtcDate.today());
    }

    // package-private overload with an injectable "today" so streak logic is testable.
    ConnectionsStatsDto getConnectionsStats(Long userId, LocalDate today) {
        // Only finished games count; drop IN_PROGRESS.
        List<ConnectionsSession> finished = sessionRepository.findByUserIdOrderByPuzzleDateAsc(userId).stream()
                .filter(session -> session.getOutcome() != ConnectionsOutcome.IN_PROGRESS)
                .toList();

        int gamesPlayed = finished.size();
        List<ConnectionsSession> wins = finished.stream()
                .filter(session -> session.getOutcome() == ConnectionsOutcome.WON)
                .toList();
        int gamesWon = wins.size();
        double averageMistakesOnWin = wins.isEmpty()
                ? 0.0
                : wins.stream().mapToInt(ConnectionsSession::getMistakesCount).average().orElse(0.0);

        log.debug("Computed Connections stats for user {}: played={} won={} avgMistakes={}",
                userId, gamesPlayed, gamesWon, averageMistakesOnWin);

        return new ConnectionsStatsDto(
                gamesPlayed,
                gamesWon,
                averageMistakesOnWin,
                currentStreak(finished, today),
                longestStreak(finished)
        );
    }

    public List<ConnectionsHistoryItem> getConnectionsHistory(Long userId) {
        return sessionRepository.findByUserIdOrderByPuzzleDateDesc(userId).stream()
                .map(session -> new ConnectionsHistoryItem(
                        session.getPuzzleDate(),
                        resultLabel(session.getOutcome()),
                        session.getGuesses().size(),
                        session.getMistakesCount()))
                .toList();
    }

    private String resultLabel(ConnectionsOutcome outcome) {
        return switch (outcome) {
            case WON -> "მოგებული";
            case LOST -> "წაგებული";
            case IN_PROGRESS -> "მიმდინარე";
        };
    }

    /** Consecutive daily wins ending on the most recent play, or 0 if that play is older than yesterday. */
    private int currentStreak(List<ConnectionsSession> finished, LocalDate today) {
        if (finished.isEmpty()) {
            return 0;
        }
        LocalDate lastDate = finished.get(finished.size() - 1).getPuzzleDate();
        if (lastDate.isBefore(today.minusDays(1))) {
            return 0;
        }

        int streak = 0;
        LocalDate expected = lastDate;
        for (int i = finished.size() - 1; i >= 0; i--) {
            ConnectionsSession session = finished.get(i);
            if (!session.getPuzzleDate().equals(expected)
                    || session.getOutcome() != ConnectionsOutcome.WON) {
                break;
            }
            streak++;
            expected = expected.minusDays(1);
        }
        return streak;
    }

    /** Longest run of wins on consecutive calendar days. */
    private int longestStreak(List<ConnectionsSession> finished) {
        int max = 0;
        int run = 0;
        LocalDate previousWin = null;
        for (ConnectionsSession session : finished) {
            if (session.getOutcome() == ConnectionsOutcome.WON) {
                boolean consecutive = previousWin != null
                        && session.getPuzzleDate().equals(previousWin.plusDays(1));
                run = consecutive ? run + 1 : 1;
                max = Math.max(max, run);
                previousWin = session.getPuzzleDate();
            } else {
                run = 0;
                previousWin = null;
            }
        }
        return max;
    }
}
