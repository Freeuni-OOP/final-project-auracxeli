package com.auracxeli.admin;

import com.auracxeli.admin.dto.UserRow;
import com.auracxeli.user.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

@Slf4j
@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public String showUsers(@RequestParam(required = false, defaultValue = "") String q,
                            @AuthenticationPrincipal UserDetailsImpl admin,
                            Model model) {
        model.addAttribute("users", adminUserService.listUsers(q));
        model.addAttribute("currentAdminId", admin.getId());
        model.addAttribute("q", q);
        return "admin/users";
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id,
                               @RequestParam(required = false, defaultValue = "") String q,
                               @AuthenticationPrincipal UserDetailsImpl admin,
                               RedirectAttributes redirectAttributes) {
        try {
            UserRow user = adminUserService.toggleActive(id, admin.getId());
            redirectAttributes.addFlashAttribute("message", user.active()
                    ? "მომხმარებელი განიბლოკა"
                    : "მომხმარებელი დაიბლოკა");
        } catch (AdminUserActionDeniedException e) {
            log.warn("Admin user toggle rejected for target {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectToUsers(q);
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             @RequestParam(required = false, defaultValue = "") String q,
                             @AuthenticationPrincipal UserDetailsImpl admin,
                             RedirectAttributes redirectAttributes) {
        try {
            adminUserService.deleteUser(id, admin.getId());
            redirectAttributes.addFlashAttribute("message", "მომხმარებელი წაიშალა");
        } catch (AdminUserActionDeniedException e) {
            log.warn("Admin user deletion rejected for target {}: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return redirectToUsers(q);
    }

    private String redirectToUsers(String q) {
        String normalizedQuery = q == null ? "" : q.trim();
        if (normalizedQuery.isEmpty()) {
            return "redirect:/admin/users";
        }
        String uri = UriComponentsBuilder.fromPath("/admin/users")
                .queryParam("q", normalizedQuery)
                .build()
                .encode()
                .toUriString();
        return "redirect:" + uri;
    }
}
