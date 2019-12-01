package com.siteminder.email.exception;

public class EmailPayloadProcessingException extends RuntimeException {

    public EmailPayloadProcessingException(String message) {
        super(message);
    }

    public EmailPayloadProcessingException(final String message, final Throwable cause){
        super(message, cause);
    }

}
