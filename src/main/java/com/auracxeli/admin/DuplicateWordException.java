package com.auracxeli.admin;

public class DuplicateWordException extends RuntimeException {

    private final String field; // "word" or "scheduledDate"

    public DuplicateWordException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
