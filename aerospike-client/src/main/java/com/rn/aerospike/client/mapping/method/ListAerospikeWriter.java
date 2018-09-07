package com.rn.aerospike.client.mapping.method;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rahul
 */
public class ListAerospikeWriter<ENTITY_TYPE, AEROSPIKE_TYPE> extends AerospikeWriter<List<AEROSPIKE_TYPE>> {

    private final BinConverter<ENTITY_TYPE> valuesConverter;

    private Class<ENTITY_TYPE> valueClazz;

    public ListAerospikeWriter(Method method, BinConverter<List<AEROSPIKE_TYPE>> baseConverter, BinConverter<ENTITY_TYPE> keyConverter) {
        super(method, baseConverter);
        this.valuesConverter = keyConverter;
    }

    public ListAerospikeWriter(Method method, BinConverter<List<AEROSPIKE_TYPE>> baseConverter, Class<ENTITY_TYPE> keyClazz) {
        super(method, baseConverter);
        this.valueClazz = keyClazz;
        this.valuesConverter = null;
    }

    @Override
    public Value invoke(Object object) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        List<ENTITY_TYPE> values = (List<ENTITY_TYPE>) method.invoke(object);
        List<AEROSPIKE_TYPE> aerospikeValues = null;
        if (values != null) {
            aerospikeValues = new ArrayList<>(values.size());
            AEROSPIKE_TYPE aerospikeValue;
            boolean convertibleUsable = valueClazz != null && Convertible.class.isAssignableFrom(valueClazz);
            for (ENTITY_TYPE value : values) {
                aerospikeValue = null;
                if (valuesConverter != null) {
                    aerospikeValue = (AEROSPIKE_TYPE) valuesConverter.writeInAerospike(value).getObject();
                } else if (convertibleUsable) {
                    Convertible convertible = (Convertible) value;
                    aerospikeValue = (AEROSPIKE_TYPE) convertible.writeInAerospike().getObject();
                }
                aerospikeValues.add(aerospikeValue);
            }
        }
        return baseConverter.writeInAerospike(aerospikeValues);
    }

}
