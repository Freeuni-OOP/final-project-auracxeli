package com.auracxeli.user.dto;

public record ConnectionsStatsDto(
        int gamesPlayed,
        int gamesWon,
        double averageMistakesOnWin,
        int currentStreak,
        int maxStreak,
        int winPercent
) { }
