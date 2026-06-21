package com.auracxeli.user;

public class DuplicateUserException extends RuntimeException {

    private final String field;   // "username" ან "email"

    public String getField() {
        return field;
    }
    public DuplicateUserException(String field, String message) {
        super(message);
        this.field = field;
    }
}