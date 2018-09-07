package com.rn.aerospike.client.test;

import com.aerospike.client.Value;
import com.google.common.base.Joiner;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;
import lombok.NoArgsConstructor;

/**
 * @author rahul
 */
@NoArgsConstructor
public class AerospikeCustomEntityConverterA implements BinConverter<AerospikeCustomEntityA> {

    @Override
    public Value writeInAerospike(AerospikeCustomEntityA source) {
        if (source == null) {
            return Value.getFromRecordObject(null);
        }
        return Value.getFromRecordObject(Joiner.on("|").join(source.getStringValue(), source.getIntValue()));
    }

    @Override
    public AerospikeCustomEntityA readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        String value = dest.toString();
        String[] values = value.split("\\|", -1);
        return new AerospikeCustomEntityA(values[0], Integer.parseInt(values[1]));
    }

}
