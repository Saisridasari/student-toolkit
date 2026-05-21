package com.studenttoolkit.backend.exception;

/**
 * Custom exception thrown when attempting to create a resource that already exists.
 * For example, when a user tries to register with an email that is already taken.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String resource, String field, Object value) {
        super(String.format("%s already exists with %s: %s", resource, field, value));
    }
}