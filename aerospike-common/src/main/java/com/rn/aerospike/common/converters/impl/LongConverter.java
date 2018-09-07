package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class LongConverter implements BinConverter<Long> {

    @Override public Value writeInAerospike(Long source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Long readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Number) {
            return ((Number) dest).longValue();
        }
        return null;
    }
}
