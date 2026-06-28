package com.auracxeli.connections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectionsGuessEvaluatorTest {

    private final ConnectionsGuessEvaluator evaluator = new ConnectionsGuessEvaluator();

    private ConnectionsPuzzle puzzle;
    private ConnectionsGroup colors;
    private ConnectionsGroup animals;

    @BeforeEach
    void setUp() {
        puzzle = new ConnectionsPuzzle(LocalDate.now());
        colors  = addGroup(puzzle, "ფერები", 1, "წითელი", "ლურჯი", "მწვანე", "ყვითელი");
        animals = addGroup(puzzle, "ცხოველები", 2, "ძაღლი", "კატა", "ცხენი", "ლომი");
        addGroup(puzzle, "ხილი", 3, "ვაშლი", "მსხალი", "ატამი", "ბანანი");
        addGroup(puzzle, "ქალაქები", 4, "თბილისი", "ბათუმი", "ქუთაისი", "რუსთავი");
    }

    @Test
    void exactMatchIsCorrectGroupTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "ყვითელი");

        assertTrue(evaluator.isCorrectGroup(selection, puzzle));
        assertEquals(Optional.of(colors), evaluator.matchingGroup(selection, puzzle));
    }

    @Test
    void orderDoesNotMatterTest() {
        List<String> selection = List.of("ყვითელი", "მწვანე", "წითელი", "ლურჯი");

        assertTrue(evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void wrongMixIsNotCorrectTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "ძაღლი", "კატა");

        assertFalse(evaluator.isCorrectGroup(selection, puzzle));
        assertEquals(Optional.empty(), evaluator.matchingGroup(selection, puzzle));
    }

    @Test
    void almostCorrectDetectedTest() {
        // three colors + one animal
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "ძაღლი");

        assertTrue(evaluator.isAlmostCorrect(selection, colors));
        assertFalse(evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void exactMatchIsNotAlmostCorrectTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "ყვითელი");

        assertFalse(evaluator.isAlmostCorrect(selection, colors));
    }

    @Test
    void twoOverlapIsNotAlmostCorrectTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "ძაღლი", "კატა");

        assertFalse(evaluator.isAlmostCorrect(selection, colors));
        assertFalse(evaluator.isAlmostCorrect(selection, animals));
    }

    @Test
    void duplicateWordInSelectionThrowsTest() {
        List<String> selection = List.of("წითელი", "წითელი", "ლურჯი", "მწვანე");

        assertThrows(IllegalArgumentException.class, () -> evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void selectionNotFourWordsThrowsTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე");

        assertThrows(IllegalArgumentException.class, () -> evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void nullSelectionThrowsTest() {
        assertThrows(IllegalArgumentException.class, () -> evaluator.isCorrectGroup(null, puzzle));
    }

    @Test
    void nullWordInSelectionThrowsTest() {
        List<String> selection = Arrays.asList("წითელი", "ლურჯი", "მწვანე", null);

        assertThrows(IllegalArgumentException.class, () -> evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void moreThanFourWordsThrowsTest() {
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "ყვითელი", "ძაღლი");

        assertThrows(IllegalArgumentException.class, () -> evaluator.isCorrectGroup(selection, puzzle));
    }

    @Test
    void zeroOverlapIsNotAlmostCorrectTest() {
        List<String> allAnimals = List.of("ძაღლი", "კატა", "ცხენი", "ლომი");

        assertFalse(evaluator.isAlmostCorrect(allAnimals, colors));
    }

    @Test
    void unknownWordIsNotCorrectButCanBeAlmostCorrectTest() {
        // three real colors + a word that's in no group
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "უცნობი");

        assertFalse(evaluator.isCorrectGroup(selection, puzzle));
        assertEquals(Optional.empty(), evaluator.matchingGroup(selection, puzzle));
        assertTrue(evaluator.isAlmostCorrect(selection, colors));
    }

    @Test
    void emptyPuzzleHasNoMatchingGroupTest() {
        ConnectionsPuzzle emptyPuzzle = new ConnectionsPuzzle(LocalDate.now());
        List<String> selection = List.of("წითელი", "ლურჯი", "მწვანე", "ყვითელი");

        assertFalse(evaluator.isCorrectGroup(selection, emptyPuzzle));
        assertEquals(Optional.empty(), evaluator.matchingGroup(selection, emptyPuzzle));
    }

    @Test
    void allFourGroupsAreGuessableTest() {
        for (ConnectionsGroup group : puzzle.getGroups()) {
            List<String> selection = group.getWords().stream()
                    .map(ConnectionsWord::getWord)
                    .toList();

            assertTrue(evaluator.isCorrectGroup(selection, puzzle));
            assertEquals(Optional.of(group), evaluator.matchingGroup(selection, puzzle));
        }
    }

    private ConnectionsGroup addGroup(ConnectionsPuzzle puzzle, String category, int difficulty, String... words) {
        ConnectionsGroup group = new ConnectionsGroup(puzzle, category, difficulty);
        for (String word : words) {
            group.getWords().add(new ConnectionsWord(group, word));
        }
        puzzle.getGroups().add(group);
        return group;
    }
}
