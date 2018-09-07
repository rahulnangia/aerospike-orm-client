package com.rn.aerospike.client.utils;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by rahul
 */
public class Triple<T1,T2,T3> extends Tuple<T1, T2> {

    @Getter
    @Setter
    private T3 third;

    public Triple(T1 first, T2 second, T3 third) {
        super(first, second);
        this.third = third;
    }
}
