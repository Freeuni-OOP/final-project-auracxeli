package com.auracxeli.wordle;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Builds the {@link WordleBoardView} the Wordle template renders from a user's
 * daily session: evaluates each recorded guess into a row of tiles and pads the
 * board out to the maximum number of attempts. Pure read-side view assembly kept
 * out of the controller, mirroring {@link com.auracxeli.user.ProfileService}.
 */
@Service
@RequiredArgsConstructor
public class WordleBoardService {

    private static final int BOARD_ROWS = 6;

    private final WordleGuessEvaluator wordleGuessEvaluator;

    public WordleBoardView buildBoard(WordleSession session, WordleWord todaysWord) {
        List<List<Tile>> rows = new ArrayList<>();
        for (WordleGuess guess : session.getGuesses()) {
            List<LetterFeedback> feedback = wordleGuessEvaluator.evaluateGuess(guess.getGuessWord(), todaysWord.getWord());
            rows.add(toTiles(guess.getGuessWord(), feedback));
        }
        while (rows.size() < BOARD_ROWS) {
            rows.add(blankRow());
        }

        boolean readOnly = session.getOutcome() != WordleOutcome.IN_PROGRESS;
        String revealedWord = session.getOutcome() == WordleOutcome.LOST ? todaysWord.getWord() : null;

        return new WordleBoardView(rows, session.getOutcome(), readOnly,
                session.getGuesses().size(), BOARD_ROWS, revealedWord);
    }

    private List<Tile> toTiles(String word, List<LetterFeedback> feedback) {
        List<Tile> tiles = new ArrayList<>();
        String upper = word.toUpperCase(Locale.ROOT);
        for (int i = 0; i < feedback.size(); i++) {
            tiles.add(new Tile(String.valueOf(upper.charAt(i)), cssClassFor(feedback.get(i))));
        }
        return tiles;
    }

    private List<Tile> blankRow() {
        List<Tile> tiles = new ArrayList<>();
        for (int i = 0; i < WordleGuessEvaluator.WORD_LENGTH; i++) {
            tiles.add(new Tile("", "t"));
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
}
