package com.rn.aerospike.client.exceptions;

/**
 * Created by rahul
 */
public class InvalidDTOException extends RuntimeException {
    public InvalidDTOException(String message) {
        super(message);
    }

    public InvalidDTOException(String message, Throwable e) {
        super(message, e);
    }
}
