package com.rn.aerospike.client.test;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rahul
 */
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AerospikeCustomEntityA {

    private String stringValue;
    private int intValue;
}
