package com.auracxeli.connections;

public class InvalidSelectionException extends RuntimeException {

    public InvalidSelectionException() {
        super("A guess must be exactly " + ConnectionsGuessEvaluator.GROUP_SIZE + " distinct words");
    }
}
