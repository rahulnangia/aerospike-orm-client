package com.rn.aerospike.client.test;

import com.aerospike.client.Value;
import com.google.common.base.Joiner;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * Created by rahul
 */
public class AerospikeCustomEntityConverterB implements BinConverter<AerospikeCustomEntityB> {

    @Override
    public Value writeInAerospike(AerospikeCustomEntityB source) {
        if (source == null) {
            return Value.getFromRecordObject(null);
        }
        return Value.getFromRecordObject(Joiner.on("~").join(source.getStringValue(), source.isBoolValue(), source.getIntValue()));
    }

    @Override
    public AerospikeCustomEntityB readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        String value = dest.toString();
        String[] values = value.split("~", -1);
        return new AerospikeCustomEntityB(values[0], Integer.parseInt(values[2]), Boolean.parseBoolean(values[1]));
    }

}