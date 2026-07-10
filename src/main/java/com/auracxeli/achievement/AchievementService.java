package com.auracxeli.achievement;

import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsSessionRepository;
import com.auracxeli.user.ConnectionsStatsService;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import com.auracxeli.user.dto.GuessBucket;
import com.auracxeli.user.dto.WordleStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Grants achievements. Rules live in {@link Achievement}; this service builds the
 * {@link AchievementContext} from the existing stats services and persists any
 * newly-earned badges. Evaluation is triggered by {@link GameFinishedEvent}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final UserAchievementRepository userAchievementRepository;
    private final UserRepository userRepository;
    private final UserStatsService userStatsService;
    private final ConnectionsStatsService connectionsStatsService;
    private final ConnectionsSessionRepository connectionsSessionRepository;

    /** Fired after a game finishes. A failure here is logged, never breaking gameplay. */
    @EventListener
    public void onGameFinished(GameFinishedEvent event) {
        try {
            evaluateAndGrant(event.userId());
        } catch (RuntimeException e) {
            log.warn("Achievement evaluation failed for user {}", event.userId(), e);
        }
    }

    /** Grants every achievement the user now qualifies for and hasn't earned yet. Idempotent. */
    @Transactional
    public void evaluateAndGrant(Long userId) {
        AchievementContext context = buildContext(userId);
        Set<Achievement> alreadyEarned = earnedSet(userId);

        for (Achievement achievement : Achievement.earnedFor(context)) {
            if (!alreadyEarned.contains(achievement)) {
                userAchievementRepository.save(
                        new UserAchievement(userRepository.getReferenceById(userId), achievement));
                log.info("User {} earned achievement {}", userId, achievement);
            }
        }
    }

    @Transactional(readOnly = true)
    public Set<Achievement> getEarnedAchievements(Long userId) {
        return earnedSet(userId);
    }

    private Set<Achievement> earnedSet(Long userId) {
        Set<Achievement> earned = EnumSet.noneOf(Achievement.class);
        userAchievementRepository.findByUserId(userId)
                .forEach(userAchievement -> earned.add(userAchievement.getAchievement()));
        return earned;
    }

    private AchievementContext buildContext(Long userId) {
        WordleStatsDto wordle = userStatsService.getWordleStats(userId);
        ConnectionsStatsDto connections = connectionsStatsService.getConnectionsStats(userId);
        List<GuessBucket> distribution = userStatsService.getWordleGuessDistribution(userId);

        boolean wonWordleInTwoOrFewer = distribution.stream()
                .filter(bucket -> bucket.guesses() <= 2)
                .mapToInt(GuessBucket::count)
                .sum() > 0;
        boolean flawlessConnectionsWin = connectionsSessionRepository
                .existsByUserIdAndOutcomeAndMistakesCount(userId, ConnectionsOutcome.WON, 0);

        return new AchievementContext(
                wordle.wins(),
                wordle.maxStreak(),
                wonWordleInTwoOrFewer,
                connections.gamesWon(),
                connections.maxStreak(),
                flawlessConnectionsWin,
                wordle.gamesPlayed() + connections.gamesPlayed()
        );
    }
}
