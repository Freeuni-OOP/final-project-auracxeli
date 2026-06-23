package com.auracxeli.wordle;

import com.auracxeli.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WordleSessionServiceTest {

    @Mock
    private WordleSessionRepository wordleSessionRepository;
    @Mock
    private WordleDailyService wordleDailyService;
    @InjectMocks
    private WordleSessionService wordleSessionService;

    private final WordleGuessEvaluator evaluator = new WordleGuessEvaluator();
    private User user;
    private WordleWord todaysWord;

    @BeforeEach
    void setUp() throws Exception {
        wordleSessionService = new WordleSessionService(wordleSessionRepository, wordleDailyService, evaluator);
        user = userWithId(1L);
        todaysWord = new WordleWord("ვარდი", LocalDate.now(), null);
    }

    private static User userWithId(long id) throws Exception {
        User u = new User("tester", "tester@example.com", "hash");
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(u, id);
        return u;
    }

    @Test
    void getOrCreateTodaysSession_returnsExistingSession() {
        WordleSession existing = new WordleSession(user, LocalDate.now(java.time.ZoneOffset.UTC));
        when(wordleSessionRepository.findByUserIdAndPuzzleDate(eq(1L), any()))
                .thenReturn(Optional.of(existing));

        WordleSession result = wordleSessionService.getOrCreateTodaysSession(user);

        assertSame(existing, result);
        verify(wordleSessionRepository, never()).save(any());
    }

    @Test
    void getOrCreateTodaysSession_createsNewSession_whenNoneExists() {
        when(wordleSessionRepository.findByUserIdAndPuzzleDate(eq(1L), any()))
                .thenReturn(Optional.empty());
        when(wordleDailyService.getTodaysWord()).thenReturn(Optional.of(todaysWord));
        when(wordleSessionRepository.save(any(WordleSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WordleSession result = wordleSessionService.getOrCreateTodaysSession(user);

        assertEquals(user, result.getUser());
        verify(wordleSessionRepository).save(any(WordleSession.class));
    }

    @Test
    void getOrCreateTodaysSession_throws_whenNoDailyWord() {
        when(wordleSessionRepository.findByUserIdAndPuzzleDate(eq(1L), any()))
                .thenReturn(Optional.empty());
        when(wordleDailyService.getTodaysWord()).thenReturn(Optional.empty());

        assertThrows(WordleSessionService.NoDailyWordException.class,
                () -> wordleSessionService.getOrCreateTodaysSession(user));
        verify(wordleSessionRepository, never()).save(any());
    }

    @Test
    void submitGuess_correctGuess_marksSessionWon() {
        WordleSession session = new WordleSession(user, LocalDate.now());
        when(wordleSessionRepository.save(any(WordleSession.class))).thenAnswer(inv -> inv.getArgument(0));

        WordleSessionService.GuessResult result = wordleSessionService.submitGuess(session, todaysWord, "ვარდი");

        assertEquals(WordleOutcome.WON, result.outcome());
        assertEquals(WordleOutcome.WON, session.getOutcome());
        assertTrue(result.feedback().stream().allMatch(f -> f == LetterFeedback.CORRECT));
        assertEquals(1, session.getGuesses().size());
    }

    @Test
    void submitGuess_sixthWrongGuess_marksSessionLost() {
        WordleSession session = new WordleSession(user, LocalDate.now());
        when(wordleSessionRepository.save(any(WordleSession.class))).thenAnswer(inv -> inv.getArgument(0));

        for (int i = 0; i < 5; i++) {
            wordleSessionService.submitGuess(session, todaysWord, "ბარგი");
        }
        WordleSessionService.GuessResult result = wordleSessionService.submitGuess(session, todaysWord, "ბარგი");

        assertEquals(WordleOutcome.LOST, result.outcome());
        assertEquals(WordleOutcome.LOST, session.getOutcome());
        assertEquals(6, session.getGuesses().size());
    }

    @Test
    void submitGuess_rejectsGuess_whenSessionAlreadyCompleted() {
        WordleSession session = new WordleSession(user, LocalDate.now());
        session.setOutcome(WordleOutcome.WON);

        assertThrows(WordleSessionService.AlreadyCompletedException.class,
                () -> wordleSessionService.submitGuess(session, todaysWord, "ვარდი"));
        verify(wordleSessionRepository, never()).save(any());
    }

    @Test
    void submitGuess_rejectsInvalidLengthGuess_withoutPersisting() {
        WordleSession session = new WordleSession(user, LocalDate.now());

        assertThrows(WordleSessionService.InvalidGuessException.class,
                () -> wordleSessionService.submitGuess(session, todaysWord, "ბარ"));
        assertEquals(0, session.getGuesses().size());
        verify(wordleSessionRepository, never()).save(any());
    }
}
