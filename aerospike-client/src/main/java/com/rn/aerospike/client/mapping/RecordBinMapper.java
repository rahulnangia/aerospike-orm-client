package com.rn.aerospike.client.mapping;

import com.google.common.base.Joiner;
import com.rn.aerospike.client.exceptions.InitializationException;
import com.rn.aerospike.client.exceptions.InvalidDTOException;
import com.rn.aerospike.client.mapping.method.AerospikeReader;
import com.rn.aerospike.client.mapping.method.AerospikeWriter;
import com.rn.aerospike.client.mapping.method.ListAerospikeReader;
import com.rn.aerospike.client.mapping.method.ListAerospikeWriter;
import com.rn.aerospike.client.mapping.method.MapAerospikeReader;
import com.rn.aerospike.client.mapping.method.MapAerospikeWriter;
import com.rn.aerospike.client.utils.Tuple;
import com.rn.aerospike.common.records.AerospikeRecord;
import com.rn.aerospike.common.annotations.StorableBin;
import com.rn.aerospike.common.annotations.StorableKey;
import com.rn.aerospike.common.annotations.StorableRecord;
import com.rn.aerospike.common.converters.BinConverter;
import com.rn.aerospike.common.converters.Convertible;
import org.atteo.evo.classindex.ClassIndex;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

/**
 * Created by rahul
 */
public class RecordBinMapper {

    private static RecordBinMapper INSTANCE = null;
    private static final Object lock = new Object();
    private Map<Class, RuntimeException> errorMap;
    private final boolean lazyLoad;
    private Map<Class, Record> map;


    private RecordBinMapper(boolean lazyLoad) {
        this.map = new HashMap<>();
        this.errorMap = new HashMap<>();
        this.lazyLoad = lazyLoad;
        if(!this.lazyLoad) {
            init();
        }
    }

    public static RecordBinMapper getMapper(boolean lazyLoad) {
        if (INSTANCE == null) {
            synchronized (lock) {
                if (INSTANCE == null) {
                    INSTANCE = new RecordBinMapper(lazyLoad);
                }
            }
        }
        return INSTANCE;
    }

    private void init() {
        // Iterating over all the classes which are of type StorableRecord in
        // the build
        for (Class clazz : ClassIndex.getAnnotated(StorableRecord.class)) {
            init(clazz);
        }
    }

