package com.rn.aerospike.client.test;

import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.annotations.StorableRecord;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.records.impl.AerospikeRecordStringKey;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by rahul
 */
@Getter
@Setter
@EqualsAndHashCode
@StorableRecord(setname = "test")
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AerospikeTestEntityB implements AerospikeRecordStringKey {

    @StorableBin(name = "string", type = StorableType.STRING)
    private String value1;

    @StorableBin(name = "double", type = StorableType.DOUBLE)
    private Double value2;

    @Override
    public String getAerospikeKey() {
        return value1;
    }

    @Override
    public void setAerospikeKey(String key) {
        this.value1 = key;
    }

    @Override
    public Class<String> getAerospikeKeyType() {
        return String.class;
    }
}
