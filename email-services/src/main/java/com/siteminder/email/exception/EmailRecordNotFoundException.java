package com.siteminder.email.exception;

public class EmailRecordNotFoundException extends RuntimeException {


    public EmailRecordNotFoundException(String message) {

        super(message);
    }

    public EmailRecordNotFoundException(final String message, final Throwable cause) {

        super(message, cause);
    }
}
