package com.rn.aerospike.common.converters;

import com.aerospike.client.Value;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * Created by rahul
 */
public interface BinConverter<U> {

    Value writeInAerospike(U source);

    U readFromAerospike(Object dest) throws ConversionException;
}
