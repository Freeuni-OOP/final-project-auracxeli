package com.auracxeli.user.dto;

import java.time.LocalDate;

/**
 * Everything the profile page needs about a user, assembled in the service
 * layer so the JPA entity never leaves it. Stats and game history sections
 * grow as later issues fill them in.
 */
public record ProfileView(
        String username,
        LocalDate joinDate,
        String initials,
        WordleStatsDto wordleStats
) { }
