package com.rn.aerospike.client.exceptions;

import com.aerospike.client.AerospikeException;

/**
 * Created by rahul
 */
public class KeyInitializationException extends OperationException {

    public KeyInitializationException() {
    }

    public KeyInitializationException(String message) {
        super(message);
    }

    public KeyInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyInitializationException(Throwable cause) {
        super(cause);
    }

    public KeyInitializationException(int errorCode, String message, AerospikeException e) {
        super(errorCode, message, e);
    }
}
