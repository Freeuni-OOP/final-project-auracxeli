package com.auracxeli.admin.dto;

import java.time.LocalDate;
import java.util.List;

/** View model for a Connections puzzle scheduled on a given day, with its groups flattened. */
public record ScheduledPuzzle(
        LocalDate puzzleDate,
        List<ScheduledGroup> groups
) {
}
