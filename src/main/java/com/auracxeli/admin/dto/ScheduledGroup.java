package com.auracxeli.admin.dto;

/** A single category group within a {@link ScheduledPuzzle}, for display only. */
public record ScheduledGroup(
        String category,
        int difficulty
) {
}
