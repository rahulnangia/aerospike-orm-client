package com.rn.aerospike.client.test;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;
import lombok.NoArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author rahul
 */
@NoArgsConstructor
public class ListAerospikeCustomEntityConverterA implements BinConverter<List<AerospikeConvertibleEntityA>> {

    @Override
    public Value writeInAerospike(List<AerospikeConvertibleEntityA> source) {
        if (source == null) {
            return Value.getFromRecordObject(null);
        }
        StringBuilder s = new StringBuilder();
        for(AerospikeConvertibleEntityA customEntityA : source){
            s.append(customEntityA.writeInAerospike().toString());
            s.append(",");
        }
        String stringValue = s.length() == 0 ? "" : s.substring(0, s.length()-1);
        return Value.getFromRecordObject(stringValue);
    }

    @Override
    public List<AerospikeConvertibleEntityA> readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        String value = dest.toString();
        String[] valueList = value.split(",", -1);
        List<AerospikeConvertibleEntityA> customEntityAList = new LinkedList<>();
        for(String element : valueList) {
            AerospikeConvertibleEntityA convertibleEntityA = new AerospikeConvertibleEntityA();
            convertibleEntityA.readFromAerospike(element);
            customEntityAList.add(convertibleEntityA);
        }
        return customEntityAList;
    }

}
