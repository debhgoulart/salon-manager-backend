package com.manager.salon.exception;

public class ClosedPeriodException extends RuntimeException {
    public ClosedPeriodException(String message) {
        super(message);
    }
}
