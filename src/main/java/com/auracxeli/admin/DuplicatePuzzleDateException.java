package com.auracxeli.admin;

public class DuplicatePuzzleDateException extends RuntimeException {
    private final String field; // this is for teh puzzledate
    public DuplicatePuzzleDateException(String message) {
        super(message);
        this.field = field;
    }


    public String getField() {return field;}
}
