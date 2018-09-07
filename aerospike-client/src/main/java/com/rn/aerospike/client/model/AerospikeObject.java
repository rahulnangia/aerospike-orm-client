package com.rn.aerospike.client.model;

import com.aerospike.client.Bin;
import com.aerospike.client.Key;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Created by rahul
 */
@AllArgsConstructor
@Getter
public class AerospikeObject {

    private Key key;

    private Bin[] bins;
}
