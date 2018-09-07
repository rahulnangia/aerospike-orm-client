package com.rn.aerospike.client.mapping.method;

import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.exceptions.JsonUtilityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author rahul
 */
public class ListAerospikeReader<ENTITY_TYPE, AEROSPIKE_TYPE> extends AerospikeReader<List<AEROSPIKE_TYPE>> {

    private BinConverter<ENTITY_TYPE> valuesConverter;

    private Class<ENTITY_TYPE> valueClazz;

    public ListAerospikeReader(Method method, BinConverter<List<AEROSPIKE_TYPE>> baseConverter, BinConverter<ENTITY_TYPE> keyConverter) {
        super(method, baseConverter);
        this.valuesConverter = keyConverter;
    }

    public ListAerospikeReader(Method method, BinConverter<List<AEROSPIKE_TYPE>> baseConverter, Class<ENTITY_TYPE> keyClazz) {
        super(method, baseConverter);
        this.valueClazz = keyClazz;
    }

    public void invoke(Object object, Object aerospikeValue) throws InvocationTargetException, IllegalAccessException, JsonUtilityException, ConversionException, InstantiationException {
        //First read value from aerospike
        List<AEROSPIKE_TYPE> values = baseConverter.readFromAerospike(aerospikeValue);
        //Now convert this to Object Type in Entity
        List<ENTITY_TYPE> objectValues = null;
        if (values != null) {
            objectValues = new ArrayList<>(values.size());
            ENTITY_TYPE objectValue;
            boolean convertibleUsable = valueClazz!=null && Convertible.class.isAssignableFrom(valueClazz);
            for (AEROSPIKE_TYPE value : values) {
                objectValue = null;
                if(valuesConverter != null) {
                    objectValue = valuesConverter.readFromAerospike(value);
                }else if(convertibleUsable){
                    Convertible convertible = (Convertible) valueClazz.newInstance();
                    convertible.readFromAerospike(value);
                    objectValue = (ENTITY_TYPE) convertible;
                }
                objectValues.add(objectValue);
            }
        }
        //Set the object value in entity
        method.invoke(object, objectValues);
    }
}
