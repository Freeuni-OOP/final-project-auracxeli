package com.auracxeli.archive;

import com.auracxeli.connections.ConnectionsBoardView;
import com.auracxeli.connections.ConnectionsGroup;
import com.auracxeli.connections.ConnectionsGuessEvaluator;
import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsPuzzle;
import com.auracxeli.connections.ConnectionsWord;
import com.auracxeli.wordle.LetterFeedback;
import com.auracxeli.wordle.WordleController;
import com.auracxeli.wordle.WordleGuessEvaluator;
import com.auracxeli.wordle.WordleGuessValidator;
import com.auracxeli.wordle.WordleOutcome;
import com.auracxeli.wordle.WordleWord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 * Builds practice (archive) boards for past dates. Practice games are ephemeral:
 * the caller keeps the raw list of guesses (in the HTTP session) and asks this
 * service to replay them against that day's word/puzzle to render the board.
 * Nothing here is persisted and no game-finished event is published, so practice
 * never affects stats, streaks, achievements, or the leaderboard.
 */
@Service
@RequiredArgsConstructor
public class PracticeService {

    public static final int WORDLE_MAX_GUESSES = 6;
    public static final int CONNECTIONS_MAX_MISTAKES = 4;
    private static final int CONNECTIONS_GROUP_COUNT = 4;

    private final WordleGuessEvaluator wordleGuessEvaluator;
    private final WordleGuessValidator wordleGuessValidator;
    private final ConnectionsGuessEvaluator connectionsGuessEvaluator;

    // ---------- Wordle ----------

    /** @throws InvalidPracticeGuessException if the guess isn't a valid 5-letter Georgian word */
    public void validateWordleGuess(String guess) {
        if (guess == null || guess.trim().length() != WordleGuessEvaluator.WORD_LENGTH) {
            throw new InvalidPracticeGuessException(
                    "Guess must be exactly " + WordleGuessEvaluator.WORD_LENGTH + " letters");
        }
        if (!wordleGuessValidator.isValid(guess)) {
            throw new InvalidPracticeGuessException("Guess is not a valid Georgian word");
        }
    }

    /** Replays {@code guesses} against {@code word} to build the board to render. */
    public WordlePracticeView buildWordleBoard(List<String> guesses, WordleWord word) {
        List<List<WordleController.Tile>> rows = new ArrayList<>();
        WordleOutcome outcome = WordleOutcome.IN_PROGRESS;

        for (String guess : guesses) {
            List<LetterFeedback> feedback = wordleGuessEvaluator.evaluateGuess(guess, word.getWord());
            rows.add(toTiles(guess, feedback));
            if (feedback.stream().allMatch(f -> f == LetterFeedback.CORRECT)) {
                outcome = WordleOutcome.WON;
            }
        }
        if (outcome != WordleOutcome.WON && guesses.size() >= WORDLE_MAX_GUESSES) {
            outcome = WordleOutcome.LOST;
        }
        while (rows.size() < WORDLE_MAX_GUESSES) {
            rows.add(blankRow());
        }

        boolean readOnly = outcome != WordleOutcome.IN_PROGRESS;
        String revealedWord = outcome == WordleOutcome.LOST ? word.getWord() : null;
        return new WordlePracticeView(rows, outcome, readOnly, guesses.size(), WORDLE_MAX_GUESSES, revealedWord);
    }

    private List<WordleController.Tile> toTiles(String word, List<LetterFeedback> feedback) {
        List<WordleController.Tile> tiles = new ArrayList<>();
        String upper = word.toUpperCase(Locale.ROOT);
        for (int i = 0; i < feedback.size(); i++) {
            tiles.add(new WordleController.Tile(String.valueOf(upper.charAt(i)), cssClassFor(feedback.get(i))));
        }
        return tiles;
    }

    private List<WordleController.Tile> blankRow() {
        List<WordleController.Tile> tiles = new ArrayList<>();
        for (int i = 0; i < WordleGuessEvaluator.WORD_LENGTH; i++) {
            tiles.add(new WordleController.Tile("", "t"));
        }
        return tiles;
    }

    private String cssClassFor(LetterFeedback feedback) {
        return switch (feedback) {
            case CORRECT -> "t g";
            case PRESENT -> "t y";
            case ABSENT -> "t x";
        };
    }

    // ---------- Connections ----------

