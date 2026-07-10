package com.auracxeli.friend;

import com.auracxeli.friend.dto.ConnectionsLeaderboardEntry;
import com.auracxeli.user.ConnectionsStatsService;
import com.auracxeli.user.dto.ConnectionsStatsDto;
import com.auracxeli.friend.dto.LeaderboardEntry;
import com.auracxeli.user.User;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.dto.WordleStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendLeaderboardService {

    private final FriendshipRepository fr;
    private final UserStatsService uss;

    // I build the wordle leaderboard for current user by the stats, most of them desc
    public List<LeaderboardEntry> buildWordleLeaderboard(User currentUser) {
        List<Friendship> accepted = fr.findByStatusForUser(currentUser.getId(), FriendshipStatus.ACCEPTED);
        List<User> everyone = new ArrayList<>();
        everyone.add(currentUser);

        for (Friendship friendship : accepted) {
            User other = otherUser(friendship, currentUser.getId());
            everyone.add(other);
        }
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (User user : everyone) {
            LeaderboardEntry entry = toEntry(user, currentUser.getId());
            entries.add(entry);
        }
        return rank(entries);
    }
    private User otherUser(Friendship friendship, Long currentUserId) {
        if (friendship.getRequester().getId().equals(currentUserId)) {
            return friendship.getAddressee();
        } else {
            return friendship.getRequester();
        }
    }

    private LeaderboardEntry toEntry(User user, Long currentUserId) {
        WordleStatsDto stats = uss.getWordleStats(user.getId());
        boolean isCurrentUser;
        if (user.getId().equals(currentUserId)) {
            isCurrentUser = true;
        } else {
            isCurrentUser = false;
        }
        return new LeaderboardEntry(user.getId(), user.getUsername(), stats, isCurrentUser);
    }

    List<LeaderboardEntry> rank(List<LeaderboardEntry> entries) {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(new Comparator<LeaderboardEntry>() {
            @Override
            public int compare(LeaderboardEntry a, LeaderboardEntry b) {
                if (a.stats().currentStreak() != b.stats().currentStreak()) {
                    return b.stats().currentStreak() - a.stats().currentStreak();
                }
                if (a.stats().winPercent() != b.stats().winPercent()) {
                    return b.stats().winPercent() - a.stats().winPercent();
                }
                return a.username().toLowerCase().compareTo(b.username().toLowerCase());
            }
        });
        return sorted;
    }
    private final ConnectionsStatsService connectionsStatsService;

    public List<ConnectionsLeaderboardEntry> buildConnectionsLeaderboard(User currentUser) {
        List<Friendship> accepted = fr.findByStatusForUser(currentUser.getId(), FriendshipStatus.ACCEPTED);
        List<User> everyone = new ArrayList<>();
        everyone.add(currentUser);
        for (Friendship friendship : accepted) {
            everyone.add(otherUser(friendship, currentUser.getId()));
        }
        List<ConnectionsLeaderboardEntry> entries = new ArrayList<>();
        for (User user : everyone) {
            entries.add(toConnectionsEntry(user, currentUser.getId()));
        }
        return rankConnections(entries);
    }

    private ConnectionsLeaderboardEntry toConnectionsEntry(User user, Long currentUserId) {
        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(user.getId());
        boolean isCurrentUser = user.getId().equals(currentUserId);
        return new ConnectionsLeaderboardEntry(user.getId(), user.getUsername(), stats, isCurrentUser);
    }

    List<ConnectionsLeaderboardEntry> rankConnections(List<ConnectionsLeaderboardEntry> entries) {
        List<ConnectionsLeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort((a, b) -> {
            if (a.stats().currentStreak() != b.stats().currentStreak()) {
                return b.stats().currentStreak() - a.stats().currentStreak();
            }
            double rateA = a.stats().gamesPlayed() == 0 ? 0 : (double) a.stats().gamesWon() / a.stats().gamesPlayed();
            double rateB = b.stats().gamesPlayed() == 0 ? 0 : (double) b.stats().gamesWon() / b.stats().gamesPlayed();
            if (rateA != rateB) {
                return Double.compare(rateB, rateA);
            }
            return a.username().toLowerCase().compareTo(b.username().toLowerCase());
        });
        return sorted;
    }
}