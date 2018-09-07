package com.rn.aerospike.client.mapping.method;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rahul
 */
public class MapAerospikeWriter<ENTITY_KEY, ENTITY_VALUE, AEROSPIKE_KEY, AEROSPIKE_VALUE> extends AerospikeWriter<Map<AEROSPIKE_KEY, AEROSPIKE_VALUE>> {

    private BinConverter<ENTITY_KEY> entityKeyConverter;

    private BinConverter<ENTITY_VALUE> entityValueConverter;

    private Class<ENTITY_KEY> keyClazz;

    private Class<ENTITY_VALUE> valueClazz;

    public MapAerospikeWriter(Method method, BinConverter<Map<AEROSPIKE_KEY, AEROSPIKE_VALUE>> baseConverter, BinConverter<ENTITY_KEY> entityKeyConverter, BinConverter<ENTITY_VALUE> entityValueConverter, Class<ENTITY_KEY> keyClazz, Class<ENTITY_VALUE> valueClazz) {
        super(method, baseConverter);
        this.entityKeyConverter = entityKeyConverter;
        this.entityValueConverter = entityValueConverter;
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
    }

    @Override
    public Value invoke(Object object) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Map<ENTITY_KEY, ENTITY_VALUE> values = (Map<ENTITY_KEY, ENTITY_VALUE>) method.invoke(object);
        Map<AEROSPIKE_KEY, AEROSPIKE_VALUE> aerospikeValues = null;
        if (values != null) {
            aerospikeValues = new HashMap<>(values.size());
            AEROSPIKE_KEY aerospikeKey;
            AEROSPIKE_VALUE aerospikeValue;
            boolean keyConvertibleUsable = keyClazz != null && Convertible.class.isAssignableFrom(keyClazz);
            boolean valueConvertibleUsable = valueClazz != null && Convertible.class.isAssignableFrom(valueClazz);
            for (Map.Entry<ENTITY_KEY, ENTITY_VALUE> entityEntry : values.entrySet()) {
                aerospikeKey = null;
                aerospikeValue = null;

                //Get Aerospike Key
                if (entityKeyConverter != null) {
                    aerospikeKey = (AEROSPIKE_KEY) entityKeyConverter.writeInAerospike(entityEntry.getKey()).getObject();
                }else if(keyConvertibleUsable){
                    Convertible convertible = (Convertible) entityEntry.getKey();
                    aerospikeKey = (AEROSPIKE_KEY) convertible.writeInAerospike().getObject();
                }else {
                    aerospikeKey = (AEROSPIKE_KEY) Value.getFromRecordObject(entityEntry.getKey());
                }

                //Get Aerospike Value
                if (entityValueConverter != null) {
                    aerospikeValue = (AEROSPIKE_VALUE) entityValueConverter.writeInAerospike(entityEntry.getValue()).getObject();
                }else if(valueConvertibleUsable){
                    Convertible convertible = (Convertible) entityEntry.getValue();
                    aerospikeValue = (AEROSPIKE_VALUE) convertible.writeInAerospike().getObject();
                } else {
                    aerospikeValue = (AEROSPIKE_VALUE) Value.getFromRecordObject(entityEntry.getValue());
                }
                aerospikeValues.put(aerospikeKey, aerospikeValue);
            }
        }
        return baseConverter.writeInAerospike(aerospikeValues);
    }
}