    /** @throws InvalidPracticeGuessException if the selection isn't 4 distinct words */
    public void validateConnectionsSelection(List<String> words) {
        if (words == null || words.size() != CONNECTIONS_GROUP_COUNT
                || words.stream().anyMatch(w -> w == null || w.isBlank())
                || new HashSet<>(words).size() != CONNECTIONS_GROUP_COUNT) {
            throw new InvalidPracticeGuessException("Selection must be 4 distinct words");
        }
    }

    /** Replays {@code guesses} (each a 4-word selection) against {@code puzzle}. */
    public ConnectionsBoardView buildConnectionsBoard(List<List<String>> guesses, ConnectionsPuzzle puzzle) {
        Set<ConnectionsGroup> solved = new LinkedHashSet<>();   // identity-based, in solve order
        int mistakes = 0;

        for (List<String> guess : guesses) {
            Optional<ConnectionsGroup> match = connectionsGuessEvaluator.matchingGroup(guess, puzzle);
            if (match.isPresent()) {
                solved.add(match.get());
            } else {
                mistakes++;
            }
        }

        ConnectionsOutcome outcome;
        if (solved.size() == CONNECTIONS_GROUP_COUNT) {
            outcome = ConnectionsOutcome.WON;
        } else if (mistakes >= CONNECTIONS_MAX_MISTAKES) {
            outcome = ConnectionsOutcome.LOST;
        } else {
            outcome = ConnectionsOutcome.IN_PROGRESS;
        }
        boolean readOnly = outcome != ConnectionsOutcome.IN_PROGRESS;

        List<ConnectionsBoardView.GroupView> solvedGroups = solved.stream().map(this::toGroupView).toList();

        List<ConnectionsBoardView.GroupView> revealedGroups = new ArrayList<>();
        if (outcome == ConnectionsOutcome.LOST) {
            for (ConnectionsGroup group : puzzle.getGroups()) {
                if (!solved.contains(group)) {
                    revealedGroups.add(toGroupView(group));
                }
            }
        }

        return new ConnectionsBoardView(solvedGroups, remainingWords(puzzle, solved),
                mistakes, CONNECTIONS_MAX_MISTAKES, outcome, readOnly, revealedGroups);
    }

    /** "correct", "one_away", or "wrong" - drives the tile shake / toast, mirrors the daily game. */
    public String classifyConnectionsGuess(List<String> words, ConnectionsPuzzle puzzle) {
        if (connectionsGuessEvaluator.isCorrectGroup(words, puzzle)) {
            return "correct";
        }
        for (ConnectionsGroup group : puzzle.getGroups()) {
            if (connectionsGuessEvaluator.isAlmostCorrect(words, group)) {
                return "one_away";
            }
        }
        return "wrong";
    }

    private List<String> remainingWords(ConnectionsPuzzle puzzle, Set<ConnectionsGroup> solved) {
        List<String> allWords = new ArrayList<>();
        Set<String> solvedWords = new HashSet<>();
        for (ConnectionsGroup group : puzzle.getGroups()) {
            for (ConnectionsWord word : group.getWords()) {
                allWords.add(word.getWord());
                if (solved.contains(group)) {
                    solvedWords.add(word.getWord());
                }
            }
        }
        // Stable order per puzzle date (not per session) so the grid doesn't reshuffle between renders.
        Collections.shuffle(allWords, new Random(puzzle.getPuzzleDate().toEpochDay()));

        List<String> remaining = new ArrayList<>();
        for (String word : allWords) {
            if (!solvedWords.contains(word)) {
                remaining.add(word);
            }
        }
        return remaining;
    }

    private ConnectionsBoardView.GroupView toGroupView(ConnectionsGroup group) {
        List<String> words = group.getWords().stream().map(ConnectionsWord::getWord).toList();
        return new ConnectionsBoardView.GroupView(group.getCategory(), group.getDifficulty(), words);
    }

    /** View model for a practice Wordle board - mirrors what {@code wordle.html} expects. */
    public record WordlePracticeView(
            List<List<WordleController.Tile>> rows,
            WordleOutcome outcome,
            boolean readOnly,
            int attemptsUsed,
            int maxAttempts,
            String revealedWord
    ) {
    }

    public static class InvalidPracticeGuessException extends RuntimeException {
        public InvalidPracticeGuessException(String message) {
            super(message);
        }
    }
}
