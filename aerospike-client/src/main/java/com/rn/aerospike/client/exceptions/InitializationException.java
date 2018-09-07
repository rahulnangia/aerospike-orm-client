package com.rn.aerospike.client.exceptions;

/**
 * Created by rahul
 */
public class InitializationException extends RuntimeException {
    public InitializationException(String message) {
        super(message);
    }

    public InitializationException(String message, Throwable e) {
        super(message, e);
    }
}
