package com.auracxeli.friend.dto;

import com.auracxeli.user.dto.WordleStatsDto;
// here will be 1 row from the leaderboard for each User, and also it will mark if the user is logged in or not

public record LeaderboardEntry(
        Long userId,
        String username,
        WordleStatsDto stats,
        boolean isCurrentUser
) { }
