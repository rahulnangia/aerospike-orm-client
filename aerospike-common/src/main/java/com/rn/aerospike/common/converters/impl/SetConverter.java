package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.util.Set;

/**
 * Created by rahul
 */
public class SetConverter <T> implements BinConverter<Set<T>> {
    @Override
    public Value writeInAerospike(Set<T> source) {
        return Value.getFromRecordObject(source);
    }

    @Override
    public Set<T> readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof Set) {
            return (Set) dest;
        }
        throw new ConversionException(Set.class, SetConverter.class);
    }
}
