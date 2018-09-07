package com.rn.aerospike.client.model;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.exceptions.ConversionException;

/**
 * Created by rahul.
 */
public class AerospikeLockValue implements Convertible {

    private AerospikeLockValueEnum value;

    public AerospikeLockValue() {
        super();
    }

    public AerospikeLockValue(AerospikeLockValueEnum value) {
        this.value = value;
    }


    @Override
    public Value writeInAerospike() {
        return Value.getFromRecordObject(value.name());
    }

    @Override
    public void readFromAerospike(Object o) throws ConversionException {
        if (o != null && o instanceof String) {
            this.value = AerospikeLockValueEnum.valueOf(o.toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AerospikeLockValue that = (AerospikeLockValue) o;

        return value == that.value;

    }

    public AerospikeLockValueEnum getValue() {
        return value;
    }

    public void setValue(AerospikeLockValueEnum value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return "AerospikeLockValue{" +
                "value=" + value +
                '}';
    }

    public enum AerospikeLockValueEnum {
        PROCESSING,
        COMPLETED;
    }
}
