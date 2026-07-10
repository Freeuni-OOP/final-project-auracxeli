package com.auracxeli.admin.dto;

import java.time.LocalDate;

/** View row for a Wordle word scheduled on a given day. */
public record ScheduledWord(
        Long id,
        String word,
        LocalDate scheduledDate
) {
}
