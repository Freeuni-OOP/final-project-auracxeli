package com.auracxeli.connections;

import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ConnectionsGuessEvaluator {

    public static final int GROUP_SIZE = 4;

    /**
     * Returns the group whose words exactly equal the selection, or empty if the
     * selection doesn't form a complete category.
     *
     * @throws IllegalArgumentException if the selection isn't 4 distinct, non-null words
     */
    public Optional<ConnectionsGroup> matchingGroup(List<String> selected, ConnectionsPuzzle puzzle) {
        Set<String> selection = validatedSelection(selected);
        return puzzle.getGroups().stream()
                .filter(group -> wordsOf(group).equals(selection))
                .findFirst();
    }

    /** True if the selection exactly matches one of the puzzle's groups. */
    public boolean isCorrectGroup(List<String> selected, ConnectionsPuzzle puzzle) {
        return matchingGroup(selected, puzzle).isPresent();
    }

    /**
     * "One away" check: exactly 3 of the 4 selected words belong to {@code group}
     * (an exact 4/4 match is correct, not almost-correct, so it returns false).
     *
     * @throws IllegalArgumentException if the selection isn't 4 distinct, non-null words
     */
    public boolean isAlmostCorrect(List<String> selected, ConnectionsGroup group) {
        Set<String> selection = validatedSelection(selected);
        Set<String> groupWords = wordsOf(group);
        long overlap = selection.stream().filter(groupWords::contains).count();
        return overlap == GROUP_SIZE - 1;
    }

    private Set<String> wordsOf(ConnectionsGroup group) {
        return group.getWords().stream()
                .map(ConnectionsWord::getWord)
                .collect(Collectors.toSet());
    }

    private Set<String> validatedSelection(List<String> selected) {
        if (selected == null || selected.size() != GROUP_SIZE) {
            throw new IllegalArgumentException("selection must contain exactly " + GROUP_SIZE + " words");
        }
        if (selected.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("selection must not contain null words");
        }
        Set<String> distinct = new HashSet<>(selected);
        if (distinct.size() != GROUP_SIZE) {
            throw new IllegalArgumentException("selection must not contain duplicate words");
        }
        return distinct;
    }
}
