package com.auracxeli.friend;

import com.auracxeli.friend.dto.LeaderboardEntry;
import com.auracxeli.user.User;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.dto.WordleStatsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FriendLeaderboardServiceTest {

    @Mock
    private FriendshipRepository fr;

    @Mock
    private UserStatsService uss;

    @InjectMocks
    private FriendLeaderboardService fls;

    private LeaderboardEntry entry(String username, int streak, int winPercent) {
        WordleStatsDto stats = new WordleStatsDto(10, 5, winPercent, streak, streak);
        return new LeaderboardEntry(1L, username, stats, false);
    }

    @Test
    void rank_ordersByCurrentStreakDescending() {
        List<LeaderboardEntry> input = new ArrayList<>();
        input.add(entry("deme", 2, 50));
        input.add(entry("irakli", 8, 50));
        input.add(entry("shmagi", 5, 50));

        List<LeaderboardEntry> ranked = fls.rank(input);

        assertThat(ranked.get(0).username()).isEqualTo("irakli");
        assertThat(ranked.get(1).username()).isEqualTo("shmagi");
        assertThat(ranked.get(2).username()).isEqualTo("deme");
    }

    @Test
    void rank_tiesOnStreak_brokenByWinPercentDescending() {
        List<LeaderboardEntry> input = new ArrayList<>();
        input.add(entry("murtaziko", 5, 40));
        input.add(entry("sandro", 5, 80));

        List<LeaderboardEntry> ranked = fls.rank(input);

        assertThat(ranked.get(0).username()).isEqualTo("sandro");
        assertThat(ranked.get(1).username()).isEqualTo("murtaziko");
    }

    @Test
    void rank_tiesOnStreakAndWinPercent_brokenByUsernameAscending() {
        List<LeaderboardEntry> input = new ArrayList<>();
        input.add(entry("zviadi", 5, 50));
        input.add(entry("guga", 5, 50));

        List<LeaderboardEntry> ranked = fls.rank(input);

        assertThat(ranked.get(0).username()).isEqualTo("guga");
        assertThat(ranked.get(1).username()).isEqualTo("zviadi");
    }

    @Test
    void rank_usernameTiebreakIsCaseInsensitive() {
        List<LeaderboardEntry> input = new ArrayList<>();
        input.add(entry("Merabs", 5, 50));
        input.add(entry("merab", 5, 50));

        List<LeaderboardEntry> ranked = fls.rank(input);

        assertThat(ranked.get(0).username()).isEqualTo("merab");
        assertThat(ranked.get(1).username()).isEqualTo("Merabs");
    }

    @Test
    void rank_usersWithZeroWins_stillIncludedAndRankedLast() {
        List<LeaderboardEntry> input = new ArrayList<>();
        input.add(entry("damimtavrda fantazia", 3, 60));
        input.add(entry("amazec", 0, 0));

        List<LeaderboardEntry> ranked = fls.rank(input);

        assertThat(ranked.get(0).username()).isEqualTo("damimtavrda fantazia");
        assertThat(ranked.get(1).username()).isEqualTo("amazec");
        assertThat(ranked.get(1).stats().currentStreak()).isEqualTo(0);
    }
    @Test
    void buildWordleLeaderboard_includesCurrentUserAndFriends() {
        User currentUser = new User("me", "me@gmail.com", "hash");
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);

        User friendOne = new User("shen", "shen@gmail.com", "hash");
        org.springframework.test.util.ReflectionTestUtils.setField(friendOne, "id", 2L);

        Friendship friendship = new Friendship(currentUser, friendOne);

        List<Friendship> accepted = new ArrayList<>();
        accepted.add(friendship);

        when(fr.findByStatusForUser(1L, FriendshipStatus.ACCEPTED)).thenReturn(accepted);
        when(uss.getWordleStats(1L)).thenReturn(new WordleStatsDto(10, 5, 50, 3, 6));
        when(uss.getWordleStats(2L)).thenReturn(new WordleStatsDto(8, 2, 25, 1, 4));

        List<LeaderboardEntry> entries = fls.buildWordleLeaderboard(currentUser);

        assertThat(entries.size()).isEqualTo(2);
    }

    @Test
    void buildWordleLeaderboard_otherUserResolvesCorrectly_whenCurrentUserIsAddressee() {
        User currentUser = new User("me", "me@gmail.com", "hash");
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);

        User friendOne = new User("shen", "shen@gmail.com", "hash");
        org.springframework.test.util.ReflectionTestUtils.setField(friendOne, "id", 2L);

        // current user is the ADDRESSEE this time, not the requester
        Friendship friendship = new Friendship(friendOne, currentUser);

        List<Friendship> accepted = new ArrayList<>();
        accepted.add(friendship);

        when(fr.findByStatusForUser(1L, FriendshipStatus.ACCEPTED)).thenReturn(accepted);
        when(uss.getWordleStats(1L)).thenReturn(new WordleStatsDto(10, 5, 50, 3, 6));
        when(uss.getWordleStats(2L)).thenReturn(new WordleStatsDto(8, 2, 25, 1, 4));

        List<LeaderboardEntry> entries = fls.buildWordleLeaderboard(currentUser);

        boolean foundFriendOne = false;
        for (LeaderboardEntry e : entries) {
            if (e.username().equals("shen")) {
                foundFriendOne = true;
            }
        }
        assertThat(foundFriendOne).isEqualTo(true);
    }

    @Test
    void buildWordleLeaderboard_marksCurrentUserCorrectly() {
        User currentUser = new User("me", "me@gmail.com", "hash");
        org.springframework.test.util.ReflectionTestUtils.setField(currentUser, "id", 1L);

        List<Friendship> accepted = new ArrayList<>();

        when(fr.findByStatusForUser(1L, FriendshipStatus.ACCEPTED)).thenReturn(accepted);
        when(uss.getWordleStats(1L)).thenReturn(new WordleStatsDto(10, 5, 50, 3, 6));

        List<LeaderboardEntry> entries = fls.buildWordleLeaderboard(currentUser);

        assertThat(entries.size()).isEqualTo(1);
        assertThat(entries.get(0).isCurrentUser()).isEqualTo(true);
    }
}