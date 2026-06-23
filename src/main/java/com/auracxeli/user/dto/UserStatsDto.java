package com.auracxeli.user.dto;

public record UserStatsDto(
        int gamesPlayed,
        int wins,
        int winPercent,
        int currentStreak,
        int maxStreak
) { }
