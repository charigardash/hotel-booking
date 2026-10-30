package com.hotelbooking.common.exception;

/** Invalid input or a violated domain invariant. Maps to HTTP 400 in the API layer. */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
