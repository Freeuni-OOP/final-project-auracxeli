package com.auracxeli.config;

import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

/**
 * Exposes the current user's theme ("light"/"dark") to every view so the shared
 * layout can render the stored preference on first paint. Reads from the DB (not
 * the cached security principal) so a toggle takes effect on the next page load.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;

    @ModelAttribute("theme")
    public String theme(@AuthenticationPrincipal UserDetailsImpl principal) {
        if (principal == null) {
            return "light";
        }
        return userRepository.findById(principal.getId())
                .map(user -> user.getThemePreference().name().toLowerCase(Locale.ROOT))
                .orElse("light");
    }
}
