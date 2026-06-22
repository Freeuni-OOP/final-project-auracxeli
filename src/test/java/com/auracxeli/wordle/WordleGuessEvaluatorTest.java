package com.auracxeli.wordle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static com.auracxeli.wordle.LetterFeedback.ABSENT;
import static com.auracxeli.wordle.LetterFeedback.CORRECT;
import static com.auracxeli.wordle.LetterFeedback.PRESENT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for {@link WordleGuessEvaluator}, using only Georgian words
 * (the game is fully Georgian).
 */
class WordleGuessEvaluatorTest {

    private final WordleGuessEvaluator evaluator = new WordleGuessEvaluator();

    @Test
    void allCorrect_whenGuessEqualsTarget() {
        // ბურთი
        assertEquals(
                List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT),
                evaluator.evaluateGuess("ბურთი", "ბურთი"));
    }

    @Test
    void allAbsent_whenNoLettersOverlap() {
        // ცხენი, ბაბუა. they share no letters
        assertEquals(
                List.of(ABSENT, ABSENT, ABSENT, ABSENT, ABSENT),
                evaluator.evaluateGuess("ცხენი", "ბაბუა"));
    }

    @Test
    void singleYellow_whenOneLetterIsPresentButMisplaced() {
        // თვალი, ბაბუა: only ა is shared, and it is in the wrong position
        assertEquals(
                List.of(ABSENT, ABSENT, PRESENT, ABSENT, ABSENT),
                evaluator.evaluateGuess("თვალი", "ბაბუა"));
    }

    @Test
    void duplicateGuessLetter_extrasAbsent_whenGreenUsesTheOnlyMatch() {
        // ბაბუა has two ბ; ბურთი has one ბ (matched green at index 0).
        // The second ბ must be ABSENT. (The PRESENT at index 3 is the უ.)
        assertEquals(
                List.of(CORRECT, ABSENT, ABSENT, PRESENT, ABSENT),
                evaluator.evaluateGuess("ბაბუა", "ბურთი"));
    }

    @Test
    void duplicateGuessLetter_firstYellow_secondGray_whenBothMisplaced() {
        // ბაბუა has two ა, both in the wrong spot; სკამი ("chair") has one ა.
        // Left-to-right: the first ა is yellow, the second is gray.
        assertEquals(
                List.of(ABSENT, PRESENT, ABSENT, ABSENT, ABSENT),
                evaluator.evaluateGuess("ბაბუა", "სკამი"));
    }

    @Test
    void greenAndYellow_sameLetter_whenTargetHasTwoOccurrences() {
        // The subtle one: ბომბი has ბ at 0 and 3; ბაბუა has ბ at 0 and 2.
        // Index 0 is green, and the SECOND ბ is YELLOW because the target still
        // has a spare ბ — it must NOT be marked absent.
        assertEquals(
                List.of(CORRECT, ABSENT, ABSENT, PRESENT, ABSENT),
                evaluator.evaluateGuess("ბომბი", "ბაბუა"));
    }

    @Test
    void repeatedLetters_allGreen_whenPositionsAlign() {
        // ბებია vs ბაბუა: both ბ's and the final ა line up exactly
        assertEquals(
                List.of(CORRECT, ABSENT, CORRECT, ABSENT, CORRECT),
                evaluator.evaluateGuess("ბებია", "ბაბუა"));
    }

    @Test
    void mixedRound_greensYellowsAndGrays() {
        // ვაშლი vs თვალი : ვ & ა present-but-misplaced,
        // შ absent, ლ & ი correct
        assertEquals(
                List.of(PRESENT, PRESENT, ABSENT, CORRECT, CORRECT),
                evaluator.evaluateGuess("ვაშლი", "თვალი"));
    }

    @Test
    void caseInsensitive_mtavruliGuessMatchesMkhedruli() {
        // Georgian's only upper/lower distinction is Mtavruli vs Mkhedruli.
        // "ᲑᲣᲠᲗᲘ" is ბურთი written in Mtavruli (uppercase-style).
        String mtavruliBurti = "ᲑᲣᲠᲗᲘ";
        assertEquals(
                List.of(CORRECT, CORRECT, CORRECT, CORRECT, CORRECT),
                evaluator.evaluateGuess(mtavruliBurti, "ბურთი"));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluateGuess(null, "ბურთი"));
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluateGuess("ბურთი", null));
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluateGuess("ბურ", "ბურთი"));       // too short (3 letters)
        assertThrows(IllegalArgumentException.class,
                () -> evaluator.evaluateGuess("ბურთები", "ბურთი"));   // too long (7 letters)
    }
}
