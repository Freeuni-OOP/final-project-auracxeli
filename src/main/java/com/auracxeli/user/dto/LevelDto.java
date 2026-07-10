package com.auracxeli.user.dto;

/**
 * Level derived from total finished games (Wordle + Connections combined).
 * No XP system — level is a pure function of the finished-game count.
 *
 * @param level             1 + floor(totalFinishedGames / 5)
 * @param gamesRemaining    games still needed to reach the next level (1..5)
 * @param percentComplete   progress toward the next level, 0..100
 */
public record LevelDto(int level, int gamesRemaining, int percentComplete) {
}