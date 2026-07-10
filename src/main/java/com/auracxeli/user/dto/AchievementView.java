package com.auracxeli.user.dto;

/** One badge on the profile: its display text and whether this user has earned it. */
public record AchievementView(
        String title,
        String description,
        String icon,
        boolean earned
) { }
