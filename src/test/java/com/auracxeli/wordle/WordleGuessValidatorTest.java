package com.auracxeli.wordle;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleGuessValidatorTest {

    // Mtavruli letters sit exactly this far above their Mkhedruli counterparts.
    private static final int MTAVRULI_OFFSET = 0x0BC0;

    private final WordleGuessValidator validator =
            new WordleGuessValidator(new ClassPathResource("words/test_guesses.txt"));

    @Test
    void isValid_wordInDictionary_returnsTrue() {
        assertTrue(validator.isValid("ბურთი"));
    }

    @Test
    void isValid_realWordNotInDictionary_returnsFalse() {
        assertFalse(validator.isValid("ვაშლი"));
    }

    @Test
    void isValid_null_returnsFalse() {
        assertFalse(validator.isValid(null));
    }

    @Test
    void isValid_empty_returnsFalse() {
        assertFalse(validator.isValid(""));
    }

    @Test
    void isValid_whitespaceOnly_returnsFalse() {
        assertFalse(validator.isValid("     "));
    }

    @Test
    void isValid_tooShort_returnsFalse() {
        assertFalse(validator.isValid("ბურ"));
    }

    @Test
    void isValid_tooLong_returnsFalse() {
        assertFalse(validator.isValid("ბურთები"));
    }

    @Test
    void isValid_asciiGarbage_returnsFalse() {
        assertFalse(validator.isValid("abcde"));
    }

    @Test
    void isValid_mtavruliUppercase_isNormalizedAndMatches() {
        assertTrue(validator.isValid(toMtavruli("ბურთი")));
    }

    @Test
    void isValid_surroundingWhitespace_isTrimmedAndMatches() {
        assertTrue(validator.isValid("  ბურთი  "));
    }

    /**
     * Shifts each Mkhedruli letter up into its Mtavruli (capital) form.
     */
    private static String toMtavruli(String mkhedruli) {
        StringBuilder sb = new StringBuilder(mkhedruli.length());
        for (int i = 0; i < mkhedruli.length(); i++) {
            sb.append((char) (mkhedruli.charAt(i) + MTAVRULI_OFFSET));
        }
        return sb.toString();
    }
}
