package com.rn.aerospike.client.test;

import com.rn.aerospike.client.model.Counter;
import com.rn.aerospike.common.annotations.StorableRecord;
import lombok.NoArgsConstructor;

/**
 * Created by rahul.
 */
@NoArgsConstructor
@StorableRecord(setname = "counters" , namespace = "test")
public class TestCounter extends Counter {
}
