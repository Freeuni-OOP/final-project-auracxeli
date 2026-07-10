package com.auracxeli.friend;

import com.auracxeli.friend.dto.ConnectionsLeaderboardEntry;
import com.auracxeli.friend.dto.LeaderboardEntry;
import com.auracxeli.friend.dto.LeaderboardView;
import com.auracxeli.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


@Controller
@RequiredArgsConstructor
public class FriendLeaderboardController {

    private final FriendLeaderboardService friendLeaderboardService;

    @GetMapping("/friends/leaderboard/wordle")
    public String showWordleLeaderboard(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        LeaderboardView<LeaderboardEntry> view = friendLeaderboardService.buildWordleLeaderboard(principal.getId());
        model.addAttribute("hasFriends", view.hasFriends());
        model.addAttribute("entries", view.entries());
        return "wordle-leaderboard";
    }
    @GetMapping("/friends/leaderboard/connections")
    public String showConnectionsLeaderboard(@AuthenticationPrincipal UserDetailsImpl principal, Model model) {
        LeaderboardView<ConnectionsLeaderboardEntry> view = friendLeaderboardService.buildConnectionsLeaderboard(principal.getId());
        model.addAttribute("hasFriends", view.hasFriends());
        model.addAttribute("entries", view.entries());
        return "connections-leaderboard";
    }
}