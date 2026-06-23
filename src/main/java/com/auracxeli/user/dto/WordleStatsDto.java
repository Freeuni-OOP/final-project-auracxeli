package com.auracxeli.user.dto;

public record WordleStatsDto(
        int gamesPlayed,
        int wins,
        int winPercent,
        int currentStreak,
        int maxStreak
) { }
