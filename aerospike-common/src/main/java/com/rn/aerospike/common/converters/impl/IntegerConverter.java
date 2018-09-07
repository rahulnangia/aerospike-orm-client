package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class IntegerConverter implements BinConverter<Integer> {

    @Override public Value writeInAerospike(Integer source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Integer readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Number) {
            return ((Number) dest).intValue();
        }
        return null;
    }

}
