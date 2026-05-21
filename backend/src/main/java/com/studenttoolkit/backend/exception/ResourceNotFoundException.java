package com.studenttoolkit.backend.exception;

/**
 * Custom exception thrown when a requested resource is not found in the database.
 * For example, when a user tries to access a CGPA history entry that doesn't exist.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s not found with %s: %s", resource, field, value));
    }
}