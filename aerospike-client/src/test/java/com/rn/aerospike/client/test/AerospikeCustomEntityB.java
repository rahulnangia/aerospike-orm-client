package com.rn.aerospike.client.test;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by rahul
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AerospikeCustomEntityB {

    private String stringValue;
    private int intValue;
    private boolean boolValue;
}
