package com.auracxeli.connections;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionsDailyService {

    private final ConnectionsPuzzleRepository connectionsPuzzleRepository;
    // it returns the scheduled puzzle for today if there is no puzzle scheduled I return empty
    public Optional<ConnectionsPuzzle> getTodaysPuzzle() {
        return getPuzzleForDate(LocalDate.now(ZoneOffset.UTC));
    }

    /**
     * Returns the puzzle scheduled for {@code date}, or empty if none. Used by the
     * archive to let users practise past puzzles.
     */
    public Optional<ConnectionsPuzzle> getPuzzleForDate(LocalDate date) {
        Optional<ConnectionsPuzzle> result = connectionsPuzzleRepository.findByPuzzleDate(date);
        log.debug("Looked up puzzle for {}: present={}", date, result.isPresent());
        return result;
    }
}