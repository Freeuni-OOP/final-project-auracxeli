package com.auracxeli.friend;

import com.auracxeli.friend.dto.ConnectionsLeaderboardEntry;
import com.auracxeli.friend.dto.LeaderboardView;
import com.auracxeli.user.ConnectionsStatsService;
import com.auracxeli.user.UserRepository;
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

    private final FriendshipRepository friendshipRepository;
    private final UserStatsService userStatsService;
    private final ConnectionsStatsService connectionsStatsService;
    private final UserRepository userRepository;
    // I build the wordle leaderboard for current user by the stats, most of them desc
    private User loadUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + userId));
    }

    public LeaderboardView<LeaderboardEntry> buildWordleLeaderboard(Long userId) {
        User currentUser = loadUser(userId);
        List<User> everyone = collectFriendGroup(currentUser);
        List<LeaderboardEntry> entries = new ArrayList<>();
        for (User user : everyone) {
            LeaderboardEntry entry = toWordleEntry(user, currentUser.getId());
            entries.add(entry);
        }
        List<LeaderboardEntry> ranked = rank(entries);
        return new LeaderboardView<>(ranked, ranked.size() > 1);
    }
    private User otherUser(Friendship friendship, Long currentUserId) {
        if (friendship.getRequester().getId().equals(currentUserId)) {
            return friendship.getAddressee();
        } else {
            return friendship.getRequester();
        }
    }

    private LeaderboardEntry toWordleEntry(User user, Long currentUserId) {
        WordleStatsDto stats = userStatsService.getWordleStats(user.getId());
        boolean isCurrentUser = user.getId().equals(currentUserId);
        return new LeaderboardEntry(user.getId(), user.getUsername(), stats, isCurrentUser);
    }

    List<LeaderboardEntry> rank(List<LeaderboardEntry> entries) {
        List<LeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(
                Comparator.comparingInt((LeaderboardEntry e) -> e.stats().currentStreak()).reversed()
                        .thenComparing(e -> e.stats().winPercent(), Comparator.reverseOrder())
                        .thenComparing(e -> e.username().toLowerCase())
        );
        return sorted;
    }


    public LeaderboardView<ConnectionsLeaderboardEntry> buildConnectionsLeaderboard(Long userId) {
        User currentUser = loadUser(userId);
       List<User> everyone = collectFriendGroup(currentUser);
        List<ConnectionsLeaderboardEntry> entries = new ArrayList<>();
        for (User user : everyone) {
            entries.add(toConnectionsEntry(user, currentUser.getId()));
        }
        List<ConnectionsLeaderboardEntry> ranked = rankConnections(entries);
        return new LeaderboardView<>(ranked, ranked.size() > 1);
    }

    private ConnectionsLeaderboardEntry toConnectionsEntry(User user, Long currentUserId) {
        ConnectionsStatsDto stats = connectionsStatsService.getConnectionsStats(user.getId());
        boolean isCurrentUser = user.getId().equals(currentUserId);
        return new ConnectionsLeaderboardEntry(user.getId(), user.getUsername(), stats, isCurrentUser);
    }

    List<ConnectionsLeaderboardEntry> rankConnections(List<ConnectionsLeaderboardEntry> entries) {
        List<ConnectionsLeaderboardEntry> sorted = new ArrayList<>(entries);
        sorted.sort(
                Comparator.comparingInt((ConnectionsLeaderboardEntry e) -> e.stats().currentStreak()).reversed()
                        .thenComparing(e -> e.stats().winPercent(), Comparator.reverseOrder())
                        .thenComparing(e -> e.username().toLowerCase())
        );
        return sorted;
    }

    private List<User> collectFriendGroup(User currentUser) {
        List<Friendship> accepted = friendshipRepository.findByStatusForUser(currentUser.getId(), FriendshipStatus.ACCEPTED);
        List<User> everyone = new ArrayList<>();
        everyone.add(currentUser);
        for (Friendship friendship : accepted) {
            everyone.add(otherUser(friendship, currentUser.getId()));
        }
        return everyone;
    }
}