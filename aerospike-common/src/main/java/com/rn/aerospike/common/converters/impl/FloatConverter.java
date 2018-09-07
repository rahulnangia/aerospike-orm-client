package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public class FloatConverter implements BinConverter<Float> {

    @Override public Value writeInAerospike(Float source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Float readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Integer || dest instanceof Long) {
            return (float) Double.longBitsToDouble(((Number) dest).longValue());
        }
        if (dest instanceof Double) {
            return ((Double) dest).floatValue();
        }
        if (dest instanceof Float) {
            return ((Float) dest);
        }
        return null;
    }
}
