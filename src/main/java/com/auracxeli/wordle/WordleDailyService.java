package com.auracxeli.wordle;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;


@Slf4j
@Service

@RequiredArgsConstructor
public class WordleDailyService {

    private final WordleWordRepository wordleWordRepository;


    /**
     * Returns the word scheduled for today (UTC), or {@link Optional#empty()}
     * if no word is scheduled for today.
     */
    public Optional<WordleWord> getTodaysWord() {
        return getWordForDate(LocalDate.now(ZoneOffset.UTC));
    }

    /**
     * Returns the word scheduled for {@code date}, or {@link Optional#empty()} if
     * none. Used by the archive to let users practise past puzzles.
     */
    public Optional<WordleWord> getWordForDate(LocalDate date) {
        Optional<WordleWord> result = wordleWordRepository.findByScheduledDate(date);
        log.debug("Looked up word for {}: present={}", date, result.isPresent());
        return result;
    }
}
