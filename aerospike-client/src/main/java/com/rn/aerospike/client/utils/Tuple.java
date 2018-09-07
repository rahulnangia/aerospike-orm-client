package com.rn.aerospike.client.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author rahul
 */
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Tuple<T1, T2> {

    private T1 first;
    private T2 second;

}