    private synchronized void init(Class<?> clazz) {
        if (!AerospikeRecord.class.isAssignableFrom(clazz)) {
            throw new InitializationException(
                    String.format("StorableRecord %s must implement interface AerospikeRecord", clazz.getName()));
        }

        Class baseClazz = clazz;

        String setName = ((StorableRecord) clazz.getAnnotation(StorableRecord.class)).setname();
        String namespace = ((StorableRecord) clazz.getAnnotation(StorableRecord.class)).namespace();

        Record record = new Record(setName, namespace);
        while (clazz != null && !clazz.equals(Object.class)) {
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(StorableKey.class)) {
                    if (record.getKeyClass() == null) {
                        record.setKeyClass(field.getType());
                    }
                }
                // Getting all the storable bins out there
                if (field.isAnnotationPresent(StorableBin.class)) {
                    StorableBin storableBinAnnotation = field.getAnnotation(StorableBin.class);
                    String binName = storableBinAnnotation.name();
                    try {
                        // Find getter and setters for the above properties
                        Method binGetter = new PropertyDescriptor(field.getName(), clazz).getReadMethod();
                        Method binSetter = new PropertyDescriptor(field.getName(), clazz).getWriteMethod();

                        AerospikeReader binAerospikeReader;
                        AerospikeWriter binAerospikeWriter;

                        switch (storableBinAnnotation.type()) {

                            case MAP:
                                Tuple<AerospikeReader, AerospikeWriter> mapReaderWriters = initializeMapReaderWriters(
                                        field, storableBinAnnotation, binGetter, binSetter);
                                binAerospikeReader = mapReaderWriters.getFirst();
                                binAerospikeWriter = mapReaderWriters.getSecond();

                                break;

                            case LIST:
                                Tuple<AerospikeReader, AerospikeWriter> listReaderWriters = initializeListReaderWriters(
                                        field, storableBinAnnotation, binGetter, binSetter);
                                binAerospikeReader = listReaderWriters.getFirst();
                                binAerospikeWriter = listReaderWriters.getSecond();

                                break;

                            case CUSTOM:
                                Tuple<AerospikeReader, AerospikeWriter> values = initializeCustomReaderWriters(
                                        storableBinAnnotation, field, binGetter, binSetter);
                                binAerospikeReader = values.getFirst();
                                binAerospikeWriter = values.getSecond();
                                break;

                            default:
                                // For default cases extract bin converter from
                                // StorableType enum
                                BinConverter binConvertor = storableBinAnnotation.type()
                                        .getBinConverter(field.getType());
                                binAerospikeReader = new AerospikeReader(binSetter, binConvertor);
                                binAerospikeWriter = new AerospikeWriter(binGetter, binConvertor);
                                break;

                        }
                        record.addBin(binName, binAerospikeWriter, binAerospikeReader, baseClazz,
                                !field.isAnnotationPresent(Deprecated.class));
                    } catch (IntrospectionException e) {
                        throw new InitializationException("Error while initializing RecordBinMapper ", e);
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        validate(baseClazz, record);
        map.put(baseClazz, record);
    }

    private void validate(Class<?> baseClass, Record record) {
        List<Tuple<Class<?>, String>> errorBins = new ArrayList<>();
        Map<String, List<RecordBin>> binMap = record.getBins();
        for (Map.Entry<String, List<RecordBin>> binEntry : binMap.entrySet()) {
            Boolean nonDepFound = record.getNonDeprecatedFound().get(binEntry.getKey());
            if (nonDepFound == null || !nonDepFound) {
                errorBins.add(new Tuple<Class<?>, String>(baseClass, binEntry.getKey()));
            }
        }
        if (errorBins.size() > 0) {
            Joiner joiner = Joiner.on("\t");
            throw new InvalidDTOException("NO NON DEPRECATED BINS FOUND FOR " + joiner.join(errorBins));
        }
    }

    private static Tuple<AerospikeReader, AerospikeWriter> initializeListReaderWriters(Field field,
            StorableBin storableBinAnnotation, Method binGetter, Method binSetter) {
        AerospikeReader binAerospikeReader;
        AerospikeWriter binAerospikeWriter;
        BinConverter baseConverter = storableBinAnnotation.type().getBinConverter(field.getType());
        Type fieldType = field.getGenericType();
        Class valueClazz = null;
        // Find the type of List<T> and store in valueClazz
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedFieldType = (ParameterizedType) fieldType;
            if (parameterizedFieldType.getActualTypeArguments()[0] instanceof Class) {
                valueClazz = (Class) parameterizedFieldType.getActualTypeArguments()[0];
            }
        }
        // If converter found , use list value converter
        if (!storableBinAnnotation.keyConverter().equals(StorableBin.Default.class)) {
            BinConverter listValueConverter;
            try {
                listValueConverter = storableBinAnnotation.keyConverter().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            binAerospikeReader = new ListAerospikeReader(binSetter, baseConverter, listValueConverter);
            binAerospikeWriter = new ListAerospikeWriter(binGetter, baseConverter, listValueConverter);
            
        } else if (valueClazz != null && Convertible.class.isAssignableFrom(valueClazz)) {
            // Else Check if the List entity is convertible
            binAerospikeReader = new ListAerospikeReader(binSetter, baseConverter, valueClazz);
            binAerospikeWriter = new ListAerospikeWriter(binGetter, baseConverter, valueClazz);
            
        } else {
            // Else Assume primitive type supported by aerospike
            binAerospikeReader = new AerospikeReader(binSetter, baseConverter);
            binAerospikeWriter = new AerospikeWriter(binGetter, baseConverter);
        }
        return new Tuple<>(binAerospikeReader, binAerospikeWriter);
    }
    
    private static Tuple<AerospikeReader, AerospikeWriter> initializeMapReaderWriters(Field field,
            StorableBin storableBinAnnotation, Method binGetter, Method binSetter) {
        AerospikeReader binAerospikeReader;
        AerospikeWriter binAerospikeWriter;
        BinConverter baseConverter = storableBinAnnotation.type().getBinConverter(field.getType());
        Type fieldType = field.getGenericType();
        Class keyClazz = null;
        Class valueClazz = null;
        // Find the type of Map<K,V> and store in keyClazz and valueClazz
        if (fieldType instanceof ParameterizedType) {
            ParameterizedType parameterizedFieldType = (ParameterizedType) fieldType;
            if (parameterizedFieldType.getActualTypeArguments()[0] instanceof Class) {
                keyClazz = (Class) parameterizedFieldType.getActualTypeArguments()[0];
            }
            if (parameterizedFieldType.getActualTypeArguments()[1] instanceof Class) {
                valueClazz = (Class) parameterizedFieldType.getActualTypeArguments()[1];
            }
        }
        BinConverter mapKeyConverter = null;
        BinConverter mapValueConverter = null;
        boolean useDefaultKeyConverters = false;
        boolean useDefaultValueConverters = false;
        // If converter found , use list value converter
        if (!storableBinAnnotation.keyConverter().equals(StorableBin.Default.class)) {
            try {
                mapKeyConverter = storableBinAnnotation.keyConverter().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (keyClazz == null || !Convertible.class.isAssignableFrom(keyClazz)) {
            useDefaultKeyConverters = true;
        }
        if (!storableBinAnnotation.valueConverter().equals(StorableBin.Default.class)) {
            try {
                mapValueConverter = storableBinAnnotation.valueConverter().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (valueClazz == null || !Convertible.class.isAssignableFrom(valueClazz)) {
            useDefaultValueConverters = true;
        }
        
        if (useDefaultKeyConverters && useDefaultValueConverters) {
            // If K,V in Map<K,V> entity is not convertible then it is not to be
            // used
            binAerospikeReader = new AerospikeReader(binSetter, baseConverter);
            binAerospikeWriter = new AerospikeWriter(binGetter, baseConverter);
        } else {
            binAerospikeReader = new MapAerospikeReader(binSetter, baseConverter, mapKeyConverter, mapValueConverter,
                    keyClazz, valueClazz);
            binAerospikeWriter = new MapAerospikeWriter(binGetter, baseConverter, mapKeyConverter, mapValueConverter,
                    keyClazz, valueClazz);
        }
        
        return new Tuple<>(binAerospikeReader, binAerospikeWriter);
    }
    
    private static Tuple<AerospikeReader, AerospikeWriter> initializeCustomReaderWriters(
            StorableBin storableBinAnnotation, Field field, Method binGetter, Method binSetter) {
        
        if (storableBinAnnotation.keyConverter().equals(StorableBin.Default.class)) {
            if (Convertible.class.isAssignableFrom(field.getType())) {
                return new Tuple<>(new AerospikeReader(binSetter, field.getType()), new AerospikeWriter(binGetter));
            } else {
                throw new InitializationException(
                        "Define keyConverter or use Convertible for Bin: " + storableBinAnnotation.name());
            }
        } else {
            BinConverter baseConverter = null;
            try {
                baseConverter = storableBinAnnotation.keyConverter().newInstance();
            } catch (Exception e) {
                throw new InitializationException(
                        "Unable to initialize keyConverter for Bin: " + storableBinAnnotation.name(), e);
            }
            return new Tuple<>(new AerospikeReader(binSetter, baseConverter),
                    new AerospikeWriter(binGetter, baseConverter));
        }
    }

    public Record getMapping(Class<?> clz) {
        if(lazyLoad && map.get(clz)==null){
            RuntimeException error = errorMap.get(clz);
            if (error != null) {
                throw error;
            }
            try {
                init(clz);
            }catch (InitializationException | InvalidDTOException e){
                errorMap.put(clz, e);
                throw e;
            }
        }
        return map.get(clz);
    }
}
