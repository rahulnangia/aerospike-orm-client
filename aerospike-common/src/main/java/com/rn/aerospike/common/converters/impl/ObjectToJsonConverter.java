package com.rn.aerospike.common.converters.impl;

import com.aerospike.client.Value;
import com.google.gson.Gson;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.exceptions.ConversionException;
import com.rn.aerospike.common.exceptions.JsonUtilityException;
import com.rn.aerospike.common.utils.JsonUtility;

/**
 * Created by rahul
 */
public class ObjectToJsonConverter<T> implements BinConverter<T> {

    private Class<T> objectClass;
    private Gson     gson;

    public ObjectToJsonConverter(Class<T> objectClass) {
        this.objectClass = objectClass;
        this.gson = null;
    }

    public ObjectToJsonConverter(Class objectClass, Gson gson) {
        this.objectClass = objectClass;
        this.gson = gson;
    }

    @Override public Value writeInAerospike(Object value) {
        String json = null;
        if (gson == null) {
            json = JsonUtility.getInstance().toJson(value);
        } else {
            json = gson.toJson(value);
        }
        return Value.getFromRecordObject(json);
    }

    @Override public T readFromAerospike(Object dest) throws ConversionException {
        if (dest == null) {
            return null;
        }
        String json = dest.toString();
        try {
            if (gson == null) {
                return JsonUtility.getInstance().fromJson(json, objectClass);
            }
            return gson.fromJson(json, objectClass);
        } catch (JsonUtilityException e) {
            throw new ConversionException(e);
        }
    }

}
