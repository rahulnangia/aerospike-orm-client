package com.rn.aerospike.client.test;

import com.rn.aerospike.client.model.AerospikeLock;
import com.rn.aerospike.common.annotations.StorableRecord;
import lombok.NoArgsConstructor;

/**
 * Created by rahul
 */
@NoArgsConstructor
@StorableRecord(setname = "testLocks")
public class AerospikeTestLock extends AerospikeLock {
}
