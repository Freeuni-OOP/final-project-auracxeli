package com.auracxeli.wordle;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

@Service
public class WordleDailyService {

    private final WordleWordRepository wordleWordRepository;

    public WordleDailyService(WordleWordRepository wordleWordRepository) {
        this.wordleWordRepository = wordleWordRepository;
    }

    /**
     * Returns the word scheduled for today (UTC), or {@link Optional#empty()}
     * if no word is scheduled for today.
     */
    public Optional<WordleWord> getTodaysWord() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return wordleWordRepository.findByScheduledDate(today);
    }
}
