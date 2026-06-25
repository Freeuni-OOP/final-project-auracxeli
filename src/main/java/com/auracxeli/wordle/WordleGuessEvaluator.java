package com.auracxeli.wordle;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Pure, stateless Wordle scoring: compares a 5-letter guess against the target
 * word and returns green/yellow/gray feedback for each position.
 */
@Slf4j
@Service
public class WordleGuessEvaluator {

    /** Georgian Wordle uses 5-letter words. */
    public static final int WORD_LENGTH = 5;

    /**
     * Scores {@code guess} against {@code target}, letter by letter.
     *
     * @return an immutable list of exactly {@value #WORD_LENGTH} feedback values
     * @throws IllegalArgumentException if either word is null or not 5 letters long
     */
    public List<LetterFeedback> evaluateGuess(String guess, String target) {
        if (guess == null || target == null) {
            throw new IllegalArgumentException("guess and target must not be null");
        }

        final String g = guess.toLowerCase(Locale.ROOT);
        final String t = target.toLowerCase(Locale.ROOT);

        if (g.length() != WORD_LENGTH || t.length() != WORD_LENGTH) {
            throw new IllegalArgumentException(
                    "guess and target must each be exactly " + WORD_LENGTH + " letters");
        }

        final LetterFeedback[] result = new LetterFeedback[WORD_LENGTH];

        // How many of each letter are still "available" in the target to award a yellow,
        // i.e. the target letters NOT already used up by a green (exact) match.
        final Map<Character, Integer> available = new HashMap<>();

        // ---- Pass 1: greens (exact position matches) ----
        for (int i = 0; i < WORD_LENGTH; i++) {
            final char gc = g.charAt(i);
            final char tc = t.charAt(i);
            if (gc == tc) {
                result[i] = LetterFeedback.CORRECT;
            } else {
                // This target letter wasn't matched in place, so it can still feed a yellow.
                available.merge(tc, 1, Integer::sum);
            }
        }

        // Pass 2: yellows and grays for the remaining positions
        for (int i = 0; i < WORD_LENGTH; i++) {
            if (result[i] != null) {
                continue; // already CORRECT from pass 1
            }
            final char gc = g.charAt(i);
            final int left = available.getOrDefault(gc, 0);
            if (left > 0) {
                result[i] = LetterFeedback.PRESENT;   // in the word, wrong spot
                available.put(gc, left - 1);           // consume one so duplicates can't double-count
            } else {
                result[i] = LetterFeedback.ABSENT;     // not in the word, or already used up
            }
        }

        List<LetterFeedback> feedback = List.of(result);
        // Log the guess and its feedback (not the target) to avoid leaking the answer into shared logs.
        log.debug("Scored guess '{}' (len={}) -> {}", g, WORD_LENGTH, feedback);
        return feedback;
    }
}
