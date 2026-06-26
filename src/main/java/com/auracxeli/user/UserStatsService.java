package com.auracxeli.user;

import com.auracxeli.user.dto.WordleStatsDto;
import com.auracxeli.wordle.WordleOutcome;
import com.auracxeli.wordle.WordleSession;
import com.auracxeli.wordle.WordleSessionRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatsService {

    private final WordleSessionRepository sessionRepository;

    public WordleStatsDto getWordleStats(Long userId) {
        //we made puzzle dates UTC so i will use UTC here too.
        return getWordleStats(userId, LocalDate.now(ZoneOffset.UTC));
    }

    WordleStatsDto getWordleStats(Long userId, LocalDate today) {
        //exlclude nonfinished games aka IN_progress here from calculations.
        List<WordleSession> finished = sessionRepository.findByUserIdOrderByPuzzleDateAsc(userId).stream()
                .filter(session -> session.getOutcome() != WordleOutcome.IN_PROGRESS)
                .toList();

        int gamesPlayed = finished.size();
        int wins = (int) finished.stream()
                .filter(session -> session.getOutcome() == WordleOutcome.WON)
                .count();
        int winPercent = gamesPlayed == 0 ? 0 : Math.round((float) wins * 100 / gamesPlayed);

        log.debug("Computed Wordle stats for user {}: played={} wins={} win%={}",
                userId, gamesPlayed, wins, winPercent);

        return new WordleStatsDto(
                gamesPlayed,
                wins,
                winPercent,
                wordleCurrentStreak(finished, today),
                wordleMaxStreak(finished)
        );
    }

    private int wordleCurrentStreak(List<WordleSession> finished, LocalDate today) {
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
            WordleSession session = finished.get(i);
            //if missing or loss
            if (!session.getPuzzleDate().equals(expected)
                    || session.getOutcome() != WordleOutcome.WON) {
                break;
            }
            streak++;
            expected = expected.minusDays(1);
        }
        return streak;
    }

    private int wordleMaxStreak(List<WordleSession> finished) {
        int max = 0;
        int run = 0;
        LocalDate previousWin = null;
        for (WordleSession session : finished) {
            if (session.getOutcome() == WordleOutcome.WON) {
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
