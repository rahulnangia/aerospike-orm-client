package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;
import lombok.NoArgsConstructor;

/**
 * @author rahul
 */
@NoArgsConstructor
public class StringConverter implements BinConverter<String> {

    @Override public Value writeInAerospike(String source) {
        return Value.getFromRecordObject(source);
    }

    @Override public String readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        return dest.toString();
    }
}
