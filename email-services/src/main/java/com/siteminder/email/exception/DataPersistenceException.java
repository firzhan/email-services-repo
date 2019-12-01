package com.siteminder.email.exception;

public class DataPersistenceException extends RuntimeException {

    public DataPersistenceException(String message) {
        super(message);
    }

    public DataPersistenceException(final String message, final Throwable cause){
        super(message, cause);
    }
}
