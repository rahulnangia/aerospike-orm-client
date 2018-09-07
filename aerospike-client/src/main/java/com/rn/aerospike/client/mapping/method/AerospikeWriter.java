package com.rn.aerospike.client.mapping.method;

import com.aerospike.client.Value;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;
import com.rn.aerospike.common.exceptions.ConversionException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by rahul
 */
public class AerospikeWriter<T> {

    protected final Method method;
    protected BinConverter<T> baseConverter;

    public AerospikeWriter(Method method, BinConverter<T> baseConverter) {
        this.method = method;
        this.baseConverter = baseConverter;
    }

    public AerospikeWriter(Method method) {
        this.method = method;
    }

    public Value invoke(Object object) throws InvocationTargetException, IllegalAccessException, ConversionException, InstantiationException {
        T value = (T) (method.invoke(object));
        if (baseConverter == null) {
            if(value == null){
                return Value.getFromRecordObject(null);
            }
            if (value instanceof Convertible) {
                return ((Convertible) value).writeInAerospike();
            } else {
                throw new ConversionException("No BaseConverter specified for Non-Convertible object: "+ value.getClass().getName());
            }
        }
        return baseConverter.writeInAerospike(value);
    }
}
