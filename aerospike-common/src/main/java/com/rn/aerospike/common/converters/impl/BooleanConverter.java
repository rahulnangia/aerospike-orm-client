package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class BooleanConverter implements BinConverter<Boolean> {

    @Override public Value writeInAerospike(Boolean source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Boolean readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Number) {
            return ((Number) dest).longValue() > 0;
        }
        if (dest instanceof Boolean) {
            return ((Boolean) dest);
        }

        return null;
    }
}
