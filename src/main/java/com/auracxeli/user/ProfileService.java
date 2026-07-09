package com.auracxeli.user;

import com.auracxeli.user.dto.ProfileView;
import com.auracxeli.user.dto.WordleStatsDto;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.Locale;

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
                wordleStats,
                userStatsService.getWordleGuessDistribution(user.getId()),
                connectionsStatsService.getConnectionsStats(user.getId()),
                userStatsService.getWordleHistory(user.getId()),
                connectionsStatsService.getConnectionsHistory(user.getId())
        );
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
