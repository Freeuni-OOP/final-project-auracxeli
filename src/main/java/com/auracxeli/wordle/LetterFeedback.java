package com.auracxeli.wordle;

/**
 * Feedback for a single guessed letter, Wordle-style.
 */
public enum LetterFeedback {

    /** Right letter in the right position (green). */
    CORRECT,

    /** Letter is in the word but in a different position (yellow). */
    PRESENT,

    /** Letter is not in the word, or all its occurrences are already accounted for (gray). */
    ABSENT
}
