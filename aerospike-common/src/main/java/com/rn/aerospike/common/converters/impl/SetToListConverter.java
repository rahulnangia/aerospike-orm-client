package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.util.List;
import java.util.Set;

/**
 * Created by rahul
 */
public class SetToListConverter implements BinConverter<Set> {

    @Override public Value writeInAerospike(Set source) {
        return Value.getFromRecordObject(source == null ? null : Lists.newLinkedList(source));
    }

    @Override public Set readFromAerospike(Object dest) throws ConversionException {
        if(dest == null){
            return null;
        }
        if (dest instanceof List) {
            return Sets.newHashSet((List) dest);
        }
        throw new ConversionException(List.class, SetToListConverter.class);
    }

}
