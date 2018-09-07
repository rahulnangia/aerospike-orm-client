package com.rn.aerospike.client.mapping;

import com.rn.aerospike.client.mapping.method.AerospikeReader;
import com.rn.aerospike.client.mapping.method.AerospikeWriter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Created by rahul
 */
@AllArgsConstructor
@Getter
@ToString
public class RecordBin {
    
    private AerospikeWriter<?> aerospikeWriter;
    
    private AerospikeReader<?> aerospikeReader;
}
