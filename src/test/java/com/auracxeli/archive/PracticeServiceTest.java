package com.auracxeli.archive;

import com.auracxeli.connections.ConnectionsBoardView;
import com.auracxeli.connections.ConnectionsGroup;
import com.auracxeli.connections.ConnectionsGuessEvaluator;
import com.auracxeli.connections.ConnectionsOutcome;
import com.auracxeli.connections.ConnectionsPuzzle;
import com.auracxeli.connections.ConnectionsWord;
import com.auracxeli.wordle.WordleGuessEvaluator;
import com.auracxeli.wordle.WordleGuessValidator;
import com.auracxeli.wordle.WordleOutcome;
import com.auracxeli.wordle.WordleWord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PracticeServiceTest {

    private static final String TARGET = "სახლი";   // 5 Georgian letters
    private static final String WRONG = "წიგნი";    // 5 Georgian letters, not the target

    private WordleGuessValidator wordleGuessValidator;
    private PracticeService practiceService;

    @BeforeEach
    void setUp() {
        wordleGuessValidator = mock(WordleGuessValidator.class);
        practiceService = new PracticeService(
                new WordleGuessEvaluator(), wordleGuessValidator, new ConnectionsGuessEvaluator());
    }

    // ---------- Wordle ----------

    @Test
    void wordleEmptyBoardIsInProgressWithSixRows() {
        PracticeService.WordlePracticeView board = practiceService.buildWordleBoard(List.of(), word());
        assertThat(board.outcome()).isEqualTo(WordleOutcome.IN_PROGRESS);
        assertThat(board.readOnly()).isFalse();
        assertThat(board.attemptsUsed()).isZero();
        assertThat(board.rows()).hasSize(PracticeService.WORDLE_MAX_GUESSES);
        assertThat(board.revealedWord()).isNull();
    }

    @Test
    void wordleCorrectGuessWins() {
        PracticeService.WordlePracticeView board = practiceService.buildWordleBoard(List.of(TARGET), word());
        assertThat(board.outcome()).isEqualTo(WordleOutcome.WON);
        assertThat(board.readOnly()).isTrue();
        assertThat(board.revealedWord()).isNull();   // word is only revealed on a loss
    }

    @Test
    void wordleSixWrongGuessesLosesAndRevealsWord() {
        List<String> guesses = new ArrayList<>();
        for (int i = 0; i < PracticeService.WORDLE_MAX_GUESSES; i++) {
            guesses.add(WRONG);
        }
        PracticeService.WordlePracticeView board = practiceService.buildWordleBoard(guesses, word());
        assertThat(board.outcome()).isEqualTo(WordleOutcome.LOST);
        assertThat(board.revealedWord()).isEqualTo(TARGET);
    }

    @Test
    void validateWordleGuessRejectsWrongLengthAndUnknownWord() {
        when(wordleGuessValidator.isValid(TARGET)).thenReturn(true);
        practiceService.validateWordleGuess(TARGET);   // valid: right length + in dictionary

        assertThatThrownBy(() -> practiceService.validateWordleGuess("აბ"))
                .isInstanceOf(PracticeService.InvalidPracticeGuessException.class);

        when(wordleGuessValidator.isValid(WRONG)).thenReturn(false);
        assertThatThrownBy(() -> practiceService.validateWordleGuess(WRONG))
                .isInstanceOf(PracticeService.InvalidPracticeGuessException.class);
    }

    private WordleWord word() {
        return new WordleWord(TARGET, LocalDate.of(2026, 6, 15), 1L);
    }

    // ---------- Connections ----------

    @Test
    void connectionsEmptyBoardShowsAllSixteenWords() {
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(List.of(), puzzle());
        assertThat(board.outcome()).isEqualTo(ConnectionsOutcome.IN_PROGRESS);
        assertThat(board.remainingWords()).hasSize(16);
        assertThat(board.solvedGroups()).isEmpty();
        assertThat(board.mistakesUsed()).isZero();
    }

    @Test
    void connectionsCorrectGroupIsSolved() {
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(
                List.of(List.of("a1", "a2", "a3", "a4")), puzzle());
        assertThat(board.solvedGroups()).hasSize(1);
        assertThat(board.remainingWords()).hasSize(12);
        assertThat(board.outcome()).isEqualTo(ConnectionsOutcome.IN_PROGRESS);
    }

    @Test
    void connectionsAllFourGroupsWins() {
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(List.of(
                List.of("a1", "a2", "a3", "a4"),
                List.of("b1", "b2", "b3", "b4"),
                List.of("c1", "c2", "c3", "c4"),
                List.of("d1", "d2", "d3", "d4")), puzzle());
        assertThat(board.outcome()).isEqualTo(ConnectionsOutcome.WON);
        assertThat(board.remainingWords()).isEmpty();
    }

    @Test
    void connectionsFourMistakesLosesAndRevealsGroups() {
        List<String> wrong = List.of("a1", "a2", "b1", "b2");
        ConnectionsBoardView board = practiceService.buildConnectionsBoard(
                List.of(wrong, wrong, wrong, wrong), puzzle());
        assertThat(board.outcome()).isEqualTo(ConnectionsOutcome.LOST);
        assertThat(board.mistakesUsed()).isEqualTo(4);
        assertThat(board.revealedGroups()).hasSize(4);
    }

    @Test
    void classifyConnectionsGuessDetectsCorrectOneAwayAndWrong() {
        ConnectionsPuzzle puzzle = puzzle();
        assertThat(practiceService.classifyConnectionsGuess(List.of("a1", "a2", "a3", "a4"), puzzle))
                .isEqualTo("correct");
        assertThat(practiceService.classifyConnectionsGuess(List.of("a1", "a2", "a3", "b1"), puzzle))
                .isEqualTo("one_away");
        assertThat(practiceService.classifyConnectionsGuess(List.of("a1", "a2", "b1", "b2"), puzzle))
                .isEqualTo("wrong");
    }

    @Test
    void validateConnectionsSelectionRejectsWrongCountAndDuplicates() {
        practiceService.validateConnectionsSelection(List.of("a1", "a2", "a3", "a4"));   // no throw
        assertThatThrownBy(() -> practiceService.validateConnectionsSelection(List.of("a1", "a2", "a3")))
                .isInstanceOf(PracticeService.InvalidPracticeGuessException.class);
        assertThatThrownBy(() -> practiceService.validateConnectionsSelection(List.of("a1", "a1", "a2", "a3")))
                .isInstanceOf(PracticeService.InvalidPracticeGuessException.class);
    }

    private ConnectionsPuzzle puzzle() {
        ConnectionsPuzzle puzzle = new ConnectionsPuzzle(LocalDate.of(2026, 6, 15));
        addGroup(puzzle, "A", 0, "a1", "a2", "a3", "a4");
        addGroup(puzzle, "B", 1, "b1", "b2", "b3", "b4");
        addGroup(puzzle, "C", 2, "c1", "c2", "c3", "c4");
        addGroup(puzzle, "D", 3, "d1", "d2", "d3", "d4");
        return puzzle;
    }

    private void addGroup(ConnectionsPuzzle puzzle, String category, int difficulty, String... words) {
        ConnectionsGroup group = new ConnectionsGroup(puzzle, category, difficulty);
        for (String w : words) {
            group.getWords().add(new ConnectionsWord(group, w));
        }
        puzzle.getGroups().add(group);
    }
}
