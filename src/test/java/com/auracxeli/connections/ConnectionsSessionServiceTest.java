package com.auracxeli.connections;

import com.auracxeli.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConnectionsSessionServiceTest {

    @Mock private ConnectionsSessionRepository connectionsSessionRepository;
    @Mock private ConnectionsGuessEvaluator connectionsGuessEvaluator;
    @Mock private ApplicationEventPublisher eventPublisher;
    @InjectMocks private ConnectionsSessionService service;

    private User user;

    @BeforeEach
    void setUp() {
        user = mock(User.class);
        lenient().when(user.getId()).thenReturn(1L);
        lenient().when(connectionsSessionRepository.save(any(ConnectionsSession.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private ConnectionsPuzzle puzzleWithFourGroups() {
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle(LocalDate.now());
        for (int i = 1; i <= 4; i++) {
            puzzle.getGroups().add(new ConnectionsGroup(puzzle, "group" + i, i));
        }
        return puzzle;
    }

    private ConnectionsSession newSession() {
        return new ConnectionsSession(user, LocalDate.now());
    }

    private Set<String> aSelection() {
        return Set.of("ერთი", "ორი", "სამი", "ოთხი");
    }


    @Test
    void getOrCreateReturnsExistingSessionTest() {
        ConnectionsSession existing = newSession();
        when(connectionsSessionRepository.findByPuzzleDateAndUserId(any(), any()))
                .thenReturn(Optional.of(existing));

        ConnectionsSession result = service.getOrCreateTodaysSession(user);

        assertSame(existing, result);
        verify(connectionsSessionRepository, never()).save(any());
    }

    @Test
    void getOrCreateCreatesNewSessionWhenNoneTest() {
        when(connectionsSessionRepository.findByPuzzleDateAndUserId(any(), any()))
                .thenReturn(Optional.empty());

        ConnectionsSession result = service.getOrCreateTodaysSession(user);

        assertEquals(ConnectionsOutcome.IN_PROGRESS, result.getOutcome());
        verify(connectionsSessionRepository).save(any(ConnectionsSession.class));
    }

    @Test
    void correctGuessMarksGroupFoundTest() {
        ConnectionsSession session = newSession();
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();
        when(connectionsGuessEvaluator.isCorrectGroup(any(), any())).thenReturn(true);

        ConnectionsSession result = service.submitGuess(session, puzzle, aSelection());

        assertEquals(1, result.getGuesses().size());
        assertTrue(result.getGuesses().get(0).isCorrect());
        assertEquals(0, result.getMistakesCount());
        assertEquals(ConnectionsOutcome.IN_PROGRESS, result.getOutcome()); // only 1 of 4 groups
    }

    @Test
    void wrongGuessIncrementsMistakesTest() {
        ConnectionsSession session = newSession();
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();
        when(connectionsGuessEvaluator.isCorrectGroup(any(), any())).thenReturn(false);

        ConnectionsSession result = service.submitGuess(session, puzzle, aSelection());

        assertEquals(1, result.getMistakesCount());
        assertEquals(ConnectionsOutcome.IN_PROGRESS, result.getOutcome());
    }

    @Test
    void fourthMistakeMarksLostTest() {
        ConnectionsSession session = newSession();
        session.incrementMistakes();
        session.incrementMistakes();
        session.incrementMistakes(); // already 3 mistakes
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();
        when(connectionsGuessEvaluator.isCorrectGroup(any(), any())).thenReturn(false);

        ConnectionsSession result = service.submitGuess(session, puzzle, aSelection());

        assertEquals(4, result.getMistakesCount());
        assertEquals(ConnectionsOutcome.LOST, result.getOutcome());
    }

    @Test
    void allGroupsFoundMarksWonTest() {
        ConnectionsSession session = newSession();
        // three groups already solved
        for (int i = 1; i <= 3; i++) {
            session.getGuesses().add(new ConnectionsGuess(session, "a", "b", "c", "d", true, i));
        }
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();
        when(connectionsGuessEvaluator.isCorrectGroup(any(), any())).thenReturn(true);

        ConnectionsSession result = service.submitGuess(session, puzzle, aSelection());

        assertEquals(4, result.getGuesses().stream().filter(ConnectionsGuess::isCorrect).count());
        assertEquals(ConnectionsOutcome.WON, result.getOutcome());
    }

    @Test
    void submitToFinishedSessionThrowsTest() {
        ConnectionsSession session = newSession();
        session.setOutcome(ConnectionsOutcome.WON);
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();

        assertThrows(AlreadyCompletedException.class,
                () -> service.submitGuess(session, puzzle, aSelection()));
        verify(connectionsSessionRepository, never()).save(any());
    }

    @Test
    void invalidSelectionSizeThrowsTest() {
        ConnectionsSession session = newSession();
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();

        assertThrows(InvalidSelectionException.class,
                () -> service.submitGuess(session, puzzle, Set.of("ერთი", "ორი", "სამი")));
        verify(connectionsSessionRepository, never()).save(any());
    }

    @Test
    void nullSelectionThrowsTest() {
        ConnectionsSession session = newSession();
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();

        assertThrows(InvalidSelectionException.class,
                () -> service.submitGuess(session, puzzle, null));
        verify(connectionsSessionRepository, never()).save(any());
    }

    @Test
    void nullWordInSelectionThrowsTest() {
        ConnectionsSession session = newSession();
        ConnectionsPuzzle puzzle = puzzleWithFourGroups();
        Set<String> selection = new HashSet<>(Arrays.asList("ერთი", "ორი", "სამი", null));

        assertThrows(InvalidSelectionException.class,
                () -> service.submitGuess(session, puzzle, selection));
        verify(connectionsSessionRepository, never()).save(any());
    }
}
