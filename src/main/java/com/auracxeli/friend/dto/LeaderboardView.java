package com.auracxeli.friend.dto;
import java.util.List;
public record LeaderboardView<T>(List<T> entries, boolean hasFriends) {
}