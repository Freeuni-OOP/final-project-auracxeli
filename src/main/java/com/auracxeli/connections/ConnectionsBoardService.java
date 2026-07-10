package com.auracxeli.connections;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Builds the {@link ConnectionsBoardView} the Connections template renders from a
 * user's daily session, and classifies a submitted guess for UI feedback
 * (correct / one-away / wrong). Pure read-side view assembly kept out of the
 * controller, mirroring {@link com.auracxeli.user.ProfileService}.
 */
@Service
@RequiredArgsConstructor
public class ConnectionsBoardService {

    // must match ConnectionsSessionService.MAX_MISTAKES
    private static final int MAX_MISTAKES = 4;

    private final ConnectionsGuessEvaluator connectionsGuessEvaluator;

    public ConnectionsBoardView buildBoard(ConnectionsSession session, ConnectionsPuzzle puzzle) {
        boolean readOnly = session.getOutcome() != ConnectionsOutcome.IN_PROGRESS;

        List<ConnectionsBoardView.GroupView> solvedGroups = new ArrayList<>();
        Set<Long> solvedGroupIds = new HashSet<>();

        for (ConnectionsGuess guess : session.getGuesses()) {
            if (!guess.isCorrect()) {
                continue;
            }
            List<String> guessedWords = List.of(guess.getWord1(), guess.getWord2(), guess.getWord3(), guess.getWord4());
            connectionsGuessEvaluator.matchingGroup(guessedWords, puzzle).ifPresent(group -> {
                solvedGroupIds.add(group.getId());
                solvedGroups.add(toGroupView(group));
            });
        }

        List<String> remainingWords = shuffledUnsolvedWords(session, puzzle, solvedGroupIds);

        List<ConnectionsBoardView.GroupView> revealedGroups = new ArrayList<>();
        if (session.getOutcome() == ConnectionsOutcome.LOST) {
            for (ConnectionsGroup group : puzzle.getGroups()) {
                if (!solvedGroupIds.contains(group.getId())) {
                    revealedGroups.add(toGroupView(group));
                }
            }
        }

        return new ConnectionsBoardView(
                solvedGroups,
                remainingWords,
                session.getMistakesCount(),
                MAX_MISTAKES,
                session.getOutcome(),
                readOnly,
                revealedGroups
        );
    }

    /** "correct", "one_away" (3 of 4 words match some group), or "wrong". */
    public String classifyGuess(ConnectionsGuess guess, ConnectionsPuzzle puzzle) {
        if (guess.isCorrect()) {
            return "correct";
        }
        List<String> words = List.of(guess.getWord1(), guess.getWord2(), guess.getWord3(), guess.getWord4());
        for (ConnectionsGroup group : puzzle.getGroups()) {
            if (connectionsGuessEvaluator.isAlmostCorrect(words, group)) {
                return "one_away";
            }
        }
        return "wrong";
    }

    private ConnectionsBoardView.GroupView toGroupView(ConnectionsGroup group) {
        List<String> words = group.getWords().stream().map(ConnectionsWord::getWord).toList();
        return new ConnectionsBoardView.GroupView(group.getCategory(), group.getDifficulty(), words);
    }

    private List<String> shuffledUnsolvedWords(ConnectionsSession session, ConnectionsPuzzle puzzle, Set<Long> solvedGroupIds) {
        List<String> allWords = new ArrayList<>();
        List<ConnectionsGroup> groups = puzzle.getGroups();
        for (ConnectionsGroup group : groups) {
            for (ConnectionsWord word : group.getWords()) {
                allWords.add(word.getWord());
            }
        }
        Collections.shuffle(allWords, new Random(session.getId()));

        Set<String> solvedWords = new HashSet<>();
        for (ConnectionsGroup group : groups) {
            if (solvedGroupIds.contains(group.getId())) {
                group.getWords().forEach(w -> solvedWords.add(w.getWord()));
            }
        }

        List<String> remaining = new ArrayList<>();
        for (String word : allWords) {
            if (!solvedWords.contains(word)) {
                remaining.add(word);
            }
        }
        return remaining;
    }
}
