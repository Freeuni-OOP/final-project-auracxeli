package com.auracxeli.wordle;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

/**
 * Checks whether a submitted string is a real Georgian Wordle guess by looking it
 * up in the in-memory dictionary loaded from {@code words/valid_guesses.txt}.
 * <p>
 * The word list is read once at startup into an immutable set. Lookups trim
 * whitespace and normalize Mtavruli (uppercase Georgian) to Mkhedruli first, so a
 * guess typed in caps still matches its lowercase dictionary entry.
 */
@Service
public class WordleGuessValidator {

    // Mtavruli (capital Georgian). the matching Mkhedruli
    // letter is exactly this offset lower. Java's toLowerCase() does NOT bridge
    // these two Unicode blocks, so we map code points by hand.
    private static final char MTAVRULI_START = 'Ა';
    private static final char MTAVRULI_END = 'Ჺ';
    private static final int MTAVRULI_TO_MKHEDRULI_OFFSET = 0x0BC0;

    private final Set<String> validWords;

    public WordleGuessValidator(@Value("classpath:words/valid_guesses.txt") Resource wordList) {
        this.validWords = load(wordList);
    }

    /**
     * @return {@code true} if {@code guess}, once trimmed and normalized to
     * Mkhedruli, is present in the dictionary. {@code null}, empty, wrong-length,
     * and non-Georgian input all return {@code false} (they simply aren't in the set).
     */
    public boolean isValid(String guess) {
        if (guess == null) {
            return false;
        }
        return validWords.contains(normalize(guess.trim()));
    }

    private static Set<String> load(Resource wordList) {
        Set<String> words = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(wordList.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    words.add(word);
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load Wordle word list: " + wordList, e);
        }
        return Set.copyOf(words);
    }

    private static String normalize(String input) {
        StringBuilder sb = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c >= MTAVRULI_START && c <= MTAVRULI_END) {
                sb.append((char) (c - MTAVRULI_TO_MKHEDRULI_OFFSET));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
