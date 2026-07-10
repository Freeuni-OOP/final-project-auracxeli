package com.auracxeli.connections;

import com.auracxeli.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionsBoardServiceTest {

    @Mock private ConnectionsGuessEvaluator connectionsGuessEvaluator;
    private ConnectionsBoardService boardService;

    private ConnectionsPuzzle puzzle;
    private ConnectionsSession session;

    @BeforeEach
    void setUp() throws Exception {
        boardService = new ConnectionsBoardService(connectionsGuessEvaluator);
        puzzle = puzzleWithFourGroupsOfFourWords();
        session = sessionWithId(1L);
    }

    @Test
    void freshSession_showsAllSixteenWordsAndNoProgress() {
        ConnectionsBoardView view = boardService.buildBoard(session, puzzle);

        assertEquals(16, view.remainingWords().size());
        assertEquals(0, view.mistakesUsed());
        assertEquals(4, view.maxMistakes());
        assertEquals(ConnectionsOutcome.IN_PROGRESS, view.outcome());
        assertFalse(view.readOnly());
        assertTrue(view.solvedGroups().isEmpty());
        assertTrue(view.revealedGroups().isEmpty());
    }

    @Test
    void lostSession_revealsUnsolvedGroups() {
        session.setOutcome(ConnectionsOutcome.LOST);

        ConnectionsBoardView view = boardService.buildBoard(session, puzzle);

        assertTrue(view.readOnly());
        assertEquals(4, view.revealedGroups().size());
    }

    @Test
    void classifyGuess_correctGuess_isCorrect() {
        ConnectionsGuess guess = new ConnectionsGuess(session, "a", "b", "c", "d", true, 1);

        assertEquals("correct", boardService.classifyGuess(guess, puzzle));
    }

    @Test
    void classifyGuess_threeOfFourMatch_isOneAway() {
        ConnectionsGuess guess = new ConnectionsGuess(session, "a", "b", "c", "d", false, 1);
        when(connectionsGuessEvaluator.isAlmostCorrect(any(), any())).thenReturn(true);

        assertEquals("one_away", boardService.classifyGuess(guess, puzzle));
    }

    @Test
    void classifyGuess_noNearMatch_isWrong() {
        ConnectionsGuess guess = new ConnectionsGuess(session, "a", "b", "c", "d", false, 1);
        when(connectionsGuessEvaluator.isAlmostCorrect(any(), any())).thenReturn(false);

        assertEquals("wrong", boardService.classifyGuess(guess, puzzle));
    }

    private ConnectionsPuzzle puzzleWithFourGroupsOfFourWords() {
        ConnectionsPuzzle p = new ConnectionsPuzzle(LocalDate.now());
        for (int g = 1; g <= 4; g++) {
            ConnectionsGroup group = new ConnectionsGroup(p, "group" + g, g);
            for (int w = 1; w <= 4; w++) {
                group.getWords().add(new ConnectionsWord(group, "g" + g + "w" + w));
            }
            p.getGroups().add(group);
        }
        return p;
    }

    private ConnectionsSession sessionWithId(long id) throws Exception {
        ConnectionsSession s = new ConnectionsSession(new User("tester", "tester@example.com", "hash"), LocalDate.now());
        Field idField = ConnectionsSession.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(s, id);
        return s;
    }
}
