package com.rn.aerospike.client.test;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.exceptions.ConversionException;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * Created by rahul
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
public class AerospikeConvertibleEntityB implements Convertible {

    private String stringValue;
    private int intValue;
    private boolean boolValue;

    @Override
    public Value writeInAerospike() {
        return Value.getFromRecordObject(stringValue + "#" + boolValue + "#" + intValue);
    }

    @Override
    public void readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return;
        }
        String value = dest.toString();
        String[] values = value.split("\\#", -1);
        stringValue = values[0];
        boolValue = Boolean.parseBoolean(values[1]);
        intValue = Integer.parseInt(values[2]);
    }
}
