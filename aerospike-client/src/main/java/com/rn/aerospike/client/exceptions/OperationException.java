package com.rn.aerospike.client.exceptions;

import com.aerospike.client.AerospikeException;

/**
 * Created by rahul
 */
public class OperationException extends Exception {

    private final int errorCode;

    public OperationException() {
        super();
        this.errorCode = -1;
    }

    public OperationException(String message) {
        super(message);
        this.errorCode = -1;
    }

    public OperationException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = -1;
    }

    public OperationException(Throwable cause) {
        super(cause);
        this.errorCode = -1;
    }

    public OperationException(int errorCode, String message, AerospikeException e) {
        super(message, e);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
