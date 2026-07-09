package com.auracxeli.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/profile/avatar")
@RequiredArgsConstructor
public class AvatarController {

    private final AvatarService avatarService;
    private final UserRepository userRepository;

    @GetMapping
    public String showPicker(@AuthenticationPrincipal UserDetailsImpl currentUser, Model model) {
        model.addAttribute("avatars", avatarService.availableAvatars());
        model.addAttribute("currentAvatar", currentUser.getAvatar());
        return "avatar-picker";
    }

    @PostMapping
    public String chooseAvatar(@AuthenticationPrincipal UserDetailsImpl currentUser, @RequestParam String avatar) {
        avatarService.chooseAvatar(currentUser.getId(), avatar);
        refreshPrincipal(currentUser.getId());
        return "redirect:/profile";
    }

    private void refreshPrincipal(Long userId) {
        User reloaded = userRepository.findById(userId).orElseThrow();
        UserDetailsImpl refreshed = new UserDetailsImpl(reloaded);
        Authentication current = SecurityContextHolder.getContext().getAuthentication();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(refreshed, current.getCredentials(), refreshed.getAuthorities()));
    }
}