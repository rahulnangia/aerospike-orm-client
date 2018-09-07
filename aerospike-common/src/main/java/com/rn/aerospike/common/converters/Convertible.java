package com.rn.aerospike.common.converters;

import com.aerospike.client.Value;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * @author rahul
 */
public interface Convertible {

    Value writeInAerospike();

    void readFromAerospike(Object dest) throws ConversionException;
}
