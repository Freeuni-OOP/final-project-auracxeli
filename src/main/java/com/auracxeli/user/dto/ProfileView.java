package com.auracxeli.user.dto;

import java.time.LocalDate;
import java.util.List;

public record ProfileView(
        String username,
        LocalDate joinDate,
        String initials,
        WordleStatsDto wordleStats,
        List<GuessBucket> guessDistribution,
        ConnectionsStatsDto connectionsStats,
        List<WordleHistoryItem> wordleHistory,
        List<ConnectionsHistoryItem> connectionsHistory
) { }
