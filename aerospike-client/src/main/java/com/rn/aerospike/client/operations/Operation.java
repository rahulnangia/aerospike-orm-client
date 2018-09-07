package com.rn.aerospike.client.operations;

import com.rn.aerospike.client.exceptions.OperationException;
import com.rn.aerospike.common.records.AerospikeRecord;

/**
 * Created by rahul
 */
public interface Operation<T extends AerospikeRecord> {

    T operate(T newRecord, T foundRecord) throws OperationException;
}
