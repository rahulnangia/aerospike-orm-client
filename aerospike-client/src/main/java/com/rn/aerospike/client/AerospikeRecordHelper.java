package com.rn.aerospike.client;

import com.aerospike.client.Value;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.records.AerospikeRecord;

/**
 * Created by rahul.
 */
class AerospikeRecordHelper {

    public static Value getAerospikeKeyValue(AerospikeRecord record){
        return StorableType.getBinConverterForClass(record.getAerospikeKeyType()).writeInAerospike(record.getAerospikeKey());
    }

    public static <V> Value getAerospikeKeyValue(AerospikeRecord<V> record, V key){
        return StorableType.getBinConverterForClass(record.getAerospikeKeyType()).writeInAerospike(key);
    }

    public static void populateAerospikeKeyFromValue(AerospikeRecord record, Value key) throws ConversionException {
        record.setAerospikeKey(StorableType.getBinConverterForClass(record.getAerospikeKeyType()).readFromAerospike(key.getObject()));
    }
}
