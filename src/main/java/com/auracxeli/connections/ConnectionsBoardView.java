package com.auracxeli.connections;

import java.util.List;

/**
 * View model for the Connections board template. Built fresh on every render
 * (GET /connections or after a guess) — never persisted.
 */
public record ConnectionsBoardView(
        List<GroupView> solvedGroups,
        List<String> remainingWords,
        int mistakesUsed,
        int maxMistakes,
        ConnectionsOutcome outcome,
        boolean readOnly,
        List<GroupView> revealedGroups
) {
    public record GroupView(String category, int difficulty, List<String> words) {
    }
}