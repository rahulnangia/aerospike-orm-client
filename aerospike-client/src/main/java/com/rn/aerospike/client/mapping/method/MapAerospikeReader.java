package com.rn.aerospike.client.mapping.method;

import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.exceptions.JsonUtilityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by rahul
 */
public class MapAerospikeReader<ENTITY_KEY,ENTITY_VALUE, AEROSPIKE_KEY, AEROSPIKE_VALUE> extends AerospikeReader<Map<AEROSPIKE_KEY, AEROSPIKE_VALUE>> {


    private BinConverter<ENTITY_KEY> entityKeyConverter;

    private BinConverter<ENTITY_VALUE> entityValueConverter;

    private Class<ENTITY_KEY> keyClazz;

    private Class<ENTITY_VALUE> valueClazz;

    private BinConverter defaultKeyClassConverter;

    private BinConverter defaultValueClassConverter;

    public MapAerospikeReader(Method method, BinConverter<Map<AEROSPIKE_KEY, AEROSPIKE_VALUE>> baseConverter, BinConverter<ENTITY_KEY> entityKeyConverter, BinConverter<ENTITY_VALUE> entityValueConverter, Class<ENTITY_KEY> keyClazz, Class<ENTITY_VALUE> valueClazz) {
        super(method, baseConverter);
        this.entityKeyConverter = entityKeyConverter;
        this.entityValueConverter = entityValueConverter;
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
        this.defaultKeyClassConverter = StorableType.getBinConverterForClass(keyClazz);
        this.defaultValueClassConverter = StorableType.getBinConverterForClass(valueClazz);
    }

    public void invoke(Object object, Object aerospikeValue) throws InvocationTargetException, IllegalAccessException, JsonUtilityException, ConversionException, InstantiationException {
        //First read value from aerospike
        Map<AEROSPIKE_KEY, AEROSPIKE_VALUE> values = baseConverter.readFromAerospike(aerospikeValue);
        //Now convert this to Object Type in Entity
        Map<ENTITY_KEY, ENTITY_VALUE> objectValues = null;
        if (values != null) {
            objectValues = new HashMap<>(values.size());
            ENTITY_KEY objectKey;
            ENTITY_VALUE objectValue;
            boolean keyConvertibleUsable = keyClazz!=null && Convertible.class.isAssignableFrom(keyClazz);
            boolean valueConvertibleUsable = valueClazz!=null && Convertible.class.isAssignableFrom(valueClazz);
            for (Map.Entry<AEROSPIKE_KEY, AEROSPIKE_VALUE> aerospikeEntry : values.entrySet()) {
                objectKey = null;
                objectValue = null;

                //Convert Key
                if(entityKeyConverter != null) {
                    objectKey = entityKeyConverter.readFromAerospike(aerospikeEntry.getKey());
                }else if(keyConvertibleUsable){
                    Convertible convertible = (Convertible) keyClazz.newInstance();
                    convertible.readFromAerospike(aerospikeEntry.getKey());
                    objectKey = (ENTITY_KEY) convertible;
                }else if(defaultKeyClassConverter !=null){
                    objectKey = (ENTITY_KEY) defaultKeyClassConverter.readFromAerospike(aerospikeEntry.getKey());
                }

                //Convert Value
                if(entityValueConverter != null){
                    objectValue = entityValueConverter.readFromAerospike(aerospikeEntry.getValue());
                }else if(valueConvertibleUsable){
                    Convertible convertible = (Convertible) valueClazz.newInstance();
                    convertible.readFromAerospike(aerospikeEntry.getValue());
                    objectValue = (ENTITY_VALUE) convertible;
                }else if(defaultValueClassConverter!=null){
                    objectValue = (ENTITY_VALUE) defaultValueClassConverter.readFromAerospike(aerospikeEntry.getValue());
                }

                objectValues.put(objectKey, objectValue);
            }
        }
        //Set the object value in entity
        method.invoke(object, objectValues);
    }
}
