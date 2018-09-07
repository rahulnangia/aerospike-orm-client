package com.rn.aerospike.common.records;

/**
 * Created by rahul
 */
public interface AerospikeRecord<KEY_TYPE> {

    public abstract KEY_TYPE getAerospikeKey();

    public abstract void setAerospikeKey(KEY_TYPE value);

    public abstract Class<KEY_TYPE> getAerospikeKeyType();

}
