package com.rn.aerospike.common.enums;

import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.impl.BooleanConverter;
import com.rn.aerospike.common.converters.impl.ByteArrayConverter;
import com.rn.aerospike.common.converters.impl.DoubleConverter;
import com.rn.aerospike.common.converters.impl.EnumToStringConverter;
import com.rn.aerospike.common.converters.impl.FloatConverter;
import com.rn.aerospike.common.converters.impl.IntegerConverter;
import com.rn.aerospike.common.converters.impl.ListConverter;
import com.rn.aerospike.common.converters.impl.LongConverter;
import com.rn.aerospike.common.converters.impl.MapConverter;
import com.rn.aerospike.common.converters.impl.ObjectToJsonConverter;
import com.rn.aerospike.common.converters.impl.SetToListConverter;
import com.rn.aerospike.common.converters.impl.StringConverter;
import com.rn.aerospike.common.utils.Tuple;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by rahul
 */
public enum StorableType {

    INT(new IntegerConverter(), true, Integer.class, int.class),
    LONG(new LongConverter(), true, Long.class, long.class),
    FLOAT(new FloatConverter(), true, Float.class, float.class),
    STRING(new StringConverter(), true, String.class),
    BYTES(new ByteArrayConverter(), true, Byte[].class),
    DOUBLE(new DoubleConverter(), true, Double.class, double.class),
    BOOLEAN(new BooleanConverter(), true, Boolean.class, boolean.class),
    LIST(new ListConverter(), true, List.class),
    MAP(new MapConverter(), true, Map.class),
    SET(new SetToListConverter(), List.class),
    JSON(null, String.class),
    ENUM(null, String.class),
    CUSTOM(null, null);

    @Getter
    private Class[] aerospikeType;

    private BinConverter binConvertor;

    private boolean considerUniversal;

    static {
        initializeClassMap();
    }

    StorableType(BinConverter binConvertor, Class... aerospikeType) {
        this.aerospikeType = aerospikeType;
        this.binConvertor = binConvertor;
        this.considerUniversal = false;
    }

    StorableType(BinConverter binConvertor, boolean considerUniversal, Class... aerospikeType) {
        this.aerospikeType = aerospikeType;
        this.binConvertor = binConvertor;
        this.considerUniversal = considerUniversal;
    }

    private static Map<Class, Tuple<BinConverter, StorableType>> classConverterStorableMap;

    public BinConverter getBinConverter(Class sourceObjectClass) {
        switch (this) {
            case JSON: {
                return new ObjectToJsonConverter(sourceObjectClass);
            }
            case ENUM: {
                return new EnumToStringConverter(sourceObjectClass);
            }
            default: {
                return this.binConvertor;
            }
        }
    }

    public  static StorableType fromClass(Class clazz){
        if (clazz != null) {
            Tuple<BinConverter, StorableType> classTuple = classConverterStorableMap.get(clazz);
            return classTuple == null ? null : classTuple.getSecond();
        }
        return null;
    }

    public static BinConverter getBinConverterForClass(Class clazz) {
        if (clazz != null) {
            Tuple<BinConverter, StorableType> classTuple = classConverterStorableMap.get(clazz);
            return classTuple == null ? null : classTuple.getFirst();
        }
        return null;
    }

    private static void initializeClassMap() {
        classConverterStorableMap = new HashMap<>();
        for (StorableType type : StorableType.values()) {
            if (type.considerUniversal) {
                for(Class aerospikeClass : type.aerospikeType) {
                    if (classConverterStorableMap.containsKey(aerospikeClass) ) {
                        throw new RuntimeException(String.format("Mulptiple convertors/StorableTypes defined for %s class. Check StorableType.java", aerospikeClass == null ? null : aerospikeClass.getName()));
                    }
                    classConverterStorableMap.put(aerospikeClass, new Tuple<>(type.binConvertor, type));
                }
            }
        }
    }
}
