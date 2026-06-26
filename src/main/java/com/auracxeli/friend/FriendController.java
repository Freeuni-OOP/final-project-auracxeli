package com.auracxeli.friend;

import com.auracxeli.user.UserDetailsImpl;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/friends")
public class FriendController {

    private final FriendService friendService;

    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @GetMapping
    public String friendsPage(@AuthenticationPrincipal UserDetailsImpl currentUser, Model model) {
        Long userId = currentUser.getId();
        model.addAttribute("friends", friendService.listFriends(userId));
        model.addAttribute("pendingRequests", friendService.listPendingRequests(userId));
        return "friends";
    }

    @PostMapping("/request")
    public String sendRequest(@AuthenticationPrincipal UserDetailsImpl currentUser, @RequestParam String username,
                              RedirectAttributes redirectAttributes) {
        try {
            friendService.sendRequest(currentUser.getId(), username);
            redirectAttributes.addFlashAttribute("message", "მოთხოვნა გაიგზავნა");
        } catch (FriendshipException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/friends";
    }

    @PostMapping("/{id}/accept")
    public String accept(@AuthenticationPrincipal UserDetailsImpl currentUser, @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            friendService.acceptRequest(currentUser.getId(), id);
        } catch (FriendshipException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/friends";
    }

    @PostMapping("/{id}/decline")
    public String decline(@AuthenticationPrincipal UserDetailsImpl currentUser, @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        try {
            friendService.declineRequest(currentUser.getId(), id);
        } catch (FriendshipException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/friends";
    }

    @PostMapping("/{id}/remove")
    public String remove(@AuthenticationPrincipal UserDetailsImpl currentUser,
                         @PathVariable Long id,
                         RedirectAttributes redirectAttributes) {
        try {
            friendService.removeFriend(currentUser.getId(), id);
        } catch (FriendshipException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/friends";
    }
}
