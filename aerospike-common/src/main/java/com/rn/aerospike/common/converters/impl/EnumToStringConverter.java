package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * Created by rahul
 */
public class EnumToStringConverter implements BinConverter<Enum> {

    private Class enumClass;

    public EnumToStringConverter(Class enumClass) {
        this.enumClass = enumClass;
    }

    @Override public Value writeInAerospike(Enum source) {
        return Value.getFromRecordObject(source == null ? null : source.name());
    }

    @Override public Enum readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        String name = dest.toString();
        try {
            return Enum.valueOf(enumClass, name);
        } catch (IllegalArgumentException e) {
            throw new ConversionException(e);
        }
    }
}
