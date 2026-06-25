package com.auracxeli.wordle;

import com.auracxeli.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;

/**
 * Ties together today's word, a user's daily session, and guess evaluation:
 * the orchestration {@link WordleController} needs but that doesn't belong
 * in any single existing repository/service.
 */
@Slf4j
@Service
public class WordleSessionService {

    private static final int MAX_GUESSES = 6;

    private final WordleSessionRepository wordleSessionRepository;
    private final WordleDailyService wordleDailyService;
    private final WordleGuessEvaluator wordleGuessEvaluator;
    private final WordleGuessValidator wordleGuessValidator;

    public WordleSessionService(WordleSessionRepository wordleSessionRepository,
                                WordleDailyService wordleDailyService,
                                WordleGuessEvaluator wordleGuessEvaluator,
                                WordleGuessValidator wordleGuessValidator) {
        this.wordleSessionRepository = wordleSessionRepository;
        this.wordleDailyService = wordleDailyService;
        this.wordleGuessEvaluator = wordleGuessEvaluator;
        this.wordleGuessValidator = wordleGuessValidator;
    }

    /**
     * Finds today's session for the user, creating one if this is their first
     * visit today.
     *
     * @throws NoDailyWordException if no word is scheduled for today
     */
    @Transactional
    public WordleSession getOrCreateTodaysSession(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return wordleSessionRepository.findByUserIdAndPuzzleDate(user.getId(), today)
                .orElseGet(() -> {
                    wordleDailyService.getTodaysWord().orElseThrow(NoDailyWordException::new);
                    WordleSession created = wordleSessionRepository.save(new WordleSession(user, today));
                    log.info("Started Wordle game for user {} on {}", user.getId(), today);
                    return created;
                });
    }

    /**
     * Evaluates {@code rawGuess} against {@code todaysWord}, records it on
     * {@code session}, and updates the session's outcome if this guess wins
     * or exhausts the user's attempts.
     *
     * @throws InvalidGuessException        if the guess isn't exactly 5 letters
     * @throws InvalidGeorgianWordException if the guess isn't in the dictionary
     * @throws AlreadyCompletedException     if the session already has an outcome
     */
    @Transactional
    public GuessResult submitGuess(WordleSession session, WordleWord todaysWord, String rawGuess) {
        if (session.getOutcome() != WordleOutcome.IN_PROGRESS) {
            throw new AlreadyCompletedException();
        }
        if (rawGuess == null || rawGuess.trim().length() != WordleGuessEvaluator.WORD_LENGTH) {
            throw new InvalidGuessException();
        }
        if (!wordleGuessValidator.isValid(rawGuess)) {
            throw new InvalidGeorgianWordException();
        }

        String guess = rawGuess.trim().toLowerCase(Locale.ROOT);
        List<LetterFeedback> feedback = wordleGuessEvaluator.evaluateGuess(guess, todaysWord.getWord());

        int guessNumber = session.getGuesses().size() + 1;
        log.debug("Evaluated guess #{} for session {}: feedback={}", guessNumber, session.getId(), feedback);
        session.getGuesses().add(new WordleGuess(session, guess, guessNumber));

        boolean won = feedback
                .stream()
                .allMatch(f -> f == LetterFeedback.CORRECT);

        if (won) {
            session.setOutcome(WordleOutcome.WON);
        } else if (guessNumber >= MAX_GUESSES) {
            session.setOutcome(WordleOutcome.LOST);
        }

        wordleSessionRepository.save(session);
        if (session.getOutcome() != WordleOutcome.IN_PROGRESS) {
            log.info("User {} finished game {} outcome={} attempts={}",
                    session.getUser().getId(), session.getId(), session.getOutcome(), guessNumber);
        }
        return new GuessResult(feedback, session.getOutcome());
    }

    public record GuessResult(List<LetterFeedback> feedback, WordleOutcome outcome) {
    }

    public static class NoDailyWordException extends RuntimeException {
        public NoDailyWordException() {
            super("No Wordle word is scheduled for today");
        }
    }

    public static class InvalidGuessException extends RuntimeException {
        public InvalidGuessException() {
            super("Guess must be exactly " + WordleGuessEvaluator.WORD_LENGTH + " letters");
        }
    }

    public static class AlreadyCompletedException extends RuntimeException {
        public AlreadyCompletedException() {
            super("Today's puzzle is already completed");
        }
    }
}
