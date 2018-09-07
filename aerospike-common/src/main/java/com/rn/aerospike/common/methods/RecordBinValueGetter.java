package com.rn.aerospike.common.methods;

import com.aerospike.client.Record;
import com.google.common.collect.Maps;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by rahul
 */
public class RecordBinValueGetter {

    public static Map<Class<?>, Method> binValueGetterMap;

    static {
        binValueGetterMap = Maps.newHashMap();
        try {
            binValueGetterMap.put(Integer.class, Record.class.getDeclaredMethod("getInt", String.class));
            binValueGetterMap.put(String.class, Record.class.getDeclaredMethod("getString", String.class));
            binValueGetterMap.put(Byte[].class, Record.class.getDeclaredMethod("getByte", String.class));
            binValueGetterMap.put(Double.class, Record.class.getDeclaredMethod("getDouble", String.class));
            binValueGetterMap.put(Long.class, Record.class.getDeclaredMethod("getLong", String.class));
            binValueGetterMap.put(Float.class, Record.class.getDeclaredMethod("getFloat", String.class));
            binValueGetterMap.put(List.class, Record.class.getDeclaredMethod("getList", String.class));
            binValueGetterMap.put(Boolean.class, Record.class.getDeclaredMethod("getBoolean", String.class));
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Unable to find Record bin value getter methods ", e);
        }

    }

    private RecordBinValueGetter(){
    }


}
