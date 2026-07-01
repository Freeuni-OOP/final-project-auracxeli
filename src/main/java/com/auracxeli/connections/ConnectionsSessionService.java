package com.auracxeli.connections;

import com.auracxeli.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * THis Orchestrates a user's daily Connections game: finds or creates today's session
 * and applies a submitted guess. evaluate then record then update outcome.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionsSessionService {

    private static final int MAX_MISTAKES = 4;

    private final ConnectionsSessionRepository connectionsSessionRepository;
    private final ConnectionsGuessEvaluator connectionsGuessEvaluator;

    /**
     * Find today's session for the user. if it cant find it creates one on their first visit today.
     */
    @Transactional
    public ConnectionsSession getOrCreateTodaysSession(User user) {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return connectionsSessionRepository.findByPuzzleDateAndUserId(today, user.getId())
                .orElseGet(() -> {
                    ConnectionsSession created = connectionsSessionRepository.save(new ConnectionsSession(user, today));
                    log.info("Started Connections game for user {} on {}", user.getId(), today);
                    return created;
                });
    }

    /**
     * Record a guess of 4 selected words against the puzzle: a correct guess
     * solves a group (and wins once all groups are found); a wrong guess counts
     * as a mistake and user loses on 4th guees.
     *
     * @throws AlreadyCompletedException if the session already has an outcome
     * @throws InvalidSelectionException if the selection isn't exactly 4 distinct, non-null words
     */
    @Transactional(noRollbackFor = {AlreadyCompletedException.class, InvalidSelectionException.class})
    public ConnectionsSession submitGuess(ConnectionsSession session, ConnectionsPuzzle puzzle, Set<String> selectedWords) {
        //this method is like wordle session service method
        if (session.getOutcome() != ConnectionsOutcome.IN_PROGRESS) {
            throw new AlreadyCompletedException();
        }
        if (selectedWords == null) {
            throw new InvalidSelectionException();
        }
        List<String> words = new ArrayList<>(selectedWords);
        if (words.size() != ConnectionsGuessEvaluator.GROUP_SIZE || words.contains(null)) {
            throw new InvalidSelectionException();
        }
        boolean correct = connectionsGuessEvaluator.isCorrectGroup(words, puzzle);
        int guessNumber = session.getGuesses().size() + 1;
        session.getGuesses().add(new ConnectionsGuess(session, words.get(0), words.get(1), words.get(2), words.get(3), correct, guessNumber));
        log.debug("Connections guess #{} for session {}: correct={}", guessNumber, session.getId(), correct);

        if (correct) {
            long groupsFound = session.getGuesses().stream().filter(ConnectionsGuess::isCorrect).count();
            if (groupsFound >= puzzle.getGroups().size()) {
                session.setOutcome(ConnectionsOutcome.WON);
            }
        } else {
            session.incrementMistakes();
            if (session.getMistakesCount() >= MAX_MISTAKES) {
                session.setOutcome(ConnectionsOutcome.LOST);
            }
        }

        ConnectionsSession saved = connectionsSessionRepository.save(session);
        if (saved.getOutcome() != ConnectionsOutcome.IN_PROGRESS) {
            log.info("User {} finished Connections game {} outcome={} mistakes={}",
                    saved.getUser().getId(), saved.getId(), saved.getOutcome(), saved.getMistakesCount());
        }
        return saved;
    }
}
