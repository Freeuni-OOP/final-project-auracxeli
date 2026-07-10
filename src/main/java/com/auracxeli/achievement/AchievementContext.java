package com.auracxeli.achievement;

/**
 * A snapshot of the stats an {@link Achievement} rule needs. Rules are pure
 * functions of this record, which keeps each one simple to unit-test.
 */
public record AchievementContext(
        int wordleWins,
        int wordleMaxStreak,
        boolean wonWordleInTwoOrFewer,
        int connectionsWins,
        int connectionsMaxStreak,
        boolean flawlessConnectionsWin,
        int totalGamesPlayed
) { }
