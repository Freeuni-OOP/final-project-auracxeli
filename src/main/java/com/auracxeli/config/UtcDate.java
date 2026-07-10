package com.auracxeli.config;

import java.time.LocalDate;
import java.time.ZoneOffset;

/** All puzzle/session dates are stored and compared in UTC; use this instead of repeating the zone. */
public final class UtcDate {

    private UtcDate() {
    }

    public static LocalDate today() {
        return LocalDate.now(ZoneOffset.UTC);
    }
}
