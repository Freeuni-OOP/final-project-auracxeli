package com.auracxeli.config;

import com.auracxeli.user.UserDetailsImpl;
import com.auracxeli.user.UserRepository;
import com.auracxeli.user.UserStatsService;
import com.auracxeli.user.dto.LevelDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Locale;

/**
 * Exposes the current user's theme ("light"/"dark") and level info to every
 * view so the shared layout (navbar) can render them without every controller
 * wiring them in manually.
 */
@ControllerAdvice
@RequiredArgsConstructor
public class GlobalModelAttributes {

    private final UserRepository userRepository;
    private final UserStatsService userStatsService;

    @ModelAttribute("theme")
    public String theme(@AuthenticationPrincipal UserDetailsImpl principal) {
        if (principal == null) {
            return "light";
        }
        return userRepository.findById(principal.getId())
                .map(user -> user.getThemePreference().name().toLowerCase(Locale.ROOT))
                .orElse("light");
    }

    @ModelAttribute("levelInfo")
    public LevelDto levelInfo(@AuthenticationPrincipal UserDetailsImpl principal) {
        if (principal == null) {
            return null;
        }
        return userStatsService.getLevelInfo(principal.getId());
    }
}