package com.auracxeli.wordle;

import com.auracxeli.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WordleBoardServiceTest {

    private static final int BOARD_ROWS = 6;

    private final WordleBoardService boardService = new WordleBoardService(new WordleGuessEvaluator());

    private User user;
    private WordleWord todaysWord;

    @BeforeEach
    void setUp() {
        user = new User("tester", "tester@example.com", "hash");
        todaysWord = new WordleWord("ვარდი", LocalDate.now(), null);
    }

    @Test
    void emptySession_rendersSixBlankRowsInProgress() {
        WordleSession session = new WordleSession(user, LocalDate.now());

        WordleBoardView view = boardService.buildBoard(session, todaysWord);

        assertEquals(BOARD_ROWS, view.rows().size());
        assertEquals(0, view.attemptsUsed());
        assertEquals(BOARD_ROWS, view.maxAttempts());
        assertEquals(WordleOutcome.IN_PROGRESS, view.outcome());
        assertFalse(view.readOnly());
        assertNull(view.revealedWord());
        assertTrue(view.rows().stream().flatMap(java.util.List::stream)
                .allMatch(t -> t.letter().isEmpty() && "t".equals(t.cssClass())));
    }

    @Test
    void correctGuess_producesAllGreenTilesAndStaysReadOnlyWhenWon() {
        WordleSession session = new WordleSession(user, LocalDate.now());
        session.getGuesses().add(new WordleGuess(session, "ვარდი", 1));
        session.setOutcome(WordleOutcome.WON);

        WordleBoardView view = boardService.buildBoard(session, todaysWord);

        assertEquals(1, view.attemptsUsed());
        assertTrue(view.readOnly());
        assertNull(view.revealedWord());
        assertTrue(view.rows().getFirst().stream().allMatch(t -> "t g".equals(t.cssClass())));
        // guessed row plus blank padding
        assertEquals(BOARD_ROWS, view.rows().size());
    }

    @Test
    void lostSession_revealsTheAnswer() {
        WordleSession session = new WordleSession(user, LocalDate.now());
        session.setOutcome(WordleOutcome.LOST);

        WordleBoardView view = boardService.buildBoard(session, todaysWord);

        assertTrue(view.readOnly());
        assertEquals("ვარდი", view.revealedWord());
    }
}
