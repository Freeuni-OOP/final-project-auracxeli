package com.auracxeli.friend.dto;

import com.auracxeli.user.dto.ConnectionsStatsDto;

public record ConnectionsLeaderboardEntry(
        Long userId,
        String username,
        ConnectionsStatsDto stats,
        boolean isCurrentUser
) { }