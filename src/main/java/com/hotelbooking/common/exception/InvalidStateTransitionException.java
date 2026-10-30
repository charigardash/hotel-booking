package com.hotelbooking.common.exception;

public class InvalidStateTransitionException extends ConflictException {
    public InvalidStateTransitionException(String message) {
        super(message);
    }
}
