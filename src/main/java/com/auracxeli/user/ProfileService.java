package com.auracxeli.user;

import com.auracxeli.achievement.Achievement;
import com.auracxeli.achievement.AchievementService;
import com.auracxeli.user.dto.AchievementView;
import com.auracxeli.user.dto.ProfileView;
import com.auracxeli.user.dto.WordleStatsDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Assembles a {@link ProfileView} for a user from the pieces that live in
 * other services (identity from {@link UserService}, stats from
 * {@link UserStatsService}). Controllers stay thin by delegating here.
 */
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserService userService;
    private final UserStatsService userStatsService;
    private final ConnectionsStatsService connectionsStatsService;
    private final AchievementService achievementService;

    /**
     * Builds the profile view for {@code username}.
     *
     * @throws UserNotFoundException if no user has that username
     */
    public ProfileView getProfile(String username) {
        User user = userService.getByUsername(username);
        WordleStatsDto wordleStats = userStatsService.getWordleStats(user.getId());
        return new ProfileView(
                user.getUsername(),
                user.getCreatedAt().toLocalDate(),
                initialsOf(user.getUsername()),
                user.getAvatar(),
                wordleStats,
                userStatsService.getWordleGuessDistribution(user.getId()),
                connectionsStatsService.getConnectionsStats(user.getId()),
                userStatsService.getWordleHistory(user.getId()),
                connectionsStatsService.getConnectionsHistory(user.getId()),
                achievements(user.getId())
        );
    }

    private List<AchievementView> achievements(Long userId) {
        Set<Achievement> earned = achievementService.getEarnedAchievements(userId);
        return Arrays.stream(Achievement.values())
                .map(a -> new AchievementView(a.getTitle(), a.getDescription(), a.getIcon(), earned.contains(a)))
                .toList();
    }

    /** First one or two characters of the username, used as an avatar badge. */
    private String initialsOf(String username) {
        String trimmed = username == null ? "" : username.trim();
        if (trimmed.isEmpty()) {
            return "?";
        }
        int count = Math.min(2, trimmed.length());
        return trimmed.substring(0, count).toUpperCase(Locale.ROOT);
    }
}
