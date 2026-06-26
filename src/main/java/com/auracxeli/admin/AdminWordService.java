package com.auracxeli.admin;

import com.auracxeli.wordle.InvalidGeorgianWordException;
import com.auracxeli.wordle.WordleGuessValidator;
import com.auracxeli.wordle.WordleWord;
import com.auracxeli.wordle.WordleWordRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminWordService {

    /** A word may not repeat within this many days (before or after) of another use. */
    static final int UNIQUENESS_WINDOW_DAYS = 60;

    /** How many upcoming days the admin list covers. */
    private static final int UPCOMING_DAYS = 10;

    private final WordleWordRepository wordleWordRepository;
    private final WordleGuessValidator wordleGuessValidator;

    /** Words scheduled for the next {@value #UPCOMING_DAYS} days, earliest first. */
    public List<WordleWord> upcomingWords() {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return wordleWordRepository.findByScheduledDateBetweenOrderByScheduledDate(
                today, today.plusDays(UPCOMING_DAYS - 1));
    }

    /**
     * Adds {@code word}, scheduling it for {@code requestedDate} if that day is given and free,
     * otherwise for the first upcoming day with no word. Adds nothing (throws) if the word is
     * not a valid Wordle guess, or if the same word is already scheduled within
     * {@value #UNIQUENESS_WINDOW_DAYS} days of the resolved day.
     *
     * @return the saved word (carrying its resolved date)
     * @throws InvalidGeorgianWordException if the word is not in the guess dictionary
     * @throws DuplicateWordException       if the 60-day uniqueness rule would be violated
     */
    @Transactional
    public WordleWord addWord(String word, LocalDate requestedDate, Long addedBy) {
        // An answer must itself be a valid guess, otherwise players could never type it to win.
        if (!wordleGuessValidator.isValid(word)) {
            throw new InvalidGeorgianWordException();
        }

        LocalDate target = (requestedDate != null && !wordleWordRepository.existsByScheduledDate(requestedDate))
                ? requestedDate
                : firstFreeDay();

        boolean usedRecently = wordleWordRepository.existsByWordAndScheduledDateBetween(
                word,
                target.minusDays(UNIQUENESS_WINDOW_DAYS),
                target.plusDays(UNIQUENESS_WINDOW_DAYS));
        if (usedRecently) {
            log.warn("Rejected word add by admin {} for {}: violates {}-day uniqueness rule",
                    addedBy, target, UNIQUENESS_WINDOW_DAYS);
            throw new DuplicateWordException("word",
                    "ეს სიტყვა გამოყენებულია ბოლო " + UNIQUENESS_WINDOW_DAYS + " დღეში");
        }

        WordleWord saved = wordleWordRepository.save(new WordleWord(word, target, addedBy));
        log.info("Admin {} scheduled word for {}", addedBy, saved.getScheduledDate());
        return saved;
    }

    /** The earliest day from today onward that has no word scheduled. */
    private LocalDate firstFreeDay() {
        LocalDate day = LocalDate.now(ZoneOffset.UTC);
        while (wordleWordRepository.existsByScheduledDate(day)) {
            day = day.plusDays(1);
        }
        return day;
    }
}
