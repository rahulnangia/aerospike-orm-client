package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.util.Map;

/**
 * @author rahul
 */
public class MapConverter implements BinConverter<Map> {

    @Override public Value writeInAerospike(Map source) {
        return Value.getFromRecordObject(source);
    }

    @Override public Map readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Map) {
            return (Map) dest;
        }
        throw new ConversionException(Map.class, MapConverter.class);
    }
}
