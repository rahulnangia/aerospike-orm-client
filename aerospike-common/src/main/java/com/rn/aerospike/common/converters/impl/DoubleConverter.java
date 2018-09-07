package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class DoubleConverter implements BinConverter<Double> {

    @Override public Value writeInAerospike(Double source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Double readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Integer || dest instanceof Long) {
            return Double.longBitsToDouble(((Number) dest).longValue());
        }
        if (dest instanceof Double) {
            return (Double) dest;
        }
        if (dest instanceof Float) {
            return ((Float) dest).doubleValue();
        }
        return null;
    }
}
