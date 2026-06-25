package com.auracxeli.user;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a profile is requested for a username that does not exist.
 * Annotated so Spring maps it straight to a 404, keeping controllers thin.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String username) {
        super("No user found with username: " + username);
    }
}
