package com.auracxeli.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/profile")
    public String showOwnProfile(@AuthenticationPrincipal UserDetailsImpl currentUser, Model model) {
        model.addAttribute("profile", profileService.getProfile(currentUser.getUsername()));
        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String showUserProfile(@PathVariable String username, Model model) {
        model.addAttribute("profile", profileService.getProfile(username));
        return "profile";
    }
}
