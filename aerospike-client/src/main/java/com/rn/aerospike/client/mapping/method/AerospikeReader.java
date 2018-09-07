package com.rn.aerospike.client.mapping.method;

import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.enums.StorableType;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.exceptions.JsonUtilityException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by rahul
 */
public class AerospikeReader<T> {

    protected final Method method;
    protected BinConverter<T> baseConverter;
    protected Class<T> clazz;

    public AerospikeReader(Method method, BinConverter<T> baseConverter) {
        this.method = method;
        this.baseConverter = baseConverter;
    }

    public AerospikeReader(Method method, Class<T> clazz) {
        this.method = method;
        this.clazz = clazz;
    }

    public void invoke(Object object, Object aerospikeValue) throws InvocationTargetException, IllegalAccessException,
            JsonUtilityException, ConversionException, InstantiationException {
        Object value = null;
        if (baseConverter == null) {
            if (Convertible.class.isAssignableFrom(clazz)) {
                T convertible = null;
                try {
                    convertible = clazz.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    throw e;
                }
                ((Convertible) convertible).readFromAerospike(aerospikeValue);
                value = convertible;
            }
        } else {
            value = baseConverter.readFromAerospike(aerospikeValue);
        }
        Class<?> param = method.getParameterTypes()[0];
        if (value != null) {
            Class<?> valueClazz = value.getClass();
            if (param.isAssignableFrom(valueClazz) || valueClazz.isAssignableFrom(param) || (StorableType.fromClass(param).equals(StorableType.fromClass(valueClazz)))) {
                method.invoke(object, value);
            } else {
                throw new ConversionException(
                        String.format("INVALID CLASS %s IN AEROSPIKE FOR %s", valueClazz, param));
            }
        }
    }
}
