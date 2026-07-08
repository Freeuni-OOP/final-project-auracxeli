package com.auracxeli.user.dto;

import java.time.LocalDate;

public record WordleHistoryItem(
        LocalDate puzzleDate,
        String result,
        int attemptsUsed
) { }
