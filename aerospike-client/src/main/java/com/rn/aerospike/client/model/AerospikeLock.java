package com.rn.aerospike.client.model;

import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.records.impl.AerospikeRecordStringKey;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Created by rahul
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public abstract class AerospikeLock implements AerospikeRecordStringKey {

    private String key;

    @StorableBin(name = "value", type = StorableType.CUSTOM)
    private AerospikeLockValue value;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AerospikeLock that = (AerospikeLock) o;

        if (!key.equals(that.key)) return false;
        return value == that.value;

    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AerospikeLockKeyValue{" +
                "key='" + key + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public String getAerospikeKey() {
        return key;
    }

    @Override
    public void setAerospikeKey(String s) {
        this.key = s;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    @Override
    public Class<String> getAerospikeKeyType() {
        return String.class;
    }
}
