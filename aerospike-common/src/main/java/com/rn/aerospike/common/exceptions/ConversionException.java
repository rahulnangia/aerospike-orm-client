package com.rn.aerospike.common.exceptions;

import com.rn.aerospike.common.converters.BinConverter;

/**
 * @author rahul
 */
public class ConversionException extends Exception {

    public ConversionException(String message) {
        super(message);
    }

    public ConversionException(Class clazz, Class<? extends BinConverter> converterClazz) {
        super("Unexpected type from aerospike " + clazz + " for " + converterClazz);
    }

    public ConversionException(Exception e) {
        super(e);
    }
}
