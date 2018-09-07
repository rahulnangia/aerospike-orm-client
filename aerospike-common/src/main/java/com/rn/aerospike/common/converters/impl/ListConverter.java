package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.util.List;

/**
 * @author rahul
 */
public class ListConverter<T> implements BinConverter<List<T>> {

    @Override public Value writeInAerospike(List<T> source) {
        return Value.getFromRecordObject(source);
    }

    @Override public List<T> readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof List) {
            return (List) dest;
        }
        throw new ConversionException(List.class, ListConverter.class);
    }

}
