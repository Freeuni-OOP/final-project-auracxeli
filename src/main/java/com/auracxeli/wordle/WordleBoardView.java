package com.auracxeli.wordle;

import java.util.List;

/**
 * View model for the Wordle board template, built fresh on every render from a
 * user's daily {@link WordleSession}. Never persisted.
 */
public record WordleBoardView(
        List<List<Tile>> rows,
        WordleOutcome outcome,
        boolean readOnly,
        int attemptsUsed,
        int maxAttempts,
        String revealedWord
) {
}
