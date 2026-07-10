package com.auracxeli.achievement;

/** Published when a user finishes (wins or loses) a game, to trigger achievement evaluation. */
public record GameFinishedEvent(Long userId) { }
