package com.rn.aerospike.client.test;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.impl.SetConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by rahul
 */
public class IntegerSetConverter implements BinConverter<Set<Integer>> {
    public IntegerSetConverter() {
    }

    public Value writeInAerospike(Set<Integer> source) {
        return Value.getFromRecordObject(source);
    }

    public Set<Integer> readFromAerospike(Object dest) throws ConversionException {
        if(dest == null) {
            return null;
        } else if(dest instanceof Set) {
            Set<Integer> integerSet = new HashSet<>();
            for(Object element : (Set)dest){
                if(element instanceof Number){
                    integerSet.add(((Number)element).intValue());
                }
            }
            return integerSet;
        } else {
            throw new ConversionException(Set.class, SetConverter.class);
        }
    }

}
