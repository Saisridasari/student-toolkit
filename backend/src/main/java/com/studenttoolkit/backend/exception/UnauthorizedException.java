package com.studenttoolkit.backend.exception;

/**
 * Custom exception thrown when a user attempts an action they are not authorized for.
 * For example, when a student tries to access admin-only endpoints.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}