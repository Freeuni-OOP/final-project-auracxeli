package com.auracxeli.connections;

public class AlreadyCompletedException extends RuntimeException {

    public AlreadyCompletedException() {
        super("Today's Connections puzzle is already completed");
    }
}
