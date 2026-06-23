package com.auracxeli.user;

import com.auracxeli.user.dto.UserStatsDto;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ProfileController {

    private final UserStatsService userStatsService;

    public ProfileController(UserStatsService userStatsService) {
        this.userStatsService = userStatsService;
    }

    @GetMapping("/profile")
    public String showProfile(@AuthenticationPrincipal UserDetailsImpl currentUser, Model model) {
        UserStatsDto stats = userStatsService.getStats(currentUser.getId());
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("stats", stats);
        return "profile";
    }
}
