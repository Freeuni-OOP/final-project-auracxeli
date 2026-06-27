package com.auracxeli.friend;

import com.auracxeli.friend.dto.LeaderboardEntry;
import com.auracxeli.user.User;
import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Optional;

@Controller
public class FriendLeaderboardController {

    private final FriendLeaderboardService fls;
    private final UserRepository ur;

    public FriendLeaderboardController(FriendLeaderboardService fls, UserRepository ur) {
        this.fls = fls;
        this.ur = ur;
    }

    @GetMapping("/friends/leaderboard/wordle")
    public String showWordleLeaderboard(Authentication authentication, Model model) {
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = principal.getId();

        Optional<User> userOpt = ur.findById(userId);
        User currentUser;
        if (userOpt.isPresent()) {
            currentUser = userOpt.get();
        } else {
            throw new IllegalStateException("Authenticated user not found: " + userId);
        }

        List<LeaderboardEntry> entries = fls.buildWordleLeaderboard(currentUser);

        boolean hasFriends;
        if (entries.size() > 1) {
            hasFriends = true;
        } else {
            hasFriends = false;
        }

        model.addAttribute("hasFriends", hasFriends);
        model.addAttribute("entries", entries);

        return "wordle-leaderboard";
    }
}