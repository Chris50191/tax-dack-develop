package com.searly.taxcontrol.sii.exception;

public class SiiApiException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SiiApiException(String message) {
        super(message);
    }

    public SiiApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
