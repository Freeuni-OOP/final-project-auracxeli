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
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        Optional<ConnectionsPuzzle> result = connectionsPuzzleRepository.findByPuzzleDate(today);
        log.debug("Looked up daily puzzle for {}: present={}", today, result.isPresent());
        return result;
    }
}