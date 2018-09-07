package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class ByteArrayConverter implements BinConverter<byte[]> {

    @Override public Value writeInAerospike(byte[] source) {
        return Value.getFromRecordObject(source);
    }

    @Override public byte[] readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof byte[]) {
            return (byte[]) dest;
        }
        throw new ConversionException(byte[].class, ByteArrayConverter.class);
    }
}
