package com.auracxeli.wordle;

/**
 * Thrown when a submitted guess is not a valid Georgian word in the Wordle
 * dictionary. Carries a player-facing Georgian message exposed as a public
 * constant so tests and the controller share a single source of truth.
 */
public class InvalidGeorgianWordException extends RuntimeException {

    public static final String MESSAGE = "ასეთი ქართული სიტყვა არ არსებობს";

    public InvalidGeorgianWordException() {
        super(MESSAGE);
    }
}
