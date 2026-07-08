package com.auracxeli.user.dto;

import java.time.LocalDate;

public record ConnectionsHistoryItem(
        LocalDate puzzleDate,
        String result,
        int attemptsUsed,
        int mistakesCount
) { }
