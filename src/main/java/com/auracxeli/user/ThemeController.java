package com.auracxeli.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.net.URI;

@Controller
@RequiredArgsConstructor
public class ThemeController {

    private final UserService userService;

    @PostMapping("/theme/toggle")
    public String toggle(@AuthenticationPrincipal UserDetailsImpl currentUser,
                         @RequestHeader(value = "Referer", required = false) String referer) {
        userService.toggleTheme(currentUser.getId());
        return "redirect:" + backTo(referer);
    }

    /** Return to the page the toggle was clicked on, as a same-app relative path. */
    private String backTo(String referer) {
        if (referer != null && !referer.isBlank()) {
            try {
                URI uri = URI.create(referer);
                String path = uri.getRawPath();
                if (path != null && path.startsWith("/")) {
                    return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
                }
            } catch (IllegalArgumentException ignored) {
                // malformed Referer: fall through to home
            }
        }
        return "/";
    }
}
