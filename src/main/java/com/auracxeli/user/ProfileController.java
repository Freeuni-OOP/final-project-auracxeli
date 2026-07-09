package com.auracxeli.user;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/profile")
    public String showOwnProfile(@AuthenticationPrincipal UserDetailsImpl currentUser, Model model) {
        model.addAttribute("profile", profileService.getProfile(currentUser.getUsername()));
        model.addAttribute("isOwnProfile", true);
        return "profile";
    }

    @GetMapping("/profile/{username}")
    public String showUserProfile(@PathVariable String username,
                                   @AuthenticationPrincipal UserDetailsImpl currentUser,
                                   Model model) {
        model.addAttribute("profile", profileService.getProfile(username));
        model.addAttribute("isOwnProfile", currentUser != null && currentUser.getUsername().equals(username));
        return "profile";
    }
}
