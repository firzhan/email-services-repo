package com.siteminder.email.exception;

public class EmailClientNotAvailableException extends RuntimeException {
    public EmailClientNotAvailableException(String message) {
        super(message);
    }

    public EmailClientNotAvailableException(final String message, final Throwable cause){
        super(message, cause);
    }

}