package com.rn.aerospike.client.model;

import com.rn.aerospike.common.records.impl.AerospikeRecordStringKey;
import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.annotations.StorableKey;
import com.rn.aerospike.common.enums.StorableType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by rahul
 */
@Getter
@Setter
@NoArgsConstructor
public abstract class Counter implements AerospikeRecordStringKey {

    @StorableKey
    private String key;

    public static final String binName = "count";

    @StorableBin(name = "count", type = StorableType.LONG)
    private Long count;

    @Override
    public String getAerospikeKey() {
        return key;
    }

    @Override
    public void setAerospikeKey(String key) {
        this.key = key;
    }

    @Override
    public Class<String> getAerospikeKeyType() {
        return String.class;
    }
}
